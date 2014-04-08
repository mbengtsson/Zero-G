package se.bengtsson.thegame.activities.game;

import org.andengine.entity.scene.Scene;

import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;
import android.os.Bundle;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class SingleplayerGameActivity extends GameActivity {

	private Ai ai;

	private int tick = 0;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		ai = new Ai();
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		sceneManager.setupSingleplayerScene(playerController, externalController);
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		tick++;

		if (tick > 90) {
			ai.updateAi();
		}

		if (!gameOver) {
			checkGameOver(sceneManager.getPlayerFighter(), sceneManager.getEnemyFighter(), false);

		}

		super.onUpdate(pSecondsElapsed);
	}

	@Override
	protected ContactListener createContactListener() {
		ContactListener contactListener = new ContactListener() {
			@Override
			public void beginContact(Contact contact) {

				final Fixture fixtureA = contact.getFixtureA();
				final Fixture fixtureB = contact.getFixtureB();

				if (fixtureA.getBody().getUserData() instanceof Bullet) {
					Bullet bullet = (Bullet) fixtureA.getBody().getUserData();
					sceneManager.getBulletPool().recyclePoolItem(bullet);
					if (fixtureB.getBody().getUserData() instanceof Fighter) {
						Fighter fighter = (Fighter) fixtureB.getBody().getUserData();
						fighter.hit();
						if (fighter.isEnemy()) {

							hud.setEnemyHealth(fighter.getHealth());
						} else {

							hud.setPlayerHealth(fighter.getHealth());
						}

					}

				}
				if (fixtureB.getBody().getUserData() instanceof Bullet) {
					Bullet bullet = (Bullet) fixtureB.getBody().getUserData();
					sceneManager.getBulletPool().recyclePoolItem(bullet);
					if (fixtureA.getBody().getUserData() instanceof Fighter) {
						Fighter fighter = (Fighter) fixtureA.getBody().getUserData();
						fighter.hit();
						if (fighter.isEnemy()) {

							hud.setEnemyHealth(fighter.getHealth());
						} else {

							hud.setPlayerHealth(fighter.getHealth());
						}

					}
				}

			}

			@Override
			public void endContact(Contact contact) {

			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		};
		return contactListener;
	}

	private class Ai {

		private float breakingVelocity = 12;

		private float playerX;
		private float playerY;
		private float enemyX;
		private float enemyY;
		private float velocityX;
		private float velocityY;

		private double rotation;
		private double angleToTarget;
		private double distanceToTarget;
		private double velocity;
		private double breakHeading;

		public void updateAi() {

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
			breakingVelocity = 12;

			externalController
					.setTilt((byte) (angleToTarget > 0 ? (angleToTarget * 10) + 2 : (angleToTarget * 10) - 2));

			if ((angleToTarget < 0.7 && angleToTarget > 0.2 || angleToTarget > -0.7 && angleToTarget < -0.2)
					&& distanceToTarget > 6) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		}

		private void turnAndBreak() {
			breakingVelocity = 8;

			externalController.setTilt((byte) (breakHeading > 0 ? (breakHeading * 12) + 2 : (breakHeading * 12) - 2));

			if (breakHeading < 0.7 && breakHeading > -0.7) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		}

		private void fireIfInRange() {
			if (angleToTarget < 0.1 && angleToTarget > -0.1) {
				externalController.setLeftTriggerPressed(true);
			} else {
				externalController.setLeftTriggerPressed(false);
			}
		}

	}
}
