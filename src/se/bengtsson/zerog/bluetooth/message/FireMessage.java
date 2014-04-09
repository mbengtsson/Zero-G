package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

public class FireMessage extends BluetoothMessage {

	public FireMessage() {
		setFlag(BluetoothCommunicationService.FIRE_FLAG);
	}
}
