package se.bengtsson.thegame.game.objects.fighter.factories;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;

import se.bengtsson.thegame.game.manager.ResourceManager;
import se.bengtsson.thegame.game.objects.fighter.Fighter;
import android.util.Log;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class BulletsFactory {

	ResourceManager resources;
	Fighter fighter;

	public BulletsFactory(Fighter fighter) {
		resources = ResourceManager.getInstance();
		this.fighter = fighter;
	}

	public Bullet createBullet(float xPos, float yPos, float rotation) {
		Log.d("BulletFactory", "Create bullet");
		return new Bullet(xPos, yPos, rotation);
	}

	public class Bullet extends Sprite {

		private final float VELOCITY = 15f;

		private final float WORLD_WIDTH;
		private final float WORLD_HEIGHT;

		private Body body;
		private float velocityX;
		private float velocityY;

		public Bullet(float pX, float pY, float rotation) {
			super(pX, pY, resources.bulletTextureRegion, resources.vbom);

			// this.rotation = rotation;

			body =
					PhysicsFactory.createBoxBody(resources.physicsWorld, this, BodyType.DynamicBody,
							PhysicsFactory.createFixtureDef(1.0f, 0.0f, 0.0f));
			resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));

			WORLD_WIDTH = resources.camera.getWidth();
			WORLD_HEIGHT = resources.camera.getHeight();
			velocityX = (float) (Math.sin(rotation) * VELOCITY);
			velocityY = (float) -(Math.cos(rotation) * VELOCITY);
			body.setLinearVelocity(velocityX, velocityY);
			body.setTransform(pX, pY, rotation);
		}

		@Override
		protected void onManagedUpdate(float pSecondsElapsed) {
			if (body.getPosition().x < 0) {
				destroy(this);
			} else if (body.getPosition().x > WORLD_WIDTH) {
				destroy(this);
			}

			if (body.getPosition().y < 0) {
				destroy(this);
			} else if (body.getPosition().x > WORLD_HEIGHT) {
				destroy(this);
			}
			super.onManagedUpdate(pSecondsElapsed);
		}

		public void destroy(final Bullet bullet) {
			resources.engine.runOnUpdateThread(new Runnable() {

				@Override
				public void run() {

					final Body body = bullet.body;
					resources.physicsWorld.unregisterPhysicsConnector(resources.physicsWorld
							.getPhysicsConnectorManager().findPhysicsConnectorByShape(bullet));
					body.setActive(false);
					resources.physicsWorld.destroyBody(body);
					fighter.detachChild(bullet);

					System.gc();

					Log.d("Bullet", "Destroyed");
				}
			});
		}
	}

}
