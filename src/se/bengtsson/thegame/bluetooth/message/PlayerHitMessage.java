package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class PlayerHitMessage extends BluetoothMessage {

	public PlayerHitMessage() {
		setFlag(BluetoothCommunicationService.PLAYER_HIT_FLAG);
	}
}
