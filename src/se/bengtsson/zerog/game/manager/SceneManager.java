package se.bengtsson.zerog.game.manager;

import org.andengine.entity.Entity;

import se.bengtsson.zerog.game.controller.Controller;
import se.bengtsson.zerog.game.objects.fighter.Fighter;
import se.bengtsson.zerog.game.objects.pools.BulletPool;
import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class SceneManager {

	private static float CAMERA_WIDTH;
	private static float CAMERA_HEIGHT;

	private ResourceManager resources;
	private Entity spriteLayer;
	private BulletPool bulletPool;
	private Controller playerController;
	private Controller externalController;

	private Fighter playerFighter;
	private Fighter enemyFighter;

	public SceneManager(Entity spriteLayer, Controller playerController, Controller externalController) {
		Log.d("SceneManager", "Creating SceneManager");
		this.resources = ResourceManager.getInstance();
		CAMERA_WIDTH = resources.camera.getWidth();
		CAMERA_HEIGHT = resources.camera.getHeight();

		this.spriteLayer = spriteLayer;
		this.bulletPool = new BulletPool(spriteLayer);
		this.playerController = playerController;
		this.externalController = externalController;

	}

	public void setupSingleplayerScene() {
		Log.d("SceneManager", "Setting up SinglePlayerScene");

		playerFighter =
				new Fighter(playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	public void setupMultiPlayerScene(boolean server) {

		if (server) {
			setupMultiPlayerServerScene();
		} else {
			setupMultiPlayerClientScene();
		}
	}

	private void setupMultiPlayerServerScene() {
		Log.d("SceneManager", "Setting up MultiPlayerServerScene");

		playerFighter =
				new Fighter(playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	private void setupMultiPlayerClientScene() {
		Log.d("SceneManager", "Setting up MultiPlayerClientScene");
		playerFighter =
				new Fighter(playerController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(externalController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, true);
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
