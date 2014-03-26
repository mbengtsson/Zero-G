package se.bengtsson.thegame.game.manager;

import org.andengine.entity.Entity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;

import se.bengtsson.thegame.game.controller.ExternalController;
import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import se.bengtsson.thegame.game.objects.pools.BulletPool;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class SceneManager {

	private static float CAMERA_WIDTH;
	private static float CAMERA_HEIGHT;

	private ResourceManager resources;
	private Scene scene;
	private Entity spriteLayer;
	private BulletPool bulletPool;
	private PlayerController playerController;
	private ExternalController externalController;
	private boolean server;

	private Fighter playerFighter;
	private Fighter enemyFighter;

	public SceneManager(Scene scene, Entity spriteLayer) {
		this.scene = scene;
		this.resources = ResourceManager.getInstance();
		CAMERA_WIDTH = resources.camera.getWidth();
		CAMERA_HEIGHT = resources.camera.getHeight();

		this.spriteLayer = spriteLayer;
		this.bulletPool = new BulletPool(spriteLayer);

	}

	public void setupMultiplayerScene(PlayerController playerController, ExternalController externalController,
			boolean server) {

		this.playerController = playerController;
		this.externalController = externalController;
		this.server = server;

		if (server) {
			playerFighter =
					new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2,
							false);
			spriteLayer.attachChild(playerFighter);

			enemyFighter =
					new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
							CAMERA_HEIGHT / 2, true);
			spriteLayer.attachChild(enemyFighter);
		} else {
			playerFighter =
					new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH - (CAMERA_WIDTH / 4),
							CAMERA_HEIGHT / 2, false);
			spriteLayer.attachChild(playerFighter);

			enemyFighter =
					new Fighter(this.externalController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2,
							true);
			spriteLayer.attachChild(enemyFighter);
		}
	}

	public void setupSingleplayerScene(PlayerController playerController) {

		this.playerController = playerController;

		playerFighter =
				new Fighter(this.playerController, bulletPool, resources, CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2, false);
		spriteLayer.attachChild(playerFighter);

		Sprite aSprite = new Sprite(100, 100, resources.dummyTextureRegion, resources.vbom);
		Body aBody =
				PhysicsFactory.createCircleBody(resources.physicsWorld, aSprite, BodyType.DynamicBody,
						PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(aSprite, aBody, true, true));
		spriteLayer.attachChild(aSprite);
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
