package se.bengtsson.zerog.bluetooth.message;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class RotationMessage extends BluetoothMessage {

	private byte payload;

	public RotationMessage(byte payload) {
		setFlag(BluetoothCommunicationService.ROTATION_FLAG);
		setPayload(payload);
	}

	public byte getPayload() {
		return payload;
	}

	private void setPayload(byte payload) {
		this.payload = payload;
	}
}
