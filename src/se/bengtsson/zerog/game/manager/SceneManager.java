package se.bengtsson.zerog.game.manager;

import org.andengine.entity.Entity;

import se.bengtsson.zerog.game.controller.ExternalController;
import se.bengtsson.zerog.game.controller.PlayerController;
import se.bengtsson.zerog.game.objects.fighter.Fighter;
import se.bengtsson.zerog.game.objects.pools.BulletPool;
import android.util.Log;

public class SceneManager {

	private static float CAMERA_WIDTH;
	private static float CAMERA_HEIGHT;

	private ResourceManager resources;
	private Entity spriteLayer;
	private BulletPool bulletPool;
	private PlayerController playerController;
	private ExternalController externalController;

	private Fighter playerFighter;
	private Fighter enemyFighter;

	public SceneManager(Entity spriteLayer) {
		Log.d("SceneManager", "Creating SceneManager");
		this.resources = ResourceManager.getInstance();
		CAMERA_WIDTH = resources.camera.getWidth();
		CAMERA_HEIGHT = resources.camera.getHeight();

		this.spriteLayer = spriteLayer;
		this.bulletPool = new BulletPool(spriteLayer);

	}

	public void setupSingleplayerScene(PlayerController playerController, ExternalController externalController) {
		Log.d("SceneManager", "Setting up SinglePlayerScene");

		this.playerController = playerController;
		this.externalController = externalController;

		playerFighter =
				new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	public void setupMultiPlayerScene(PlayerController playerController, ExternalController externalController,
			boolean server) {

		this.playerController = playerController;
		this.externalController = externalController;

		if (server) {
			setupMultiPlayerServerScene();
		} else {
			setupMultiPlayerClientScene();
		}
	}

	private void setupMultiPlayerServerScene() {
		Log.d("SceneManager", "Setting up MultiPlayerServerScene");

		playerFighter =
				new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	private void setupMultiPlayerClientScene() {
		Log.d("SceneManager", "Setting up MultiPlayerClientScene");
		playerFighter =
				new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	public BulletPool getBulletPool() {
		return bulletPool;
	}

	public Fighter getPlayerFighter() {
		return playerFighter;
	}

	public Fighter getEnemyFighter() {
		return enemyFighter;
	}

	public void cleanUp() {
		playerFighter.destroy();
		enemyFighter.destroy();
	}

}
