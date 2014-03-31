package se.bengtsson.thegame.game.hud;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.andengine.util.color.Color;

import se.bengtsson.thegame.game.controller.PlayerController;
import se.bengtsson.thegame.game.manager.ResourceManager;

public class PlayerHUD extends HUD {

	private final int HEALTH_BAR_WIDTH = 300;
	private final int HEALTH_BAR_HEIGHT = 5;
	private final float CAMERA_WIDTH;
	private final float CAMERA_HEIGHT;

	private ResourceManager resources;
	private PlayerController playerController;
	private Rectangle playerHealthBar;
	private Rectangle enemyHealthBar;

	public PlayerHUD(PlayerController playerController) {
		this.playerController = playerController;
		this.resources = ResourceManager.getInstance();
		this.CAMERA_WIDTH = resources.camera.getWidth();
		this.CAMERA_HEIGHT = resources.camera.getHeight();

		attachController();
		createPlayerHealthBar();
		createEnemyHealthBar();

	}

	public void decreasePlayerHealth(int health) {
		playerHealthBar.setWidth(playerHealthBar.getWidth() - health);
		if (playerHealthBar.getWidth() < 0) {
			playerHealthBar.setWidth(0);
		}

	}

	public void decreaseEnemyHealth(int health) {
		enemyHealthBar.setWidth(enemyHealthBar.getWidth() - health);
		if (enemyHealthBar.getWidth() < 0) {
			enemyHealthBar.setWidth(0);
		}
		enemyHealthBar.setX(CAMERA_WIDTH - enemyHealthBar.getWidth() - 5);
	}

	public void setPlayerHealth(int health) {
		playerHealthBar.setWidth(health * 3);
	}

	public void setEnemyHealth(int health) {
		enemyHealthBar.setWidth(health * 3);
		enemyHealthBar.setX(CAMERA_WIDTH - enemyHealthBar.getWidth() - 5);
	}

	public void showMessage(boolean winner) {

		Text message;

		if (winner) {
			message = new Text(150, 100, resources.messageFont, "VICTORY!!", resources.vbom);
		} else {
			message = new Text(150, 100, resources.messageFont, "DEFEAT!!", resources.vbom);
		}

		attachChild(message);

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

		Text text = new Text(10, 10, resources.smallFont, "Player health", resources.vbom);
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

		Text text = new Text(CAMERA_WIDTH - 109, 10, resources.smallFont, "Enemy health", resources.vbom);
		text.setAlpha(0.5f);
		attachChild(text);
	}

}
