package se.bengtsson.thegame.activities.game;

import java.nio.ByteBuffer;

import org.andengine.entity.scene.Scene;

import se.bengtsson.thegame.activities.StatisticsActivity;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService.LocalBinder;
import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MultiplayerGameActivity extends GameActivity {

	public static byte ROTATION_FLAG = 0x1;
	public static byte THRUST_FLAG = 0x2;
	public static byte FIRE_FLAG = 0x3;
	public static byte SYNC_ROTATION_FLAG = 0x4;
	public static byte SYNC_VELOCITY_FLAG = 0x5;
	public static byte SYNC_POSITION_FLAG = 0x6;
	public static byte PLAYER_HIT_FLAG = 0x7;
	public static byte OPONENT_HIT_FLAG = 0x8;

	Handler handler;

	private BluetoothCommunicationService communicationService;
	private ExternalController externalController;

	private double time;
	private boolean server;

	boolean gameOver = false;
	boolean winner = false;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			communicationService = binder.getService();

		}
	};

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		server = getIntent().getBooleanExtra("isServer", false);
		handler = new Handler();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, BluetoothCommunicationService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		time = System.currentTimeMillis();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(serviceConnection);
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		externalController = new ExternalController();
		super.onCreateScene(pOnCreateSceneCallback);

	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		sceneManager.setupMultiplayerScene(playerController, externalController, server);
		super.onPopulateScene(pScene, pOnPopulateSceneCallback);
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		communicationService.writeToSocket(ROTATION_FLAG);

		communicationService.writeToSocket(playerController.getTilt());

		if (playerController.isRightTriggerPressed()) {
			communicationService.writeToSocket(THRUST_FLAG);
		}

		if (playerController.isLeftTriggerPressed()) {
			communicationService.writeToSocket(FIRE_FLAG);
		}

		if ((System.currentTimeMillis() - time) > 3000) {
			sendSync();
		}

		if (!server && communicationService.nextFromSocket() != null
				&& communicationService.nextFromSocket() == OPONENT_HIT_FLAG) {
			communicationService.readFromSocket();
			Fighter fighter = sceneManager.getPlayerFighter();
			fighter.hit();
			hud.setPlayerHealth(fighter.getHealth());

		}

		if (!server && communicationService.nextFromSocket() != null
				&& communicationService.nextFromSocket() == PLAYER_HIT_FLAG) {
			communicationService.readFromSocket();
			Fighter fighter = sceneManager.getEnemyFighter();
			fighter.hit();
			hud.setEnemyHealth(fighter.getHealth());

		}

		if (communicationService.nextFromSocket() != null && communicationService.nextFromSocket() == ROTATION_FLAG) {
			communicationService.readFromSocket();
			Byte tilt = null;
			while (tilt == null) {
				tilt = communicationService.readFromSocket();
			}
			externalController.setTilt(tilt);
		}

		if (communicationService.nextFromSocket() != null && communicationService.nextFromSocket() == THRUST_FLAG) {
			communicationService.readFromSocket();
			externalController.setRightTriggerPressed(true);
		} else {
			externalController.setRightTriggerPressed(false);
		}

		if (communicationService.nextFromSocket() != null && communicationService.nextFromSocket() == FIRE_FLAG) {
			communicationService.readFromSocket();
			externalController.setLeftTriggerPressed(true);
		} else {
			externalController.setLeftTriggerPressed(false);
		}

		reciveSync();

		if (!gameOver) {
			checkGameOver(sceneManager.getPlayerFighter(), sceneManager.getEnemyFighter());
		}

		super.onUpdate(pSecondsElapsed);
	}

	public void sendSync() {
		time = System.currentTimeMillis();
		byte[] rotationBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getRotation()).array();
		communicationService.writeToSocket(SYNC_ROTATION_FLAG);
		for (int i = 0; i < rotationBytes.length; i++) {
			communicationService.writeToSocket(rotationBytes[i]);
		}

		byte[] xPosBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getXpos()).array();
		byte[] yPosBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getYpos()).array();
		communicationService.writeToSocket(SYNC_POSITION_FLAG);
		for (int i = 0; i < xPosBytes.length; i++) {
			communicationService.writeToSocket(xPosBytes[i]);
		}
		for (int i = 0; i < yPosBytes.length; i++) {
			communicationService.writeToSocket(yPosBytes[i]);
		}

		byte[] xVelBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getVelocityX()).array();
		byte[] yVelBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getVelocityY()).array();
		communicationService.writeToSocket(SYNC_VELOCITY_FLAG);
		for (int i = 0; i < xVelBytes.length; i++) {
			communicationService.writeToSocket(xVelBytes[i]);
		}
		for (int i = 0; i < yVelBytes.length; i++) {
			communicationService.writeToSocket(yVelBytes[i]);
		}
	}

	public void reciveSync() {
		if (communicationService.nextFromSocket() != null
				&& communicationService.nextFromSocket() == SYNC_ROTATION_FLAG) {
			communicationService.readFromSocket();
			byte[] rotationBytes = new byte[4];
			for (int i = 0; i < rotationBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = communicationService.readFromSocket();
				}
				rotationBytes[i] = aByte;
			}
			sceneManager.getEnemyFighter().setRotation(ByteBuffer.wrap(rotationBytes).getFloat());
		}

		if (communicationService.nextFromSocket() != null
				&& communicationService.nextFromSocket() == SYNC_POSITION_FLAG) {
			communicationService.readFromSocket();
			byte[] xPosBytes = new byte[4];
			byte[] yPosBytes = new byte[4];
			for (int i = 0; i < xPosBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = communicationService.readFromSocket();
				}
				xPosBytes[i] = aByte;
			}
			for (int i = 0; i < yPosBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = communicationService.readFromSocket();
				}
				yPosBytes[i] = aByte;
			}
			sceneManager.getEnemyFighter().setPosition(ByteBuffer.wrap(xPosBytes).getFloat(),
					ByteBuffer.wrap(yPosBytes).getFloat());
		}

		if (communicationService.nextFromSocket() != null
				&& communicationService.nextFromSocket() == SYNC_VELOCITY_FLAG) {
			communicationService.readFromSocket();
			byte[] xVelBytes = new byte[4];
			byte[] yVelBytes = new byte[4];
			for (int i = 0; i < xVelBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = communicationService.readFromSocket();
				}
				xVelBytes[i] = aByte;
			}
			for (int i = 0; i < yVelBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = communicationService.readFromSocket();
				}
				yVelBytes[i] = aByte;
			}
			sceneManager.getEnemyFighter().setVelocity(ByteBuffer.wrap(xVelBytes).getFloat(),
					ByteBuffer.wrap(yVelBytes).getFloat());
		}

	}

	private void checkGameOver(Fighter player, Fighter enemy) {

		if (!player.isAlive() || !enemy.isAlive()) {
			if (player.isAlive()) {
				winner = true;
			}
			gameOver = true;
			hud.showMessage(winner);

			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
					intent.putExtra("isWinner", winner);
					intent.putExtra("bulletsFired", sceneManager.getPlayerFighter().getBulletsFired());
					intent.putExtra("hits", sceneManager.getEnemyFighter().getTimesHit());
					startActivity(intent);
					finish();
				}
			}, 3000);
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
							communicationService.writeToSocket(OPONENT_HIT_FLAG);
							hud.setEnemyHealth(fighter.getHealth());
						} else {
							communicationService.writeToSocket(PLAYER_HIT_FLAG);
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
							communicationService.writeToSocket(OPONENT_HIT_FLAG);
							hud.setEnemyHealth(fighter.getHealth());
						} else {
							communicationService.writeToSocket(PLAYER_HIT_FLAG);
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
