package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class OpponentHitMessage extends BluetoothMessage {

	public OpponentHitMessage() {
		setFlag(BluetoothCommunicationService.OPPONENT_HIT_FLAG);
	}
}
