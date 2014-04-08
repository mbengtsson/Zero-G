package se.bengtsson.thegame.game.objects.fighter;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.andengine.audio.sound.Sound;
import org.andengine.entity.Entity;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import se.bengtsson.thegame.game.controller.Controller;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.pools.BulletPool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Fighter extends Entity {

	private Controller controller;
	private ResourceManager resources;

	private final float WORLD_WIDTH;
	private final float WORLD_HEIGHT;

	private final float THRUST = 1.5f / PIXEL_TO_METER_RATIO_DEFAULT;
	private final float ROTATION_MODIFIER = 0.15f;

	private final float RATE_OF_FIRE = 5;

	private long lastFired;
	private boolean fireLeft;

	private int bulletsFired;
	private int timesHit;

	private BulletPool bulletPool;

	private boolean enemy;
	private boolean alive = true;
	private int health = 100;

	private Sprite fighter;
	private Sprite mainThrust;
	private Sprite leftThrust;
	private Sprite rightThrust;
	private AnimatedSprite explosion;
	private Body fighterBody;

	private Sound firingSound;
	private Sound hitSound;
	private Sound explosionSound;
	private Sound engineSound;
	private boolean engineSoundPlaying;

	public Fighter(Controller controller, BulletPool bulletPool, ResourceManager resources, float xPos, float yPos,
			boolean enemy) {

		this.resources = resources;

		this.controller = controller;
		this.bulletPool = bulletPool;
		this.WORLD_WIDTH = resources.camera.getWidth() / PIXEL_TO_METER_RATIO_DEFAULT;
		this.WORLD_HEIGHT = resources.camera.getHeight() / PIXEL_TO_METER_RATIO_DEFAULT;
		this.enemy = enemy;

		bulletsFired = 0;
		timesHit = 0;

		if (enemy) {
			this.fighter = new Sprite(xPos, yPos, resources.redFighterTextureRegion, resources.vbom);
		} else {
			this.fighter = new Sprite(xPos, yPos, resources.blueFighterTextureRegion, resources.vbom);
		}

		this.mainThrust = new Sprite(xPos, yPos, resources.fighterThrustTextureRegion, resources.vbom);
		this.leftThrust = new Sprite(xPos, yPos, resources.fighterLeftTextureRegion, resources.vbom);
		this.rightThrust = new Sprite(xPos, yPos, resources.fighterRightTextureRegion, resources.vbom);

		this.explosion = new AnimatedSprite(xPos, yPos, resources.explosionTextureRegion, resources.vbom);

		fighterBody =
				createFighterBody(resources.physicsWorld, fighter, BodyType.DynamicBody,
						PhysicsFactory.createFixtureDef(2.0f, 0.1f, 0.5f));
		fighterBody.setUserData(this);

		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(fighter, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(mainThrust, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(leftThrust, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(rightThrust, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(explosion, fighterBody, true, false));

		this.attachChild(fighter);
		this.attachChild(mainThrust);
		this.attachChild(leftThrust);
		this.attachChild(rightThrust);
		this.attachChild(explosion);

		mainThrust.setVisible(false);
		leftThrust.setVisible(false);
		rightThrust.setVisible(false);
		explosion.setVisible(false);

		this.firingSound = resources.firingSound;
		this.hitSound = resources.hitSound;
		this.explosionSound = resources.explosionSound;
		this.engineSound = resources.engineSound;

		this.firingSound.setVolume(0.2f);
		this.hitSound.setVolume(0.4f);
		this.explosionSound.setVolume(0.5f);
		this.engineSound.setVolume(0.5f);

		engineSoundPlaying = false;

	}

	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {

		if (alive) {
			if (controller.isRightTriggerPressed()) {
				mainThrust.setVisible(true);
				accelerate();
				if (!enemy && !engineSoundPlaying) {
					engineSound.play();
					engineSoundPlaying = true;
				}

			} else {
				mainThrust.setVisible(false);
				if (!enemy && engineSoundPlaying) {
					engineSound.stop();
					engineSoundPlaying = false;
				}
			}

			if (controller.isLeftTriggerPressed()) {
				fire();
			}

			rotate(controller.getTilt() * ROTATION_MODIFIER);
		}

		if (fighterBody.getPosition().x < 0) {
			fighterBody.setTransform(WORLD_WIDTH, fighterBody.getPosition().y, fighterBody.getAngle());
		} else if (fighterBody.getPosition().x > WORLD_WIDTH) {
			fighterBody.setTransform(0, fighterBody.getPosition().y, fighterBody.getAngle());
		}

		if (fighterBody.getPosition().y < 0) {
			fighterBody.setTransform(fighterBody.getPosition().x, WORLD_HEIGHT, fighterBody.getAngle());
		} else if (fighterBody.getPosition().y > WORLD_HEIGHT) {
			fighterBody.setTransform(fighterBody.getPosition().x, 0, fighterBody.getAngle());
		}

		super.onManagedUpdate(pSecondsElapsed);
	}

	public void rotate(float velocity) {

		if (velocity < 0) {
			leftThrust.setVisible(true);
			rightThrust.setVisible(false);
			if (velocity > -1) {
				leftThrust.setAlpha(Math.abs(velocity / 2));
			}
		} else if (velocity > 0) {
			leftThrust.setVisible(false);
			rightThrust.setVisible(true);
			if (velocity < 1) {
				rightThrust.setAlpha(velocity / 2);
			}
		} else {
			leftThrust.setVisible(false);
			rightThrust.setVisible(false);
		}

		fighterBody.setAngularVelocity(velocity);
	}

	public void accelerate() {

		float velocityX = (float) (fighterBody.getLinearVelocity().x + (Math.sin(fighterBody.getAngle()) * THRUST));
		float velocityY = (float) (fighterBody.getLinearVelocity().y - (Math.cos(fighterBody.getAngle()) * THRUST));

		fighterBody.setLinearVelocity(velocityX, velocityY);
	}

	public void fire() {
		long time = System.currentTimeMillis();

		if (time - lastFired > 1000 / RATE_OF_FIRE) {
			if (!enemy) {
				firingSound.play();
			}

			float offsetX = 15 / PIXEL_TO_METER_RATIO_DEFAULT;
			float offsetY;

			if (fireLeft) {
				offsetY = -10 / PIXEL_TO_METER_RATIO_DEFAULT;
				fireLeft = false;
			} else {
				offsetY = 10 / PIXEL_TO_METER_RATIO_DEFAULT;
				fireLeft = true;
			}

			float xPos =
					(float) (fighterBody.getPosition().x + (Math.sin(fighterBody.getAngle()) * offsetX + Math
							.cos(fighterBody.getAngle()) * offsetY));
			float yPos =
					(float) (fighterBody.getPosition().y + (Math.sin(fighterBody.getAngle()) * offsetY - Math
							.cos(fighterBody.getAngle()) * offsetX));

			bulletPool.obtainPoolItem(xPos, yPos, fighterBody.getAngle());
			lastFired = time;
			bulletsFired++;
		}
	}

	public void hit() {
		if (alive) {
			if (!enemy) {
				hitSound.play();
			}
			health -= 10;
			timesHit++;
			if (health <= 0) {
				explode();
				alive = false;
			}
		}
	}

	public void explode() {
		explosionSound.play();
		explosion.setVisible(true);
		fighter.setVisible(false);
		mainThrust.setVisible(false);
		leftThrust.setVisible(false);
		rightThrust.setVisible(false);
		setVelocity(fighterBody.getLinearVelocity().x / 10, fighterBody.getLinearVelocity().y / 10);
		explosion.animate(100, 0);
	}

	public void destroy() {

		resources.engine.runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				PhysicsConnector connector =
						resources.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(fighter);
				resources.physicsWorld.unregisterPhysicsConnector(connector);
				connector = resources.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(leftThrust);
				resources.physicsWorld.unregisterPhysicsConnector(connector);
				connector = resources.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mainThrust);
				resources.physicsWorld.unregisterPhysicsConnector(connector);
				connector =
						resources.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(rightThrust);
				resources.physicsWorld.unregisterPhysicsConnector(connector);
				connector = resources.physicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(explosion);
				resources.physicsWorld.unregisterPhysicsConnector(connector);
				resources.physicsWorld.destroyBody(fighterBody);
				detachChild(fighter);
				detachChild(leftThrust);
				detachChild(mainThrust);
				detachChild(rightThrust);
				detachSelf();

			}

		});

	}

	public float getXpos() {
		return fighterBody.getPosition().x;
	}

	public float getYpos() {
		return fighterBody.getPosition().y;
	}

	public float getVelocityX() {
		return fighterBody.getLinearVelocity().x;
	}

	public float getVelocityY() {
		return fighterBody.getLinearVelocity().y;
	}

	public float getWidth() {
		return fighter.getWidth();
	}

	public float getHeight() {
		return fighter.getHeight();
	}

	@Override
	public float getRotation() {
		return fighterBody.getAngle();
	}

	public int getHealth() {
		return health;
	}

	public int getBulletsFired() {
		return bulletsFired;
	}

	public int getTimesHit() {
		return timesHit;
	}

	public boolean isEnemy() {
		return enemy;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setPosition(float xPos, float yPos) {

		fighterBody.setTransform(xPos, yPos, fighterBody.getAngle(), true);
	}

	public void setVelocity(float velocityX, float velocityY) {
		fighterBody.setLinearVelocity(velocityX, velocityY);
	}

	public void setRotation(float rotation) {
		fighterBody.setTransform(fighterBody.getPosition().x, fighterBody.getPosition().y, rotation, true);
	}

	private Body createFighterBody(PhysicsWorld physicsWorld, IAreaShape areaShape, BodyType bodyType,
			FixtureDef fixtureDef) {

		float halfWidth = areaShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		float halfHeight = areaShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

		float top = -halfHeight;
		float bottom = halfHeight;
		float left = -halfHeight;
		float centerX = 0;
		float right = halfWidth;

		Vector2[] vertices = { new Vector2(centerX, top), new Vector2(right, bottom), new Vector2(left, bottom) };

		return PhysicsFactory.createPolygonBody(physicsWorld, areaShape, vertices, bodyType, fixtureDef);
	}

}
