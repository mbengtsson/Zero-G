package se.bengtsson.thegame.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothCommunicationService extends Service {

	private final IBinder binder = new LocalBinder();

	private BluetoothCommunicationThread communicationThread;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void initiate(BluetoothSocket socket) {
		communicationThread = new BluetoothCommunicationThread(socket);
		communicationThread.start();
		Log.d("BluetoothCommunicationService", "service initiated");
	}

	public void writeToSocket(byte aByte) {
		communicationThread.writeByte(aByte);
	}

	public Byte readFromSocket() {
		return communicationThread.readByte();
	}

	public Byte nextFromSocket() {
		return communicationThread.nextByte();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		communicationThread.cancel();
		try {
			communicationThread.join();
		} catch (InterruptedException e) {
			Log.e("BluetoothCommunicationService", "Can't stop BluetoothCommunicationThread");
		}
	}

	private class BluetoothCommunicationThread extends Thread {

		private final BluetoothSocket socket;
		private final InputStream inputStream;
		private final OutputStream outputStream;

		private ConcurrentLinkedQueue<Byte> buffer = new ConcurrentLinkedQueue<Byte>();

		public BluetoothCommunicationThread(BluetoothSocket socket) {
			this.socket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't get streams from socket");
			}

			inputStream = tmpIn;
			outputStream = tmpOut;
		}

		public void run() {

			while (true) {
				try {
					buffer.offer((byte) inputStream.read());
				} catch (IOException e) {
					break;
				}
			}
		}

		public void writeByte(byte aByte) {
			try {
				outputStream.write(aByte);
			} catch (IOException e) {
				Log.e("BluetoothCommunicationThread", "Can't write to stream");
			}
		}

		public Byte readByte() {
			return buffer.poll();
		}

		public Byte nextByte() {
			return buffer.peek();
		}

		public void cancel() {
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
