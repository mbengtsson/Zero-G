package se.bengtsson.thegame.game.manager;

import org.andengine.entity.Entity;

import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool;

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
		this.resources = ResourceManager.getInstance();
		CAMERA_WIDTH = resources.camera.getWidth();
		CAMERA_HEIGHT = resources.camera.getHeight();

		this.spriteLayer = spriteLayer;
		this.bulletPool = new BulletPool(spriteLayer);

	}

	public void setupSingleplayerScene(PlayerController playerController, ExternalController externalController) {

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
		playerFighter =
				new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		enemyFighter =
				new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
						CAMERA_HEIGHT / 2, true);
		spriteLayer.attachChild(enemyFighter);
	}

	private void setupMultiPlayerClientScene() {
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

}
