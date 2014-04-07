package se.bengtsson.thegame.activities.game;

import org.andengine.entity.scene.Scene;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationListener;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService.LocalBinder;
import se.bengtsson.thegame.bluetooth.message.BluetoothMessage;
import se.bengtsson.thegame.bluetooth.message.FireMessage;
import se.bengtsson.thegame.bluetooth.message.OponentHitMessage;
import se.bengtsson.thegame.bluetooth.message.PlayerHitMessage;
import se.bengtsson.thegame.bluetooth.message.RotationMessage;
import se.bengtsson.thegame.bluetooth.message.SyncPositionMessage;
import se.bengtsson.thegame.bluetooth.message.SyncRotationMessage;
import se.bengtsson.thegame.bluetooth.message.SyncVelocityMessage;
import se.bengtsson.thegame.bluetooth.message.ThrustMessage;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MultiplayerGameActivity extends GameActivity {

	private BluetoothCommunicationService communicationService;

	private int syncTimer = 0;

	private boolean server;

	private boolean playerHit = false;
	private boolean oponentHit = false;
	private boolean lastThrustState = false;;
	private boolean lastFireState = false;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			communicationService = binder.getService();
			communicationService.addBluetoothCommunicationListener(new CommunicationListener());

		}
	};

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		server = getIntent().getBooleanExtra("isServer", false);
		// handler = new Handler();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, BluetoothCommunicationService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(serviceConnection);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		sceneManager.setupMultiplayerScene(playerController, externalController, server);
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		sendBluetoothMessages();

		if (!gameOver) {
			checkGameOver(sceneManager.getPlayerFighter(), sceneManager.getEnemyFighter(), true);
		}

		super.onUpdate(pSecondsElapsed);
	}

	private void sendBluetoothMessages() {
		if (playerHit) {
			communicationService.sendMessage(new PlayerHitMessage());
			playerHit = false;
		}

		if (oponentHit) {
			communicationService.sendMessage(new OponentHitMessage());
			oponentHit = false;
		}

		if (playerController.isRightTriggerPressed() != lastThrustState) {
			communicationService.sendMessage(new ThrustMessage());
			lastThrustState = playerController.isRightTriggerPressed();
		}

		if (playerController.isLeftTriggerPressed() != lastFireState) {
			communicationService.sendMessage(new FireMessage());
			lastFireState = playerController.isLeftTriggerPressed();
		}

		communicationService.sendMessage(new RotationMessage(playerController.getTilt()));

		syncTimer++;

		if (syncTimer == 10) {
			communicationService.sendMessage(new SyncRotationMessage(sceneManager.getPlayerFighter().getRotation()));

			float[] velocity = new float[2];
			velocity[0] = sceneManager.getPlayerFighter().getVelocityX();
			velocity[1] = sceneManager.getPlayerFighter().getVelocityY();
			communicationService.sendMessage(new SyncVelocityMessage(velocity));

			float[] position = new float[2];
			position[0] = sceneManager.getPlayerFighter().getXpos();
			position[1] = sceneManager.getPlayerFighter().getYpos();
			communicationService.sendMessage(new SyncPositionMessage(position));

			syncTimer = 0;
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
					if (fixtureB.getBody().getUserData() instanceof Fighter && server) {
						Fighter fighter = (Fighter) fixtureB.getBody().getUserData();
						fighter.hit();
						if (fighter.isEnemy()) {
							oponentHit = true;
							hud.setEnemyHealth(fighter.getHealth());
						} else {
							playerHit = true;
							hud.setPlayerHealth(fighter.getHealth());
						}

					}

				}
				if (fixtureB.getBody().getUserData() instanceof Bullet) {
					Bullet bullet = (Bullet) fixtureB.getBody().getUserData();
					sceneManager.getBulletPool().recyclePoolItem(bullet);
					if (fixtureA.getBody().getUserData() instanceof Fighter && server) {
						Fighter fighter = (Fighter) fixtureA.getBody().getUserData();
						fighter.hit();
						if (fighter.isEnemy()) {
							oponentHit = true;
							hud.setEnemyHealth(fighter.getHealth());
						} else {
							playerHit = true;
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

	private class CommunicationListener implements BluetoothCommunicationListener {

		@Override
		public void onDataRecived(BluetoothMessage message) {

			if (message instanceof PlayerHitMessage) {
				sceneManager.getEnemyFighter().hit();
				hud.setEnemyHealth(sceneManager.getEnemyFighter().getHealth());
			}

			if (message instanceof OponentHitMessage) {
				sceneManager.getPlayerFighter().hit();
				hud.setPlayerHealth(sceneManager.getPlayerFighter().getHealth());
			}

			if (message instanceof RotationMessage) {
				RotationMessage rotationMessage = (RotationMessage) message;
				externalController.setTilt(rotationMessage.getPayload());
			}

			if (message instanceof ThrustMessage) {
				externalController.setRightTriggerPressed(!externalController.isRightTriggerPressed());
			}

			if (message instanceof FireMessage) {
				externalController.setLeftTriggerPressed(!externalController.isLeftTriggerPressed());
			}

			if (message instanceof SyncRotationMessage) {

				final SyncRotationMessage syncRotationMessage = (SyncRotationMessage) message;
				runOnUpdateThread(new Runnable() {

					@Override
					public void run() {
						sceneManager.getEnemyFighter().setRotation(syncRotationMessage.getPayload());

					}
				});

			}

			if (message instanceof SyncVelocityMessage) {

				SyncVelocityMessage syncVelocityMessage = (SyncVelocityMessage) message;
				final float[] velocity = syncVelocityMessage.getPayload();
				runOnUpdateThread(new Runnable() {

					@Override
					public void run() {
						sceneManager.getEnemyFighter().setVelocity(velocity[0], velocity[1]);

					}
				});

			}

			if (message instanceof SyncPositionMessage) {
				SyncPositionMessage syncPositionMessage = (SyncPositionMessage) message;
				final float[] position = syncPositionMessage.getPayload();
				runOnUpdateThread(new Runnable() {

					@Override
					public void run() {
						sceneManager.getEnemyFighter().setPosition(position[0], position[1]);

					}
				});

			}

		}
	}
}