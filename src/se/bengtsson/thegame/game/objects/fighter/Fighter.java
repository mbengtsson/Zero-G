package se.bengtsson.thegame.game.objects.fighter;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

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

	private BulletPool bulletPool;

	private final float RATE_OF_FIRE = 5;
	private long lastFired;
	private boolean fireLeft;

	private boolean enemy;
	private boolean alive = true;
	private int health = 100;

	private float xPos;
	private float yPos;

	private float velocityX;
	private float velocityY;

	private boolean accelerating = false;

	private float rotation;

	private Sprite fighter;
	private Sprite mainThrust;
	private Sprite leftThrust;
	private Sprite rightThrust;
	private AnimatedSprite explosion;
	private Body fighterBody;

	public Fighter(Controller controller, BulletPool bulletPool, ResourceManager resources, float xPos, float yPos,
			boolean enemy) {

		this.resources = resources;

		this.controller = controller;
		this.bulletPool = bulletPool;
		this.WORLD_WIDTH = resources.camera.getWidth() / PIXEL_TO_METER_RATIO_DEFAULT;
		this.WORLD_HEIGHT = resources.camera.getHeight() / PIXEL_TO_METER_RATIO_DEFAULT;
		this.enemy = enemy;

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
	}

	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {

		xPos = fighterBody.getPosition().x;
		yPos = fighterBody.getPosition().y;

		velocityX = fighterBody.getLinearVelocity().x;
		velocityY = fighterBody.getLinearVelocity().y;

		rotation = fighterBody.getAngle();

		if (alive) {
			if (controller.isRightTriggerPressed()) {
				mainThrust.setVisible(true);
				accelerate();
			} else {
				mainThrust.setVisible(false);
			}

			if (controller.isLeftTriggerPressed()) {
				fire();
			}

			rotate(controller.getTilt() * ROTATION_MODIFIER);
		}

		if (xPos < 0) {
			fighterBody.setTransform(WORLD_WIDTH, yPos, rotation);
		} else if (xPos > WORLD_WIDTH) {
			fighterBody.setTransform(0, yPos, rotation);
		}

		if (yPos < 0) {
			fighterBody.setTransform(xPos, WORLD_HEIGHT, rotation);
		} else if (yPos > WORLD_HEIGHT) {
			fighterBody.setTransform(xPos, 0, rotation);
		}

		super.onManagedUpdate(pSecondsElapsed);
	}

	public void rotate(float velocity) {

		if (velocity < 0) {
			leftThrust.setVisible(true);
			rightThrust.setVisible(false);
			if (velocity > -1) {
				leftThrust.setAlpha(Math.abs(velocity));
			}
		} else if (velocity > 0) {
			leftThrust.setVisible(false);
			rightThrust.setVisible(true);
			if (velocity < 1) {
				rightThrust.setAlpha(velocity);
			}
		} else {
			leftThrust.setVisible(false);
			rightThrust.setVisible(false);
		}

		fighterBody.setAngularVelocity(velocity);
	}

	public void accelerate() {

		velocityX += (float) (Math.sin(rotation) * THRUST);
		velocityY -= (float) (Math.cos(rotation) * THRUST);

		fighterBody.setLinearVelocity(velocityX, velocityY);
	}

	public void fire() {
		long time = System.currentTimeMillis();

		if (time - lastFired > 1000 / RATE_OF_FIRE) {
			float offsetX = 15 / PIXEL_TO_METER_RATIO_DEFAULT;
			float offsetY;

			if (fireLeft) {
				offsetY = -10 / PIXEL_TO_METER_RATIO_DEFAULT;
				fireLeft = false;
			} else {
				offsetY = 10 / PIXEL_TO_METER_RATIO_DEFAULT;
				fireLeft = true;
			}

			float xPos = (float) (this.xPos + (Math.sin(rotation) * offsetX + Math.cos(rotation) * offsetY));
			float yPos = (float) (this.yPos + (Math.sin(rotation) * offsetY - Math.cos(rotation) * offsetX));

			bulletPool.obtainPoolItem(xPos, yPos, rotation);
			lastFired = time;
		}
	}

	public void hit() {
		health -= 10;
		if (health <= 0) {
			alive = false;
			explode();
		}
	}

	public void explode() {
		explosion.setVisible(true);
		fighter.setVisible(false);
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
		return xPos;
	}

	public float getYpos() {
		return yPos;
	}

	public float getVelocityX() {
		return velocityX;
	}

	public float getVelocityY() {
		return velocityY;
	}

	public float getWidth() {
		return fighter.getWidth();
	}

	public float getHeight() {
		return fighter.getHeight();
	}

	@Override
	public float getRotation() {
		return rotation;
	}

	public boolean isAccelerating() {
		return accelerating;
	}

	public int getHealth() {
		return health;
	}

	public boolean isEnemy() {
		return enemy;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setPosition(float xPos, float yPos) {

		fighterBody.setTransform(xPos, yPos, rotation, true);
	}

	public void setVelocity(float velocityX, float velocityY) {
		fighterBody.setLinearVelocity(velocityX, velocityY);
	}

	public void setAccelerating(boolean accelerating) {
		this.accelerating = accelerating;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
		fighterBody.setTransform(fighterBody.getPosition().x, fighterBody.getPosition().y, rotation, true);
	}

	private Body createFighterBody(final PhysicsWorld physicsWorld, final IAreaShape areaShape,
			final BodyType bodyType, final FixtureDef fixtureDef) {

		final float halfWidth = areaShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float halfHeight = areaShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

		final float top = -halfHeight;
		final float bottom = halfHeight;
		final float left = -halfHeight;
		final float centerX = 0;
		final float right = halfWidth;

		final Vector2[] vertices = { new Vector2(centerX, top), new Vector2(right, bottom), new Vector2(left, bottom) };

		return PhysicsFactory.createPolygonBody(physicsWorld, areaShape, vertices, bodyType, fixtureDef);
	}

}
