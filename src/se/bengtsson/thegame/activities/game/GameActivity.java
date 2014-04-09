package se.bengtsson.thegame.activities.game;

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

import se.bengtsson.thegame.R;
import se.bengtsson.thegame.activities.MainActivity;
import se.bengtsson.thegame.activities.StatisticsActivity;
import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.hud.PlayerHUD;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.manager.SceneManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameActivity extends LayoutGameActivity implements IUpdateHandler {

	protected Handler handler;

	private boolean debug = false;

	private final int CAMERA_WIDTH = 800;
	private final int CAMERA_HEIGHT = 450;

	private Camera camera;
	protected FixedStepPhysicsWorld physicsWorld;
	protected PlayerController playerController;
	protected ExternalController externalController;

	private SensorManager sensorManager;
	private Sensor accelerometer;

	protected ResourceManager resources;
	protected SceneManager sceneManager;

	protected PlayerHUD hud;
	private Entity spriteLayer;
	private Entity foregroundLayer;

	private Sprite background;

	protected boolean gameOver = false;
	protected boolean winner = false;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		handler = new Handler();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
		engineOptions.getAudioOptions().setNeedsSound(true);

		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {

		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, 0), false, 8, 3);

		physicsWorld.setContactListener(createContactListener());

		ResourceManager.prepareManager(physicsWorld, mEngine, this, camera, getVertexBufferObjectManager());
		resources = ResourceManager.getInstance();

		resources.loadTextures();
		resources.loadFonts();
		resources.loadSounds();

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

		sceneManager = new SceneManager(spriteLayer);

		playerController = new PlayerController();
		externalController = new ExternalController();

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

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	protected void checkGameOver(Fighter player, Fighter enemy, final boolean multiPlayer) {

		if (!player.isAlive() || !enemy.isAlive()) {
			if (player.isAlive()) {
				winner = true;
			}
			gameOver = true;

			hud.showGameOverMessage(winner);

			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
					intent.putExtra("isWinner", winner);
					intent.putExtra("bulletsFired", sceneManager.getPlayerFighter().getBulletsFired());
					intent.putExtra("hits", sceneManager.getEnemyFighter().getTimesHit());
					intent.putExtra("debriefing", true);
					intent.putExtra("multiPlayer", multiPlayer);
					startActivity(intent);
					finish();
				}
			}, 3000);
		}

	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		sensorManager.registerListener(playerController, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		sensorManager.unregisterListener(playerController);
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

	}

	@Override
	public void reset() {

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	protected ContactListener createContactListener() {
		ContactListener contactListener = new ContactListener() {
			@Override
			public void beginContact(Contact contact) {

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
