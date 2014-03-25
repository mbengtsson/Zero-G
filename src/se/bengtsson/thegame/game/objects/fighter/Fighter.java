package se.bengtsson.thegame.game.objects.fighter;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.andengine.entity.Entity;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import se.bengtsson.thegame.bluetooth.BluetoothConnectionManager;
import se.bengtsson.thegame.game.controller.Controller;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.pools.BulletPool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Fighter extends Entity {

	private Controller controller;
	private BluetoothConnectionManager connectionManager;

	private final float WORLD_WIDTH;
	private final float WORLD_HEIGHT;

	private final float THRUST = 1.5f / PIXEL_TO_METER_RATIO_DEFAULT;
	private final float ROTATION_MODIFIER = 0.15f;

	// private BulletsFactory bulletFactory;
	private BulletPool bulletPool;

	private final float RATE_OF_FIRE = 5;
	private long lastFired;
	private boolean fireLeft;

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
	private Body fighterBody;

	public Fighter(Controller controller, BulletPool bulletPool, ResourceManager resources, float xPos, float yPos) {

		this.controller = controller;
		this.bulletPool = bulletPool;
		this.WORLD_WIDTH = resources.camera.getWidth() / PIXEL_TO_METER_RATIO_DEFAULT;
		this.WORLD_HEIGHT = resources.camera.getHeight() / PIXEL_TO_METER_RATIO_DEFAULT;

		this.fighter = new Sprite(xPos, yPos, resources.fighterTextureRegion, resources.vbom);
		this.mainThrust = new Sprite(xPos, yPos, resources.fighterThrustTextureRegion, resources.vbom);
		this.leftThrust = new Sprite(xPos, yPos, resources.fighterLeftTextureRegion, resources.vbom);
		this.rightThrust = new Sprite(xPos, yPos, resources.fighterRightTextureRegion, resources.vbom);
		fighterBody =
				createFighterBody(resources.physicsWorld, fighter, BodyType.DynamicBody,
						PhysicsFactory.createFixtureDef(1.0f, 0.0f, 0.5f));

		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(fighter, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(mainThrust, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(leftThrust, fighterBody, true, true));
		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(rightThrust, fighterBody, true, true));

		this.attachChild(fighter);
		this.attachChild(mainThrust);
		this.attachChild(leftThrust);
		this.attachChild(rightThrust);

		mainThrust.setVisible(false);
		leftThrust.setVisible(false);
		rightThrust.setVisible(false);

		// bulletFactory = new BulletsFactory(this);
		connectionManager = BluetoothConnectionManager.getInstance();
	}

	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {

		xPos = fighterBody.getPosition().x;
		yPos = fighterBody.getPosition().y;

		velocityX = fighterBody.getLinearVelocity().x;
		velocityY = fighterBody.getLinearVelocity().y;

		rotation = fighterBody.getAngle();

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

			// Sprite bullet = bulletFactory.createBullet(xPos, yPos, rotation);
			Sprite bullet = bulletPool.obtainPoolItem(xPos, yPos, rotation);
			// this.attachChild(bullet);
			lastFired = time;
		}
	}

	public float getXpos() {
		// return xPos * PIXEL_TO_METER_RATIO_DEFAULT;
		return xPos;
	}

	public float getYpos() {
		// return yPos * PIXEL_TO_METER_RATIO_DEFAULT;
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

	public void setPosition(float xPos, float yPos) {

		// xPos /= PIXEL_TO_METER_RATIO_DEFAULT;
		// yPos /= PIXEL_TO_METER_RATIO_DEFAULT;

		fighterBody.setTransform(xPos, yPos, rotation);
	}

	public void setVelocity(float velocityX, float velocityY) {
		fighterBody.setLinearVelocity(velocityX, velocityY);
	}

	public void setAccelerating(boolean accelerating) {
		this.accelerating = accelerating;
	}

	public void setRotation(float rotation) {
		fighterBody.setTransform(fighterBody.getPosition().x, fighterBody.getPosition().y, rotation);
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
