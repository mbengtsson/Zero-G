package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class PlayerHitMessage extends BluetoothMessage {

	public PlayerHitMessage() {
		setFlag(BluetoothCommunicationService.PLAYER_HIT_FLAG);
	}
}
