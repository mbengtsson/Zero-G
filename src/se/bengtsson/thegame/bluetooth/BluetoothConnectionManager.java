package se.bengtsson.thegame.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothConnectionManager {

	private static BluetoothConnectionManager INSTANCE;

	private ConnectedThread connectedThread;

	private BluetoothConnectionManager() {

	}

	public static BluetoothConnectionManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BluetoothConnectionManager();
		}
		return INSTANCE;
	}

	public void initiate(BluetoothSocket socket) {
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		Log.d("BluetoothConnectionManager", "manager initiated");
	}

	public void writeToSocket(byte aByte) {
		connectedThread.writeByte(aByte);
	}

	public Byte readFromSocket() {
		return connectedThread.readByte();
	}

	public void destroy() {
		connectedThread.cancel();
		try {
			connectedThread.join();
		} catch (InterruptedException e) {
			Log.e("BluetoothConnectionManager", "Can't stop thread");
		}
	}

	private class ConnectedThread extends Thread {

		private final BluetoothSocket socket;
		private final InputStream inputStream;
		private final OutputStream outputStream;
		private ConcurrentLinkedQueue<Byte> buffer = new ConcurrentLinkedQueue<Byte>();

		public ConnectedThread(BluetoothSocket socket) {
			this.socket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("BluetoothConnectionManager", "Can't get streams from socket");
			}

			inputStream = tmpIn;
			outputStream = tmpOut;
		}

		public void run() {

			while (true) {
				try {
					buffer.add((byte) inputStream.read());
				} catch (IOException e) {
					break;
				}
			}
		}

		public void writeByte(byte aByte) {
			try {
				outputStream.write(aByte);
			} catch (IOException e) {
				Log.e("BluetoothConnectionManager", "Can't write to stream");
			}
		}

		public Byte readByte() {

			return buffer.poll();
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e("BluetoothConnectionManager", "Can't close stream");
			}
		}

	}

}
