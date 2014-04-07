package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class FireMessage extends BluetoothMessage {

	public FireMessage() {
		setFlag(BluetoothCommunicationService.FIRE_FLAG);
	}
}
