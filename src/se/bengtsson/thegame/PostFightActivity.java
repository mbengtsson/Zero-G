package se.bengtsson.thegame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class PostFightActivity extends Activity {

	SharedPreferences statistics;

	private boolean winner;
	private int bulletsFired;
	private int hits;
	private float hitRatio;

	private int totalWins;
	private int totalLosses;
	private int totalBulletsFired;
	private int totalHits;
	private float winRatio;
	private float totalHitRatio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_fight);
		getActionBar().hide();

		statistics = this.getPreferences(MODE_PRIVATE);

		winner = getIntent().getBooleanExtra("isWinner", false);
		bulletsFired = getIntent().getIntExtra("bulletsFired", 0);
		hits = getIntent().getIntExtra("hits", 0);

		readStatisticsFromSharedPreferences();
		updateStatistics();
		writeStatisticsToSharedPreferences();

		hitRatio = calculateRatio(hits, bulletsFired - hits);
		winRatio = calculateRatio(totalWins, totalLosses);
		totalHitRatio = calculateRatio(totalHits, totalBulletsFired - totalHits);

		printStatistics();

	}

	public void readStatisticsFromSharedPreferences() {
		totalWins = statistics.getInt("totalWins", 0);
		totalLosses = statistics.getInt("totalLosses", 0);
		totalBulletsFired = statistics.getInt("totalBulletsFired", 0);
		totalHits = statistics.getInt("totalHits", 0);
	}

	public void writeStatisticsToSharedPreferences() {
		SharedPreferences.Editor editor = statistics.edit();
		editor.putInt("totalWins", totalWins);
		editor.putInt("totalLosses", totalLosses);
		editor.putInt("totalBulletsFired", totalBulletsFired);
		editor.putInt("totalHits", totalHits);
		editor.commit();
	}

	public void updateStatistics() {
		if (winner) {
			totalWins++;
		} else {
			totalLosses++;
		}

		totalBulletsFired += bulletsFired;
		totalHits += hits;
	}

	public float calculateRatio(int firstValue, int secondValue) {
		if (secondValue > 0) {
			return (float) firstValue / secondValue;
		} else {
			return 1000.0f;
		}
	}

	public void printStatistics() {
		TextView greetingText = (TextView) findViewById(R.id.post_fight_greeting);

		TextView bulletsFiredText = (TextView) findViewById(R.id.bullets_fired);
		TextView hitsText = (TextView) findViewById(R.id.hits);
		TextView hitRatioText = (TextView) findViewById(R.id.hit_ratio);

		TextView totalWinsText = (TextView) findViewById(R.id.total_winns);
		TextView totalLossesText = (TextView) findViewById(R.id.total_losses);
		TextView winRatioText = (TextView) findViewById(R.id.win_ratio);
		TextView totalBulletsFiredText = (TextView) findViewById(R.id.total_bullets_fired);
		TextView totalHitsText = (TextView) findViewById(R.id.total_hits);
		TextView totalHitRatioText = (TextView) findViewById(R.id.total_hit_ratio);

		greetingText.setText(winner ? "VICTORY" : "DEFEAT");

		bulletsFiredText.setText(String.format("%s%d", getText(R.string.bullets_fired), bulletsFired));
		hitsText.setText(String.format("%s%d", getText(R.string.hits), hits));
		hitRatioText.setText(String.format("%s%.1f", getText(R.string.hit_ratio), hitRatio));

		totalWinsText.setText(String.format("%s%d", getText(R.string.total_wins), totalWins));
		totalLossesText.setText(String.format("%s%d", getText(R.string.total_losses), totalLosses));
		winRatioText.setText(String.format("%s%.1f", getText(R.string.win_ratio), winRatio));
		totalBulletsFiredText.setText(String.format("%s%d", getText(R.string.total_bullets_fired), totalBulletsFired));
		totalHitsText.setText(String.format("%s%d", getText(R.string.total_hits), totalHits));
		totalHitRatioText.setText(String.format("%s%.1f", getText(R.string.total_hit_ratio), totalHitRatio));
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
