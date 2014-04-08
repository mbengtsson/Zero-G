package se.bengtsson.thegame.game.manager;

import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import se.bengtsson.thegame.activities.game.GameActivity;
import android.graphics.Color;
import android.util.Log;

public class ResourceManager {

	private static ResourceManager INSTANCE;

	public PhysicsWorld physicsWorld;
	public Engine engine;
	public GameActivity activity;
	public Camera camera;
	public VertexBufferObjectManager vbom;

	public ITextureRegion backgroundTextureRegion;
	public ITextureRegion redFighterTextureRegion;
	public ITextureRegion blueFighterTextureRegion;
	public ITextureRegion fighterLeftTextureRegion;
	public ITextureRegion fighterRightTextureRegion;
	public ITextureRegion fighterThrustTextureRegion;
	public ITextureRegion triggerTextureRegion;
	public ITextureRegion bulletTextureRegion;
	public TiledTextureRegion explosionTextureRegion;

	public Font smallFont;
	public Font messageFont;

	public Sound engineSound;
	public Sound firingSound;
	public Sound hitSound;
	public Sound explosionSound;

	public Music backgroundLoop;

	private ITexture backgroundTexture;
	private ITexture redFighterTexture;
	private ITexture blueFighterTexture;
	private ITexture fighterLeftTexture;
	private ITexture fighterRightTexture;
	private ITexture fighterThrustTexture;
	private ITexture triggerTexture;
	private ITexture bulletTexture;
	private BuildableBitmapTextureAtlas explosionTextureAtlas;

	private ITexture smallFontTexture;
	private ITexture messageFontTexture;

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

		backgroundTexture =
				new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/background.png");
		backgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
		backgroundTexture.load();

		redFighterTexture =
				new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/fighter_red.png");
		redFighterTextureRegion = TextureRegionFactory.extractFromTexture(redFighterTexture);
		redFighterTexture.load();

		blueFighterTexture =
				new AssetBitmapTexture(activity.getTextureManager(), activity.getAssets(), "gfx/fighter_blue.png");
		blueFighterTextureRegion = TextureRegionFactory.extractFromTexture(blueFighterTexture);
		blueFighterTexture.load();

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

		explosionTextureAtlas =
				new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1024, 1280, TextureOptions.NEAREST);

		explosionTextureRegion =
				BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(explosionTextureAtlas,
						activity.getAssets(), "gfx/explosion.png", 4, 5);
		try {
			explosionTextureAtlas
					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			explosionTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Log.e("ResourceManager", "Failed building texture atlas");
		}
	}

	public void loadFonts() {

		smallFontTexture =
				new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		smallFont =
				FontFactory.createFromAsset(activity.getFontManager(), smallFontTexture, activity.getAssets(),
						"fonts/RationalInteger.ttf", 18, true, Color.WHITE);
		smallFont.load();

		messageFontTexture =
				new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		messageFont =
				FontFactory.createFromAsset(activity.getFontManager(), messageFontTexture, activity.getAssets(),
						"fonts/RationalInteger.ttf", 50, true, Color.YELLOW);
		messageFont.load();

	}

	public void loadSounds() {
		try {
			engineSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), activity, "snd/rocket.ogg");
			engineSound.setLooping(true);
		} catch (IOException e) {
			Log.e("ResourceManager", "Failed loading rocket sound");
		}

		try {
			firingSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), activity, "snd/firing.ogg");
		} catch (IOException e) {
			Log.e("ResourceManager", "Failed loading firing sound");
		}

		try {
			hitSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), activity, "snd/ricochet.ogg");
		} catch (IOException e) {
			Log.e("ResourceManager", "Failed loading hit sound");
		}

		try {
			explosionSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), activity, "snd/explosion.ogg");
		} catch (IOException e) {
			Log.e("ResourceManager", "Failed loading explosion sound");
		}

		try {
			backgroundLoop =
					MusicFactory.createMusicFromAsset(engine.getMusicManager(), activity, "snd/background.ogg");
			backgroundLoop.setLooping(true);
		} catch (IOException e) {
			Log.e("ResourceManager", "Failed loading background loop");
		}
	}

}
