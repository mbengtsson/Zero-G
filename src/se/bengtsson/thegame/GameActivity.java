package se.bengtsson.thegame;

import java.nio.ByteBuffer;

import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.ui.activity.LayoutGameActivity;

import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService;
import se.bengtsson.thegame.bluetooth.BluetoothCommunicationService.LocalBinder;
import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.hud.PlayerHUD;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.manager.SceneManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameActivity extends LayoutGameActivity implements IUpdateHandler {

	private boolean debug = false;

	public static final int CAMERA_WIDTH = 800;
	public static final int CAMERA_HEIGHT = 450;

	public static byte ROTATION_FLAG = 0x1;
	public static byte THRUST_FLAG = 0x2;
	public static byte FIRE_FLAG = 0x3;
	public static byte SYNC_ROTATION_FLAG = 0x4;
	public static byte SYNC_VELOCITY_FLAG = 0x5;
	public static byte SYNC_POSITION_FLAG = 0x6;
	public static byte PLAYER_HIT_FLAG = 0x7;
	public static byte OPONENT_HIT_FLAG = 0x8;

	private BluetoothCommunicationService communicationService;

	private boolean server;
	private boolean multiplayerGame;

	private Camera camera;
	private FixedStepPhysicsWorld physicsWorld;
	private PlayerController playerController;
	private ExternalController externalController;

	private ResourceManager resources;
	private SceneManager sceneManager;

	PlayerHUD hud;
	private Entity spriteLayer;
	private Entity foregroundLayer;

	private Sprite background;

	private double time;

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
		multiplayerGame = getIntent().getBooleanExtra("isMultiplayerGame", false);
		server = getIntent().getBooleanExtra("isServer", false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (multiplayerGame) {
			Intent intent = new Intent(this, BluetoothCommunicationService.class);
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {

		return new FixedStepEngine(pEngineOptions, 30);

	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions =
				new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH,
						CAMERA_HEIGHT), camera);
		engineOptions.getRenderOptions().setDithering(true);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {

		physicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 3);

		physicsWorld.setContactListener(createContactListener());

		ResourceManager.prepareManager(physicsWorld, mEngine, this, camera, getVertexBufferObjectManager());
		resources = ResourceManager.getInstance();

		resources.loadTextures();
		resources.loadSounds();
		resources.loadFonts();

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		final Scene scene = new Scene();

		spriteLayer = new Entity();
		foregroundLayer = new Entity();

		scene.attachChild(spriteLayer);
		scene.attachChild(foregroundLayer);

		scene.registerUpdateHandler(physicsWorld);
		scene.registerUpdateHandler(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);

		sceneManager = new SceneManager(scene, spriteLayer);

		externalController = new ExternalController();
		playerController = new PlayerController();
		hud = new PlayerHUD(playerController);
		camera.setHUD(hud);

		if (debug) {
			DebugRenderer debugRenderer = new DebugRenderer(physicsWorld, getVertexBufferObjectManager());
			scene.attachChild(debugRenderer);
		}
		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {

		background = new Sprite(0, 0, resources.backgroundTextureRegion, getVertexBufferObjectManager());
		pScene.setBackground(new SpriteBackground(background));

		if (!multiplayerGame) {
			sceneManager.setupSingleplayerScene(playerController);

		} else {
			sceneManager.setupMultiplayerScene(playerController, externalController, server);
		}

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		this.enableAccelerationSensor(playerController);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableAccelerationSensor();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (multiplayerGame) {
			unbindService(serviceConnection);
		}
	}

	@Override
	protected int getLayoutID() {
		return R.layout.activity_game;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.gameSurface;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {

		if (multiplayerGame) {

			communicationService.writeToSocket(ROTATION_FLAG);

			communicationService.writeToSocket(playerController.getTilt());

			if (playerController.isRightTriggerPressed()) {
				communicationService.writeToSocket(THRUST_FLAG);
			}

			if (playerController.isLeftTriggerPressed()) {
				communicationService.writeToSocket(FIRE_FLAG);
			}

			if ((System.currentTimeMillis() - time) > 1000) {
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

		}

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

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	private ContactListener createContactListener() {
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
