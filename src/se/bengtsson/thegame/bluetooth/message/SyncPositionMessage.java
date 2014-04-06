package se.bengtsson.thegame.bluetooth.message;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;

public class SyncPositionMessage extends BluetoothMessage {

	private float[] payload;

	public SyncPositionMessage() {
		setFlag(BluetoothCommunicationService.SYNC_POSITION_FLAG);
	}

	public SyncPositionMessage(float[] payload) {
		setFlag(BluetoothCommunicationService.SYNC_POSITION_FLAG);
		setPayload(payload);
	}

	public float[] getPayload() {
		return payload;
	}

	public void setPayload(float[] payload) {
		this.payload = payload;
	}
}
