package se.bengtsson.thegame.bluetooth;

import se.bengtsson.thegame.bluetooth.message.BluetoothMessage;

public interface BluetoothCommunicationListener {

	public void onDataRecived(BluetoothMessage message);
}
