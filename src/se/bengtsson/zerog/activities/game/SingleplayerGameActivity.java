package se.bengtsson.zerog.activities.game;

import org.andengine.entity.scene.Scene;

import se.bengtsson.zerog.game.objects.fighter.Fighter;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class SingleplayerGameActivity extends GameActivity {

	private Ai ai;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		Log.d("SinglePlayerGameActivity", "Creating activity");
		super.onCreate(pSavedInstanceState);
		ai = new Ai();
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		sceneManager.setupSingleplayerScene();
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		ai.updateAi();

		if (!gameOver) {
			checkGameOver(false);
		}

		super.onUpdate(pSecondsElapsed);
	}

	@Override
	protected void fighterHit(Fighter fighter) {
		fighter.hit();

		if (fighter.isEnemy()) {
			hud.setEnemyHealth(fighter.getHealth());

		} else {
			hud.setPlayerHealth(fighter.getHealth());

		}

	}

	private class Ai {

		private final int DELAYED_START = 90;

		private final int BASE_ROTATION = 2;
		private final int ROTATION_MODIFIER = 10;
		private final int SPEED_LIMIT_HIGH = 12;
		private final int SPEED_LIMIT_LOW = 8;
		private final int MIN_THRUST_DISTANCE = 5;

		private int tick;

		private float playerX;
		private float playerY;
		private float enemyX;
		private float enemyY;
		private float velocityX;
		private float velocityY;

		private float breakingVelocity;
		private double rotation;
		private double angleToTarget;
		private double distanceToTarget;
		private double velocity;
		private double breakHeading;

		public Ai() {
			tick = 0;
			breakingVelocity = SPEED_LIMIT_HIGH;
		}

		private void updateAi() {

			tick++;
			if (tick < DELAYED_START) {
				return;
			}

			playerX = sceneManager.getPlayerFighter().getXpos();
			playerY = sceneManager.getPlayerFighter().getYpos();
			enemyX = sceneManager.getEnemyFighter().getXpos();
			enemyY = sceneManager.getEnemyFighter().getYpos();
			velocityX = sceneManager.getEnemyFighter().getVelocityX();
			velocityY = sceneManager.getEnemyFighter().getVelocityY();

			calculateRotation();
			calculateAngleToTarget();
			calculateVelocity();

			if (velocity < breakingVelocity) {
				calculateDistanceToTarget();
				accelerateToTarget();
			} else {
				calculateBreakeHeading();
				turnAndBreak();
			}

			fireIfInRange();
		}

		private void calculateRotation() {
			rotation = (sceneManager.getEnemyFighter().getRotation() % (Math.PI * 2));
			if (rotation > Math.PI) {
				rotation = -((Math.PI * 2) - rotation);
			} else if (rotation < -Math.PI) {
				rotation = (Math.PI * 2) + rotation;
			}
		}

		private void calculateAngleToTarget() {

			float xDiff = playerX - enemyX;
			float yDiff = playerY - enemyY;

			angleToTarget = (Math.atan2(yDiff, xDiff) - (rotation) + (Math.PI / 2)) % (Math.PI * 2);
			if (angleToTarget > Math.PI) {
				angleToTarget = -((Math.PI * 2) - angleToTarget);
			} else if (angleToTarget < -Math.PI) {
				angleToTarget = (Math.PI * 2) + angleToTarget;
			}
		}

		private void calculateDistanceToTarget() {
			distanceToTarget =
					Math.abs(Math.sqrt(((playerX - enemyX) * (playerX - enemyX))
							+ ((playerY - enemyY) * (playerY - enemyY))));
		}

		private void calculateVelocity() {
			velocity = Math.abs(Math.sqrt(((velocityX) * (velocityX)) + ((velocityY) * (velocityY))));
		}

		private void calculateBreakeHeading() {
			breakHeading = (Math.atan2(velocityY, velocityX) - (rotation) + (Math.PI * 1.5)) % (Math.PI * 2);
			if (breakHeading > Math.PI) {
				breakHeading = -((Math.PI * 2) - breakHeading);
			} else if (angleToTarget < -Math.PI) {
				breakHeading = (Math.PI * 2) + breakHeading;
			}
		}

		private void accelerateToTarget() {
			breakingVelocity = SPEED_LIMIT_HIGH;

			externalController.setTilt((byte) (angleToTarget > 0 ? (angleToTarget * ROTATION_MODIFIER) + BASE_ROTATION
					: (angleToTarget * ROTATION_MODIFIER) - BASE_ROTATION));

			if ((angleToTarget < 0.7 && angleToTarget > 0.2 || angleToTarget > -0.7 && angleToTarget < -0.2)
					&& distanceToTarget > MIN_THRUST_DISTANCE) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		}

		private void turnAndBreak() {
			breakingVelocity = SPEED_LIMIT_LOW;

			externalController.setTilt((byte) (breakHeading > 0 ? (breakHeading * ROTATION_MODIFIER) + BASE_ROTATION
					: (breakHeading * ROTATION_MODIFIER) - BASE_ROTATION));

			if (breakHeading < 0.7 && breakHeading > -0.7) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		}

		private void fireIfInRange() {
			if (angleToTarget < 0.1 && angleToTarget > -0.1 && sceneManager.getPlayerFighter().isAlive()) {
				externalController.setLeftTriggerPressed(true);
			} else {
				externalController.setLeftTriggerPressed(false);
			}
		}

	}
}
