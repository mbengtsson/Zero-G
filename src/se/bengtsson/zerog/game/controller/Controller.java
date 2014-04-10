package se.bengtsson.zerog.game.controller;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public interface Controller {

	public boolean isLeftTriggerPressed();

	public boolean isRightTriggerPressed();

	public byte getTilt();
}
