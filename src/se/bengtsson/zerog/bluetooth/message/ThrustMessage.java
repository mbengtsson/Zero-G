package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

public class ThrustMessage extends BluetoothMessage {

	public ThrustMessage() {
		setFlag(BluetoothCommunicationService.THRUST_FLAG);
	}
}
