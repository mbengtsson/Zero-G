package se.bengtsson.thegame.game.objects.fighter;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.andengine.entity.Entity;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.util.math.MathUtils;

import se.bengtsson.thegame.game.controller.Controller;
import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.fighter.factories.BulletsFactory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Fighter extends Entity {

	private Controller controller;

	private final float WORLD_WIDTH;
	private final float WORLD_HEIGHT;

	private final float THRUST = 1.5f / PIXEL_TO_METER_RATIO_DEFAULT;

	private BulletsFactory bulletFactory;
	private final float RATE_OF_FIRE = 5;
	private long lastFired;

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

	public Fighter(Controller controller, ResourceManager resources, float xPos, float yPos) {

		this.controller = controller;
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

		bulletFactory = new BulletsFactory(this);
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

		rotate(controller.getTilt());

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
			Sprite bullet = bulletFactory.createBullet(xPos, yPos, rotation);
			this.attachChild(bullet);
			lastFired = time;
		}
	}

	public float getXpos() {
		return xPos * PIXEL_TO_METER_RATIO_DEFAULT;
	}

	public float getYpos() {
		return yPos * PIXEL_TO_METER_RATIO_DEFAULT;
	}

	public void setPosition(float xPos, float yPos) {

		xPos /= PIXEL_TO_METER_RATIO_DEFAULT;
		yPos /= PIXEL_TO_METER_RATIO_DEFAULT;

		fighterBody.setTransform(xPos, yPos, rotation);
	}

	public float getWidth() {
		return fighter.getWidth();
	}

	public float getHeight() {
		return fighter.getHeight();
	}

	@Override
	public float getRotation() {
		return MathUtils.radToDeg(rotation);
	}

	public boolean isAccelerating() {
		return accelerating;
	}

	public void setAccelerating(boolean accelerating) {
		this.accelerating = accelerating;
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
