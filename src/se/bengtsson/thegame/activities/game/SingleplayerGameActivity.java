package se.bengtsson.thegame.activities.game;

import org.andengine.entity.scene.Scene;

import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class SingleplayerGameActivity extends GameActivity {

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		sceneManager.setupSingleplayerScene(playerController, externalController);
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		updateAi();

		if (!gameOver) {
			checkGameOver(sceneManager.getPlayerFighter(), sceneManager.getEnemyFighter(), false);

		}

		super.onUpdate(pSecondsElapsed);
	}

	private void updateAi() {
		float playerX = sceneManager.getPlayerFighter().getXpos();
		float playerY = sceneManager.getPlayerFighter().getYpos();
		float enemyX = sceneManager.getEnemyFighter().getXpos();
		float enemyY = sceneManager.getEnemyFighter().getYpos();
		float velocityX = sceneManager.getEnemyFighter().getVelocityX();
		float velocityY = sceneManager.getEnemyFighter().getVelocityY();

		float xDiff = playerX - enemyX;
		float yDiff = playerY - enemyY;

		double rotation = (sceneManager.getEnemyFighter().getRotation() % (Math.PI * 2));

		double angle = (Math.atan2(yDiff, xDiff) - (rotation) + (Math.PI / 2)) % (Math.PI * 2);
		if (angle > Math.PI) {
			angle = -((Math.PI * 2) - angle);
		} else if (angle < -Math.PI) {
			angle = (Math.PI * 2) + angle;
		}

		double distance =
				Math.abs(Math.sqrt(((playerX - enemyX) * (playerX - enemyX))
						+ ((playerY - enemyY) * (playerY - enemyY))));

		double velocity = Math.abs(Math.sqrt(((velocityX) * (velocityX)) + ((velocityY) * (velocityY))));
		double heading = (Math.atan2(velocityY, velocityX) + (Math.PI * 1.5)) % (Math.PI * 2);
		if (heading > Math.PI) {
			heading = -((Math.PI * 2) - heading);
		} else if (angle < -Math.PI) {
			heading = (Math.PI * 2) + heading;
		}

		if (velocity < 8) {
			externalController.setTilt((byte) (angle > 0 ? (angle * 10) + 2 : (angle * 10) - 2));

			if ((angle < 0.7 && angle > 0.2 || angle > -0.7 && angle < -0.2) && distance > 6) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		} else {
			externalController.setTilt((byte) (heading > 0 ? (heading * 12) + 2 : (heading * 12) - 2));

			if ((angle - heading < 0.7 && angle - heading > -0.7)) {
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}
		}

		if (angle < 0.1 && angle > -0.1) {
			externalController.setLeftTriggerPressed(true);
		} else {
			externalController.setLeftTriggerPressed(false);
		}
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
}
