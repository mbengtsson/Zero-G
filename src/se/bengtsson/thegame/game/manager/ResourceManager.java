package se.bengtsson.thegame.game.manager;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import se.bengtsson.thegame.GameActivity;

public class ResourceManager {

	private static ResourceManager INSTANCE;

	public PhysicsWorld physicsWorld;
	public Engine engine;
	public GameActivity activity;
	public Camera camera;
	public VertexBufferObjectManager vbom;

	private ITexture dummyTexture;
	private ITexture fighterTexture;
	private ITexture fighterLeftTexture;
	private ITexture fighterRightTexture;
	private ITexture fighterThrustTexture;
	private ITexture triggerTexture;
	private ITexture bulletTexture;

	public ITextureRegion dummyTextureRegion;
	public ITextureRegion fighterTextureRegion;
	public ITextureRegion fighterLeftTextureRegion;
	public ITextureRegion fighterRightTextureRegion;
	public ITextureRegion fighterThrustTextureRegion;
	public ITextureRegion triggerTextureRegion;
	public ITextureRegion bulletTextureRegion;

	private ResourceManager() {

	}

	public static ResourceManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ResourceManager();
		}
		return INSTANCE;
	}

	public static void prepareManager(PhysicsWorld physicsWorld, Engine engine, GameActivity activity, Camera camera,
			VertexBufferObjectManager vbom) {

		getInstance().physicsWorld = physicsWorld;
		getInstance().engine = engine;
		getInstance().activity = activity;
		getInstance().camera = camera;
		getInstance().vbom = vbom;
	}

	public void loadTextures() throws IOException {
		dummyTexture = new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/dummy.png");
		dummyTextureRegion = TextureRegionFactory.extractFromTexture(dummyTexture);
		dummyTexture.load();

		fighterTexture = new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/fighter.png");
		fighterTextureRegion = TextureRegionFactory.extractFromTexture(fighterTexture);
		fighterTexture.load();

		fighterLeftTexture = new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/left.png");
		fighterLeftTextureRegion = TextureRegionFactory.extractFromTexture(fighterLeftTexture);
		fighterLeftTexture.load();

		fighterRightTexture =
				new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/right.png");
		fighterRightTextureRegion = TextureRegionFactory.extractFromTexture(fighterRightTexture);
		fighterRightTexture.load();

		fighterThrustTexture =
				new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/thrust.png");
		fighterThrustTextureRegion = TextureRegionFactory.extractFromTexture(fighterThrustTexture);
		fighterThrustTexture.load();

		triggerTexture = new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/trigger.png");
		triggerTextureRegion = TextureRegionFactory.extractFromTexture(triggerTexture);
		triggerTexture.load();

		bulletTexture = new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/bullet.png");
		bulletTextureRegion = TextureRegionFactory.extractFromTexture(bulletTexture);
		bulletTexture.load();
	}

	public void loadFonts() {

	}

	public void loadSounds() {

	}

}
