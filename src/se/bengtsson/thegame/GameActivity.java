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

import se.bengtsson.thegame.bluetooth.BluetoothConnectionManager;
import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.manager.SceneManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool.Bullet;
import android.os.Bundle;

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

	private BluetoothConnectionManager connectionManager;
	private boolean server;
	private boolean multiplayerGame;

	private Camera camera;
	private FixedStepPhysicsWorld physicsWorld;
	private PlayerController playerController;
	private ExternalController externalController;

	private ResourceManager resources;
	private SceneManager sceneManager;

	private Entity spriteLayer;
	private Entity foregroundLayer;

	private Sprite background;

	private double time;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		multiplayerGame = getIntent().getBooleanExtra("isMultiplayerGame", false);
		if (multiplayerGame) {
			connectionManager = BluetoothConnectionManager.getInstance();
			server = getIntent().getBooleanExtra("isServer", false);
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

		externalController = new ExternalController();

		sceneManager = new SceneManager(scene, spriteLayer);

		playerController = new PlayerController();
		foregroundLayer.attachChild(playerController.getLeftTrigger());
		foregroundLayer.attachChild(playerController.getRightTrigger());
		scene.registerTouchArea(playerController.getLeftTrigger());
		scene.registerTouchArea(playerController.getRightTrigger());

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
		if (multiplayerGame) {
			connectionManager.destroy();
		}
		super.onStop();
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

			connectionManager.writeToSocket(ROTATION_FLAG);

			connectionManager.writeToSocket(playerController.getTilt());

			if (playerController.isRightTriggerPressed()) {
				connectionManager.writeToSocket(THRUST_FLAG);
			}

			if (playerController.isLeftTriggerPressed()) {
				connectionManager.writeToSocket(FIRE_FLAG);
			}

			if ((System.currentTimeMillis() - time) > 1000) {
				sendSync();
			}

			if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == ROTATION_FLAG) {
				connectionManager.readFromSocket();
				Byte tilt = null;
				while (tilt == null) {
					tilt = connectionManager.readFromSocket();
				}
				externalController.setTilt(tilt);
			}

			if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == THRUST_FLAG) {
				connectionManager.readFromSocket();
				externalController.setRightTriggerPressed(true);
			} else {
				externalController.setRightTriggerPressed(false);
			}

			if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == FIRE_FLAG) {
				connectionManager.readFromSocket();
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
		connectionManager.writeToSocket(SYNC_ROTATION_FLAG);
		for (int i = 0; i < rotationBytes.length; i++) {
			connectionManager.writeToSocket(rotationBytes[i]);
		}

		byte[] xPosBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getXpos()).array();
		byte[] yPosBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getYpos()).array();
		connectionManager.writeToSocket(SYNC_POSITION_FLAG);
		for (int i = 0; i < xPosBytes.length; i++) {
			connectionManager.writeToSocket(xPosBytes[i]);
		}
		for (int i = 0; i < yPosBytes.length; i++) {
			connectionManager.writeToSocket(yPosBytes[i]);
		}

		byte[] xVelBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getVelocityX()).array();
		byte[] yVelBytes = ByteBuffer.allocate(4).putFloat(sceneManager.getPlayerFighter().getVelocityY()).array();
		connectionManager.writeToSocket(SYNC_VELOCITY_FLAG);
		for (int i = 0; i < xVelBytes.length; i++) {
			connectionManager.writeToSocket(xVelBytes[i]);
		}
		for (int i = 0; i < yVelBytes.length; i++) {
			connectionManager.writeToSocket(yVelBytes[i]);
		}
	}

	public void reciveSync() {
		if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == SYNC_ROTATION_FLAG) {
			connectionManager.readFromSocket();
			byte[] rotationBytes = new byte[4];
			for (int i = 0; i < rotationBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = connectionManager.readFromSocket();
				}
				rotationBytes[i] = aByte;
			}
			sceneManager.getEnemyFighter().setRotation(ByteBuffer.wrap(rotationBytes).getFloat());
		}

		if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == SYNC_POSITION_FLAG) {
			connectionManager.readFromSocket();
			byte[] xPosBytes = new byte[4];
			byte[] yPosBytes = new byte[4];
			for (int i = 0; i < xPosBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = connectionManager.readFromSocket();
				}
				xPosBytes[i] = aByte;
			}
			for (int i = 0; i < yPosBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = connectionManager.readFromSocket();
				}
				yPosBytes[i] = aByte;
			}
			sceneManager.getEnemyFighter().setPosition(ByteBuffer.wrap(xPosBytes).getFloat(),
					ByteBuffer.wrap(yPosBytes).getFloat());
		}

		if (connectionManager.nextFromSocket() != null && connectionManager.nextFromSocket() == SYNC_VELOCITY_FLAG) {
			connectionManager.readFromSocket();
			byte[] xVelBytes = new byte[4];
			byte[] yVelBytes = new byte[4];
			for (int i = 0; i < xVelBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = connectionManager.readFromSocket();
				}
				xVelBytes[i] = aByte;
			}
			for (int i = 0; i < yVelBytes.length; i++) {
				Byte aByte = null;
				while (aByte == null) {
					aByte = connectionManager.readFromSocket();
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
					if (fixtureB.getBody().getUserData() instanceof Fighter) {
						Fighter fighter = (Fighter) fixtureB.getBody().getUserData();
						fighter.explode();
					}

				}
				if (fixtureB.getBody().getUserData() instanceof Bullet) {
					Bullet bullet = (Bullet) fixtureB.getBody().getUserData();
					sceneManager.getBulletPool().recyclePoolItem(bullet);
					if (fixtureA.getBody().getUserData() instanceof Fighter) {
						Fighter fighter = (Fighter) fixtureA.getBody().getUserData();
						fighter.explode();
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
