package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class OponentHitMessage extends BluetoothMessage {

	public OponentHitMessage() {
		setFlag(BluetoothCommunicationService.OPONENT_HIT_FLAG);
	}
}
