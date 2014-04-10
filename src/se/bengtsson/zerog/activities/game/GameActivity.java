package se.bengtsson.zerog.activities.game;

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

import se.bengtsson.zerog.R;
import se.bengtsson.zerog.activities.MainActivity;
import se.bengtsson.zerog.activities.StatisticsActivity;
import se.bengtsson.zerog.game.controller.ExternalController;
import se.bengtsson.zerog.game.controller.PlayerController;
import se.bengtsson.zerog.game.hud.GameHUD;
import se.bengtsson.zerog.game.manager.ResourceManager;
import se.bengtsson.zerog.game.manager.SceneManager;
import se.bengtsson.zerog.game.objects.fighter.Fighter;
import se.bengtsson.zerog.game.objects.pools.BulletPool.Bullet;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameActivity extends LayoutGameActivity implements IUpdateHandler {

	private final int CAMERA_WIDTH = 800;
	private final int CAMERA_HEIGHT = 450;

	protected ResourceManager resources;
	protected SceneManager sceneManager;

	protected PlayerController playerController;
	protected ExternalController externalController;
	protected GameHUD hud;

	protected boolean gameOver = false;

	private Camera camera;
	private FixedStepPhysicsWorld physicsWorld;
	private Handler handler;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private Entity spriteLayer;
	private Sprite background;

	private boolean debug = false;
	private boolean winner = false;

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

		scene.attachChild(spriteLayer);

		scene.registerUpdateHandler(physicsWorld);
		scene.registerUpdateHandler(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);

		sceneManager = new SceneManager(spriteLayer);

		playerController = new PlayerController();
		externalController = new ExternalController();

		hud = new GameHUD(playerController);
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

	protected void checkGameOver(final boolean multiPlayer) {
		Fighter player = sceneManager.getPlayerFighter();
		Fighter enemy = sceneManager.getEnemyFighter();

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
	protected void onStop() {
		Log.d("GameActivity", "stop");
		sceneManager.cleanUp();
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

	private ContactListener createContactListener() {
		ContactListener contactListener = new ContactListener() {
			@Override
			public void beginContact(Contact contact) {

				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();

				handleContact(fixtureA, fixtureB);
				handleContact(fixtureB, fixtureA);

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

	private void handleContact(Fixture fixtureA, Fixture fixtureB) {

		if (fixtureA.getBody().getUserData() instanceof Bullet) {
			Bullet bullet = (Bullet) fixtureA.getBody().getUserData();
			sceneManager.getBulletPool().recyclePoolItem(bullet);
			if (fixtureB.getBody().getUserData() instanceof Fighter) {
				Fighter fighter = (Fighter) fixtureB.getBody().getUserData();
				fighterHit(fighter);
			}

		}
	}

	protected void fighterHit(Fighter fighter) {

	}
}
