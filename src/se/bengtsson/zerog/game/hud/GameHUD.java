package se.bengtsson.zerog.game.hud;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.util.color.Color;

import se.bengtsson.zerog.R;
import se.bengtsson.zerog.game.controller.PlayerController;
import se.bengtsson.zerog.game.manager.ResourceManager;
import android.util.Log;

public class GameHUD extends HUD {

	private final int HEALTH_BAR_WIDTH = 300;
	private final int HEALTH_BAR_HEIGHT = 5;
	private final float CAMERA_WIDTH;
	private final float CAMERA_HEIGHT;

	private ResourceManager resources;
	private PlayerController playerController;
	private Rectangle playerHealthBar;
	private Rectangle enemyHealthBar;

	public GameHUD(PlayerController playerController) {
		Log.d("GameHUD", "Creating HUD");

		this.playerController = playerController;
		this.resources = ResourceManager.getInstance();
		this.CAMERA_WIDTH = resources.camera.getWidth();
		this.CAMERA_HEIGHT = resources.camera.getHeight();

		attachController();
		createPlayerHealthBar();
		createEnemyHealthBar();

	}

	public void setPlayerHealth(int health) {
		playerHealthBar.setWidth(health * 3);
		changeColour(playerHealthBar);

	}

	public void setEnemyHealth(int health) {
		enemyHealthBar.setWidth(health * 3);
		enemyHealthBar.setX(CAMERA_WIDTH - enemyHealthBar.getWidth() - 5);
		changeColour(enemyHealthBar);
	}

	public void changeColour(Rectangle healthBar) {
		if (healthBar.getWidth() <= HEALTH_BAR_WIDTH / 4) {
			healthBar.setColor(Color.RED);
			healthBar.setAlpha(0.5f);
		} else if (healthBar.getWidth() <= HEALTH_BAR_WIDTH / 2) {
			healthBar.setColor(Color.YELLOW);
			healthBar.setAlpha(0.5f);
		}
	}

	public void showGameOverMessage(boolean winner) {

		String message =
				winner ? resources.activity.getString(R.string.victory_message) : resources.activity
						.getString(R.string.defeat_message);

		attachChild(new Text((CAMERA_WIDTH / 2) - 70, (CAMERA_HEIGHT / 2) - 25, resources.messageFont, message,
				resources.vbom));
	}

	private void attachController() {
		attachChild(playerController.getLeftTrigger());
		attachChild(playerController.getRightTrigger());

		registerTouchArea(playerController.getLeftTrigger());
		registerTouchArea(playerController.getRightTrigger());

	}

	private void createPlayerHealthBar() {
		playerHealthBar = new Rectangle(5, 2, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT, resources.vbom);
		playerHealthBar.setColor(Color.GREEN);
		playerHealthBar.setAlpha(0.5f);
		playerHealthBar.setVisible(true);
		attachChild(playerHealthBar);

		Text text =
				new Text(10, 10, resources.smallFont, resources.activity.getString(R.string.player_health),
						resources.vbom);
		text.setAlpha(0.5f);
		attachChild(text);
	}

	private void createEnemyHealthBar() {
		enemyHealthBar =
				new Rectangle(CAMERA_WIDTH - HEALTH_BAR_WIDTH - 5, 2, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT,
						resources.vbom);
		enemyHealthBar.setColor(Color.GREEN);
		enemyHealthBar.setAlpha(0.5f);
		enemyHealthBar.setVisible(true);
		attachChild(enemyHealthBar);

		Text text =
				new Text(CAMERA_WIDTH - 109, 10, resources.smallFont,
						resources.activity.getString(R.string.enemy_health), resources.vbom);
		text.setAlpha(0.5f);
		attachChild(text);
	}

}
