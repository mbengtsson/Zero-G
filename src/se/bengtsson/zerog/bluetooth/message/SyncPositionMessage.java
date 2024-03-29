package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class SyncPositionMessage extends BluetoothMessage {

	private float[] payload;

	public SyncPositionMessage(float[] payload) {
		setFlag(BluetoothCommunicationService.SYNC_POSITION_FLAG);
		setPayload(payload);
	}

	public float[] getPayload() {
		return payload;
	}

	private void setPayload(float[] payload) {
		this.payload = payload;
	}
}
