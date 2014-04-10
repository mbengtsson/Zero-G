package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class ThrustMessage extends BluetoothMessage {

	public ThrustMessage() {
		setFlag(BluetoothCommunicationService.THRUST_FLAG);
	}
}
