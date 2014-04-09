package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

public class SyncVelocityMessage extends BluetoothMessage {

	private float[] payload;

	public SyncVelocityMessage() {
		setFlag(BluetoothCommunicationService.SYNC_VELOCITY_FLAG);
	}

	public SyncVelocityMessage(float[] payload) {
		setFlag(BluetoothCommunicationService.SYNC_VELOCITY_FLAG);
		setPayload(payload);
	}

	public float[] getPayload() {
		return payload;
	}

	public void setPayload(float[] payload) {
		this.payload = payload;
	}
}
