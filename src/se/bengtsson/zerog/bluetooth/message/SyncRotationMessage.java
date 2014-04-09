package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

public class SyncRotationMessage extends BluetoothMessage {

	private float payload;

	public SyncRotationMessage() {
		setFlag(BluetoothCommunicationService.SYNC_ROTATION_FLAG);
	}

	public SyncRotationMessage(float payload) {
		setFlag(BluetoothCommunicationService.SYNC_ROTATION_FLAG);
		setPayload(payload);
	}

	public float getPayload() {
		return payload;
	}

	public void setPayload(float payload) {
		this.payload = payload;
	}
}
