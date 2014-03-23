package se.bengtsson.thegame.game.controller;

public class ExternalController implements Controller {

	private boolean leftTriggerPressed;
	private boolean rightTriggerPressed;
	private float tilt;

	@Override
	public boolean isLeftTriggerPressed() {
		return leftTriggerPressed;
	}

	@Override
	public boolean isRightTriggerPressed() {
		return rightTriggerPressed;
	}

	@Override
	public float getTilt() {
		return tilt;
	}

	public void setLeftTriggerPressed(boolean leftTriggerPressed) {
		this.leftTriggerPressed = leftTriggerPressed;
	}

	public void setRightTriggerPressed(boolean rightTriggerPressed) {
		this.rightTriggerPressed = rightTriggerPressed;
	}

	public void setTilt(float tilt) {
		this.tilt = tilt;
	}

}
