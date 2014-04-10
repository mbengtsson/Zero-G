package se.bengtsson.zerog.activities.game;

import org.andengine.entity.scene.Scene;

import se.bengtsson.zerog.bluetooth.BluetoothCommunicationListener;
import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService;
import se.bengtsson.zerog.bluetooth.BluetoothCommunicationService.LocalBinder;
import se.bengtsson.zerog.bluetooth.message.BluetoothMessage;
import se.bengtsson.zerog.bluetooth.message.FireMessage;
import se.bengtsson.zerog.bluetooth.message.OpponentHitMessage;
import se.bengtsson.zerog.bluetooth.message.PlayerHitMessage;
import se.bengtsson.zerog.bluetooth.message.RotationMessage;
import se.bengtsson.zerog.bluetooth.message.SyncPositionMessage;
import se.bengtsson.zerog.bluetooth.message.SyncRotationMessage;
import se.bengtsson.zerog.bluetooth.message.SyncVelocityMessage;
import se.bengtsson.zerog.bluetooth.message.ThrustMessage;
import se.bengtsson.zerog.game.objects.fighter.Fighter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class MultiplayerGameActivity extends GameActivity {

	private BluetoothCommunicationService communicationService;

	private int syncTimer = 0;

	private boolean server;

	private boolean playerHit = false;
	private boolean opponentHit = false;
	private boolean lastThrustState = false;;
	private boolean lastFireState = false;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

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
		Log.d("MultiPlayerGameActivity", "Creating activity");
		server = getIntent().getBooleanExtra("isServer", false);
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
		sceneManager.setupMultiPlayerScene(server);
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		sendBluetoothMessages();

		if (!gameOver) {
			checkGameOver(true);
		}

		super.onUpdate(pSecondsElapsed);
	}

	private void sendBluetoothMessages() {
		if (playerHit) {
			communicationService.sendMessage(new PlayerHitMessage());
			playerHit = false;
		}

		if (opponentHit) {
			communicationService.sendMessage(new OpponentHitMessage());
			opponentHit = false;
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
	protected void fighterHit(Fighter fighter) {

		if (server) {
			fighter.hit();
			if (fighter.isEnemy()) {
				opponentHit = true;
				hud.setEnemyHealth(fighter.getHealth());
			} else {
				playerHit = true;
				hud.setPlayerHealth(fighter.getHealth());
			}
		}
	}

	private class CommunicationListener implements BluetoothCommunicationListener {

		@Override
		public void onDataRecived(BluetoothMessage message) {

			if (message instanceof PlayerHitMessage) {
				sceneManager.getEnemyFighter().hit();
				hud.setEnemyHealth(sceneManager.getEnemyFighter().getHealth());
			}

			if (message instanceof OpponentHitMessage) {
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