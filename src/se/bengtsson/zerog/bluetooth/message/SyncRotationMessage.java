package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class SyncRotationMessage extends BluetoothMessage {

	private float payload;

	public SyncRotationMessage(float payload) {
		setFlag(BluetoothCommunicationService.SYNC_ROTATION_FLAG);
		setPayload(payload);
	}

	public float getPayload() {
		return payload;
	}

	private void setPayload(float payload) {
		this.payload = payload;
	}
}
