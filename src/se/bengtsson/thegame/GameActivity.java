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
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.ui.activity.LayoutGameActivity;

import se.bengtsson.thegame.bluetooth.BluetoothConnectionManager;
import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameActivity extends LayoutGameActivity implements IUpdateHandler {

	private boolean debug = true;

	public static final int CAMERA_WIDTH = 800;
	public static final int CAMERA_HEIGHT = 450;

	public static byte ROTATION_FLAG = 0x1;
	public static byte THRUST_FLAG = 0x2;
	public static byte FIRE_FLAG = 0x3;

	private BluetoothConnectionManager connectionManager;
	private boolean isServer;
	private boolean isMultiplayerGame;

	private Camera camera;
	private FixedStepPhysicsWorld physicsWorld;
	private PlayerController playerController;
	private ExternalController externalController;

	private ResourceManager resources;

	private Entity backgroundLayer;
	private Entity spriteLayer;
	private Entity foregroundLayer;

	private Fighter playerFighter;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		isMultiplayerGame = getIntent().getBooleanExtra("isMultiplayerGame", false);
		if (isMultiplayerGame) {
			connectionManager = BluetoothConnectionManager.getInstance();
			isServer = getIntent().getBooleanExtra("isServer", false);
		}
	}

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		return new FixedStepEngine(pEngineOptions, 60);
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
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, 0), false, 8, 3);
		physicsWorld.setContactListener(createContactListener());
		;

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

		backgroundLayer = new Entity();
		spriteLayer = new Entity();
		foregroundLayer = new Entity();

		scene.attachChild(backgroundLayer);
		scene.attachChild(spriteLayer);
		scene.attachChild(foregroundLayer);

		scene.registerUpdateHandler(physicsWorld);
		scene.registerUpdateHandler(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);

		externalController = new ExternalController();

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

		if (!isMultiplayerGame || isServer) {
			playerFighter = new Fighter(playerController, resources, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
			spriteLayer.attachChild(playerFighter);
		} else {
			playerFighter = new Fighter(externalController, resources, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
			spriteLayer.attachChild(playerFighter);
		}

		Sprite aSprite = new Sprite(100, 100, resources.dummyTextureRegion, getVertexBufferObjectManager());
		Body aBody =
				PhysicsFactory.createCircleBody(physicsWorld, aSprite, BodyType.DynamicBody,
						PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(aSprite, aBody, true, true));
		spriteLayer.attachChild(aSprite);

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
		connectionManager.destroy();
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

		if (isMultiplayerGame && !isServer) {

			Byte input;
			do {

				input = connectionManager.readFromSocket();

				if (input != null) {
					Log.d("byte", Byte.toString(input));
					byte aByte = input.byteValue();
					if (aByte == ROTATION_FLAG) {
						byte[] bytes = new byte[4];
						for (int i = 0; bytes.length > i; i++) {
							Byte b = null;
							while (b == null) {
								b = connectionManager.readFromSocket();
							}

							bytes[i] = b.byteValue();
						}
						float tilt = ByteBuffer.wrap(bytes).getFloat();
						externalController.setTilt(tilt);
					} else {
						externalController.setTilt(0);
					}

					if (aByte == THRUST_FLAG) {
						externalController.setRightTriggerPressed(true);
					} else {
						externalController.setRightTriggerPressed(false);
					}

					if (aByte == FIRE_FLAG) {
						externalController.setLeftTriggerPressed(true);
					} else {
						externalController.setLeftTriggerPressed(false);
					}
				}
			} while (input != null);

		} else if (isMultiplayerGame && isServer) {

			float tilt = playerController.getTilt();
			connectionManager.writeToSocket(ROTATION_FLAG);
			byte[] bytes = ByteBuffer.allocate(4).putFloat(tilt).array();
			for (int i = 0; bytes.length > i; i++) {
				connectionManager.writeToSocket(bytes[i]);
			}

			if (playerController.isRightTriggerPressed()) {
				connectionManager.writeToSocket(THRUST_FLAG);
			}

			if (playerController.isLeftTriggerPressed()) {
				connectionManager.writeToSocket(FIRE_FLAG);
			}

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

				Log.d("ContactListener", "Contact!!");
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
