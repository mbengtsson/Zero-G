package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class OpponentHitMessage extends BluetoothMessage {

	public OpponentHitMessage() {
		setFlag(BluetoothCommunicationService.OPPONENT_HIT_FLAG);
	}
}
