package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class ThrustMessage extends BluetoothMessage {

	public ThrustMessage() {
		setFlag(BluetoothCommunicationService.THRUST_FLAG);
	}
}
