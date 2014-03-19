package se.bengtsson.thegame.game.controller;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import se.bengtsson.thegame.game.manager.ResourceManager;
import android.util.Log;

public class PlayerController implements Controller, IAccelerationListener {

	private Trigger leftTrigger;
	private Trigger rightTrigger;

	private float tilt = 0.0f;

	public PlayerController(Scene scene) {
		ResourceManager resources = ResourceManager.getInstance();

		float triggerSize = resources.triggerTextureRegion.getWidth();

		this.leftTrigger =
				new Trigger(0, resources.camera.getHeight() - triggerSize, resources.triggerTextureRegion,
						resources.vbom);

		this.rightTrigger =
				new Trigger(resources.camera.getWidth() - triggerSize, resources.camera.getHeight() - triggerSize,
						resources.triggerTextureRegion, resources.vbom);

		scene.attachChild(leftTrigger);
		scene.attachChild(rightTrigger);

		scene.registerTouchArea(leftTrigger);
		scene.registerTouchArea(rightTrigger);

		scene.setTouchAreaBindingOnActionDownEnabled(true);

	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		tilt = pAccelerationData.getX();

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
	public float getTilt() {
		return tilt;
	}

	private class Trigger extends Sprite {

		public Trigger(final float pX, final float pY, final ITextureRegion pTextureRegion,
				final VertexBufferObjectManager pVertexBufferObjectManager) {

			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);

		}

		private boolean isDown = false;

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
