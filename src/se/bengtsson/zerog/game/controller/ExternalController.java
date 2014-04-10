package se.bengtsson.zerog.game.controller;

import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class ExternalController implements Controller {

	private boolean leftTriggerPressed;
	private boolean rightTriggerPressed;
	private byte tilt;

	public ExternalController() {
		Log.d("ExternalController", "Creating ExternalController");
	}

	@Override
	public boolean isLeftTriggerPressed() {
		return leftTriggerPressed;
	}

	@Override
	public boolean isRightTriggerPressed() {
		return rightTriggerPressed;
	}

	@Override
	public byte getTilt() {
		return tilt;
	}

	public void setLeftTriggerPressed(boolean leftTriggerPressed) {
		this.leftTriggerPressed = leftTriggerPressed;
	}

	public void setRightTriggerPressed(boolean rightTriggerPressed) {
		this.rightTriggerPressed = rightTriggerPressed;
	}

	public void setTilt(byte tilt) {
		this.tilt = tilt;
	}

}
