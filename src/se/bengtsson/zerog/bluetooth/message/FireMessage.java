package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class FireMessage extends BluetoothMessage {

	public FireMessage() {
		setFlag(BluetoothCommunicationService.FIRE_FLAG);
	}
}
