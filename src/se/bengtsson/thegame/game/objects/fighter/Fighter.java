package se.bengtsson.thegame.game.objects.fighter;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import se.bengtsson.thegame.game.manager.ResourceManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Fighter {

	// TODO: Implement this, rotation as rads
	// TODO: rotate method, rotate all Sprites and body.
	// TODO: Set xSpeed and ySpeed and vector method.

	private float xPos;
	private float yPos;

	private Vector2 velocity;

	private float rotation;

	private Sprite fighter;
	private Sprite mainThrust;
	private Sprite leftThrust;
	private Sprite rightThrust;
	private Body fighterBody;

	public Fighter(ResourceManager resources, int xPos, int yPos) {
		this.fighter = new Sprite(xPos, yPos, resources.fighterTextureRegion, resources.vbom);
		this.mainThrust =
				new Sprite(xPos - fighter.getWidth() / 2, yPos - fighter.getHeight() / 2,
						resources.fighterThrustTextureRegion, resources.vbom);
		this.leftThrust =
				new Sprite(xPos - fighter.getWidth() / 2, yPos - fighter.getHeight() / 2,
						resources.fighterLeftTextureRegion, resources.vbom);
		this.rightThrust =
				new Sprite(xPos - fighter.getWidth() / 2, yPos - fighter.getHeight() / 2,
						resources.fighterRightTextureRegion, resources.vbom);
		fighterBody =
				createTriangleBody(resources.physicsWorld, fighter, BodyType.DynamicBody,
						PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));

		resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(fighter, fighterBody, true, true));

	}

	public void attachTo(Scene scene) {

		scene.attachChild(fighter);
		scene.attachChild(mainThrust);
		scene.attachChild(leftThrust);
		scene.attachChild(rightThrust);
	}

	private Body createTriangleBody(final PhysicsWorld physicsWorld, final IAreaShape areaShape,
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

	public Sprite getFighter() {
		return fighter;
	}

	public Sprite getMainThrust() {
		return mainThrust;
	}

	public Sprite getLeftThrust() {
		return leftThrust;
	}

	public Sprite getRightThrust() {
		return rightThrust;
	}

	public Body getFighterBody() {
		return fighterBody;
	}

	public void setFighter(Sprite fighter) {
		this.fighter = fighter;
	}

	public void setMainThrust(Sprite mainThrust) {
		this.mainThrust = mainThrust;
	}

	public void setLeftThrust(Sprite leftThrust) {
		this.leftThrust = leftThrust;
	}

	public void setRightThrust(Sprite rightThrust) {
		this.rightThrust = rightThrust;
	}

	public void setFighterBody(Body fighterBody) {
		this.fighterBody = fighterBody;
	}
}
