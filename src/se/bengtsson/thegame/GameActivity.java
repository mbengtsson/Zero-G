package se.bengtsson.thegame;

import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.ui.activity.LayoutGameActivity;

import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;

import com.badlogic.gdx.math.Vector2;

public class GameActivity extends LayoutGameActivity implements IUpdateHandler {

	public static final int CAMERA_WIDTH = 800;
	public static final int CAMERA_HEIGHT = 450;

	private Camera camera;
	private FixedStepPhysicsWorld physicsWorld;
	private PlayerController playerController;

	private ResourceManager resources;

	Fighter playerFighter;

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

		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, 0), false, 8, 3);

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
		scene.registerUpdateHandler(physicsWorld);
		scene.registerUpdateHandler(this);

		playerController = new PlayerController(scene);

		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {

		playerFighter = new Fighter(playerController, resources, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
		pScene.attachChild(playerFighter);

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
	protected int getLayoutID() {
		return R.layout.activity_game;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.gameSurface;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		// HERE IS THE GAME LOOP
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
