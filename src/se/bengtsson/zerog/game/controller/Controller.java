package se.bengtsson.zerog.game.controller;

public interface Controller {

	public boolean isLeftTriggerPressed();

	public boolean isRightTriggerPressed();

	public byte getTilt();
}