package se.bengtsson.zerog.game.objects.pools;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.util.adt.pool.GenericPool;

import se.bengtsson.zerog.game.manager.ResourceManager;
import se.bengtsson.zerog.game.objects.pools.BulletPool.Bullet;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class BulletPool extends GenericPool<Bullet> {

	private ResourceManager resources;
	private Entity spriteLayer;

	public BulletPool(Entity spriteLayer) {
		this.resources = ResourceManager.getInstance();
		this.spriteLayer = spriteLayer;
	}

	@Override
	protected Bullet onAllocatePoolItem() {

		return new Bullet(0, 0, 0);
	}

	public synchronized Bullet obtainPoolItem(float pX, float pY, float rotation) {
		Bullet bullet = super.obtainPoolItem();
		bullet.body.setActive(true);
		bullet.setVisible(true);
		bullet.setIgnoreUpdate(false);
		bullet.activate(pX, pY, rotation);
		return bullet;
	}

	@Override
	protected void onHandleRecycleItem(final Bullet bullet) {
		super.onHandleRecycleItem(bullet);
		resources.engine.runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				bullet.body.setActive(false);
				bullet.setVisible(false);
				bullet.setIgnoreUpdate(true);
				bullet.clearEntityModifiers();
				bullet.clearUpdateHandlers();

			}

		});

	}

	public class Bullet extends Sprite {

		private final float VELOCITY = 20f;

		private final float WORLD_WIDTH;
		private final float WORLD_HEIGHT;

		private Body body;

		public Bullet(float pX, float pY, float rotation) {
			super(pX, pY, resources.bulletTextureRegion, resources.vbom);

			body =
					PhysicsFactory.createBoxBody(resources.physicsWorld, this, BodyType.DynamicBody,
							PhysicsFactory.createFixtureDef(0.5f, 0.0f, 0.0f));
			resources.physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
			body.setUserData(this);

			WORLD_WIDTH = resources.camera.getWidth();
			WORLD_HEIGHT = resources.camera.getHeight();

			spriteLayer.attachChild(this);

			activate(pX, pY, rotation);

		}

		private void activate(float pX, float pY, float rotation) {
			float velocityX = (float) (Math.sin(rotation) * VELOCITY);
			float velocityY = (float) -(Math.cos(rotation) * VELOCITY);
			body.setLinearVelocity(velocityX, velocityY);
			body.setAngularVelocity(0f);
			body.setTransform(pX, pY, rotation);
		}

		@Override
		protected void onManagedUpdate(float pSecondsElapsed) {
			if (body.getPosition().x < 0) {
				recyclePoolItem(this);
			} else if (body.getPosition().x > WORLD_WIDTH) {
				recyclePoolItem(this);
			}

			if (body.getPosition().y < 0) {
				recyclePoolItem(this);
			} else if (body.getPosition().x > WORLD_HEIGHT) {
				recyclePoolItem(this);
			}
			super.onManagedUpdate(pSecondsElapsed);
		}

	}

}
