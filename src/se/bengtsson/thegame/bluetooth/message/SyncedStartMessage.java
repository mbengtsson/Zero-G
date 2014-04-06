package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class SyncedStartMessage extends BluetoothMessage {
	public SyncedStartMessage() {
		setFlag(BluetoothCommunicationService.SYNCED_START_FLAG);
	}
}
