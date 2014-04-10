package se.bengtsson.zerog.bluetooth.message;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public abstract class BluetoothMessage {

	protected byte flag;

	public byte getFlag() {
		return flag;
	}

	protected void setFlag(byte flag) {
		this.flag = flag;
	}
}
