package se.bengtsson.zerog.game.controller;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import se.bengtsson.zerog.game.manager.ResourceManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class PlayerController implements Controller, SensorEventListener {

	private Trigger leftTrigger;
	private Trigger rightTrigger;

	private byte tilt = 0;

	public PlayerController() {
		ResourceManager resources = ResourceManager.getInstance();

		float triggerSize = resources.thrustTriggerTextureRegion.getWidth();

		this.leftTrigger =
				new Trigger(0, resources.camera.getHeight() - triggerSize, resources.fireTriggerTextureRegion,
						resources.vbom);

		this.rightTrigger =
				new Trigger(resources.camera.getWidth() - triggerSize, resources.camera.getHeight() - triggerSize,
						resources.thrustTriggerTextureRegion, resources.vbom);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		tilt = (byte) (event.values[1] * 10);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public boolean isLeftTriggerPressed() {
		return leftTrigger.isDown;
	}

	@Override
	public boolean isRightTriggerPressed() {
		return rightTrigger.isDown;
	}

	@Override
	public byte getTilt() {
		return tilt;
	}

	public Trigger getLeftTrigger() {
		return leftTrigger;
	}

	public Trigger getRightTrigger() {
		return rightTrigger;
	}

	private class Trigger extends Sprite {

		private boolean isDown = false;

		public Trigger(final float pX, final float pY, final ITextureRegion pTextureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager) {

			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);

		}

		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,
				final float pTouchAreaLocalY) {

			if (pSceneTouchEvent.isActionDown()) {
				Log.d("Trigger", "pressed");
				isDown = true;

			}
			if (pSceneTouchEvent.isActionUp()) {
				Log.d("Trigger", "released");
				isDown = false;
			}

			return true;
		}

	}

}
