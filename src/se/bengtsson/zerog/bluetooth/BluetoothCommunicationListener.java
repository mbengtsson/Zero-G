package se.bengtsson.zerog.bluetooth;

import se.bengtsson.zerog.bluetooth.message.BluetoothMessage;

public interface BluetoothCommunicationListener {

	public void onDataRecived(BluetoothMessage message);
}
