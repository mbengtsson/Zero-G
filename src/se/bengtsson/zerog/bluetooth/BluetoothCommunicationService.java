package se.bengtsson.zerog.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import se.bengtsson.zerog.bluetooth.message.BluetoothMessage;
import se.bengtsson.zerog.bluetooth.message.FireMessage;
import se.bengtsson.zerog.bluetooth.message.OpponentHitMessage;
import se.bengtsson.zerog.bluetooth.message.PlayerHitMessage;
import se.bengtsson.zerog.bluetooth.message.RotationMessage;
import se.bengtsson.zerog.bluetooth.message.SyncPositionMessage;
import se.bengtsson.zerog.bluetooth.message.SyncRotationMessage;
import se.bengtsson.zerog.bluetooth.message.SyncVelocityMessage;
import se.bengtsson.zerog.bluetooth.message.ThrustMessage;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class BluetoothCommunicationService extends Service {

	public static final byte ROTATION_FLAG = 0x1;
	public static final byte THRUST_FLAG = 0x2;
	public static final byte FIRE_FLAG = 0x3;
	public static final byte SYNC_ROTATION_FLAG = 0x4;
	public static final byte SYNC_VELOCITY_FLAG = 0x5;
	public static final byte SYNC_POSITION_FLAG = 0x6;
	public static final byte PLAYER_HIT_FLAG = 0x7;
	public static final byte OPPONENT_HIT_FLAG = 0x8;

	private final IBinder binder = new LocalBinder();

	private BluetoothCommunicationThread communicationThread;
	private WifiManager wifi;

	private Set<BluetoothCommunicationListener> listeners = new HashSet<BluetoothCommunicationListener>();

	private boolean originalWifiState = false;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("BluetoothCommunicationService", "Creating service");

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		originalWifiState = wifi.isWifiEnabled();
		wifi.setWifiEnabled(false);
		Log.d("BluetoothCommunicationService", "Wifi disabled");

	}

	public void initiate(BluetoothSocket socket) {
		communicationThread = new BluetoothCommunicationThread(socket);
		communicationThread.start();
		Log.d("BluetoothCommunicationService", "service initiated");
	}

	public void addBluetoothCommunicationListener(BluetoothCommunicationListener listener) {
		Log.d("BluetoothCommunicationService", "Adding BluetoothCommunicationListener");
		listeners.add(listener);
	}

	public void removeBluetoothCommunicationListener(BluetoothCommunicationListener listener) {
		Log.d("BluetoothCommunicationService", "Removing BluetoothCommunicationListener");
		listeners.remove(listener);
	}

	public void sendMessage(BluetoothMessage message) {
		switch (message.getFlag()) {

			case ROTATION_FLAG:
				sendRotationMessage((RotationMessage) message);
				break;
			case THRUST_FLAG:
				sendThrustMessage((ThrustMessage) message);
				break;
			case FIRE_FLAG:
				sendFireMessage((FireMessage) message);
				break;
			case SYNC_ROTATION_FLAG:
				sendSyncRotationMessage((SyncRotationMessage) message);
				break;
			case SYNC_VELOCITY_FLAG:
				sendSyncVelocityMessage((SyncVelocityMessage) message);
				break;
			case SYNC_POSITION_FLAG:
				sendSyncPositionMessage((SyncPositionMessage) message);
				break;
			case PLAYER_HIT_FLAG:
				sendPlayerHitMessage((PlayerHitMessage) message);
				break;
			case OPPONENT_HIT_FLAG:
				sendOpponentHitMessage((OpponentHitMessage) message);
				break;
			default:
				Log.w("BluetoothCommunicationService", "Unknown message type");
				break;
		}
	}

	private void sendRotationMessage(RotationMessage message) {
		communicationThread.writeByte(message.getFlag());
		communicationThread.writeByte(message.getPayload());

	}

	private void sendThrustMessage(ThrustMessage message) {
		communicationThread.writeByte(message.getFlag());

	}

	private void sendFireMessage(FireMessage message) {
		communicationThread.writeByte(message.getFlag());

	}

	private void sendSyncRotationMessage(SyncRotationMessage message) {
		communicationThread.writeByte(message.getFlag());
		communicationThread.writeFloat(message.getPayload());

	}

	private void sendSyncVelocityMessage(SyncVelocityMessage message) {
		communicationThread.writeByte(message.getFlag());
		communicationThread.writeFloatArray(message.getPayload());

	}

	private void sendSyncPositionMessage(SyncPositionMessage message) {
		communicationThread.writeByte(message.getFlag());
		communicationThread.writeFloatArray(message.getPayload());

	}

	private void sendPlayerHitMessage(PlayerHitMessage message) {
		communicationThread.writeByte(message.getFlag());

	}

	private void sendOpponentHitMessage(OpponentHitMessage message) {
		communicationThread.writeByte(message.getFlag());

	}

	private void updateListernes(BluetoothMessage message) {
		for (BluetoothCommunicationListener listener : listeners) {
			listener.onDataRecived(message);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("BluetoothCommunicationService", "Destroying service");

		wifi.setWifiEnabled(originalWifiState);
		Log.d("BluetoothCommunicationService", "Wifi restored to original state");

		if (communicationThread != null) {
			communicationThread.cancel();
			try {
				communicationThread.join();
			} catch (InterruptedException e) {
				Log.e("BluetoothCommunicationService", "Can't stop BluetoothCommunicationThread");
			}
		}
	}

	private class BluetoothCommunicationThread extends Thread {

		private final BluetoothSocket socket;
		private final DataInputStream inputStream;
		private final DataOutputStream outputStream;

		public BluetoothCommunicationThread(BluetoothSocket socket) {
			Log.d("BluetoothCommunicationThread", "Creating BluetoothCommunicationThread");

			this.socket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't get streams from socket");
			}

			inputStream = new DataInputStream(tmpIn);
			outputStream = new DataOutputStream(tmpOut);
		}

		public void run() {

			while (true) {

				byte flag = 0x0;
				try {
					flag = inputStream.readByte();
				} catch (IOException e) {
					break;
				}

				switch (flag) {

					case ROTATION_FLAG:
						updateListernes(reciveRotationMessage());
						break;
					case THRUST_FLAG:
						updateListernes(reciveThrustMessage());
						break;
					case FIRE_FLAG:
						updateListernes(reciveFireMessage());
						break;
					case SYNC_ROTATION_FLAG:
						updateListernes(reciveSyncRotationMessage());
						break;
					case SYNC_VELOCITY_FLAG:
						updateListernes(reciveSyncVelocityMessage());
						break;
					case SYNC_POSITION_FLAG:
						updateListernes(reciveSyncPositionMessage());
						break;
					case PLAYER_HIT_FLAG:
						updateListernes(recivePlayerHitMessage());
						break;
					case OPPONENT_HIT_FLAG:
						updateListernes(reciveOpponentHitMessage());
						break;
					default:
						Log.e("BluetoothCommunicationThread", "Unknown flag type");
						break;
				}
			}

		}

		private BluetoothMessage reciveRotationMessage() {

			byte rotation = 0x0;
			try {
				rotation = inputStream.readByte();
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Failed reading rotation (byte)");
			}

			return new RotationMessage(rotation);
		}

		private BluetoothMessage reciveThrustMessage() {

			return new ThrustMessage();
		}

		private BluetoothMessage reciveFireMessage() {

			return new FireMessage();
		}

		private BluetoothMessage reciveSyncRotationMessage() {
			float rotation = 0.0f;
			try {
				rotation = inputStream.readFloat();
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Failed reading rotation (float)");
			}
			return new SyncRotationMessage(rotation);
		}

		private BluetoothMessage reciveSyncVelocityMessage() {
			float[] velocity = new float[2];
			try {
				for (int i = 0; i < velocity.length; i++) {
					velocity[i] = inputStream.readFloat();
				}
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Failed reading velocity (float[])");
			}
			return new SyncVelocityMessage(velocity);
		}

		private BluetoothMessage reciveSyncPositionMessage() {
			float[] position = new float[2];
			try {
				for (int i = 0; i < position.length; i++) {
					position[i] = inputStream.readFloat();
				}
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Failed reading position (float[])");
			}
			return new SyncPositionMessage(position);
		}

		private BluetoothMessage recivePlayerHitMessage() {

			return new PlayerHitMessage();
		}

		private BluetoothMessage reciveOpponentHitMessage() {

			return new OpponentHitMessage();
		}

		private void writeByte(byte data) {
			try {
				outputStream.writeByte(data);
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't write to stream");
			}
		}

		private void writeFloat(float data) {
			try {
				outputStream.writeFloat(data);
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't write to stream");
			}
		}

		private void writeFloatArray(float[] data) {
			try {
				for (int i = 0; i < data.length; i++) {
					outputStream.writeFloat(data[i]);
				}
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't write to stream");
			}
		}

		private void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't close stream");
			}
		}

	}

	public class LocalBinder extends Binder {
		public BluetoothCommunicationService getService() {
			return BluetoothCommunicationService.this;
		}
	}

}
