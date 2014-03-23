package se.bengtsson.thegame.game.controller;

public class ExternalController implements Controller {

	private boolean leftTriggerPressed;
	private boolean rightTriggerPressed;
	private byte tilt;

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
