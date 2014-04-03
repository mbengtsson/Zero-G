package se.bengtsson.thegame.activities;

import se.bengtsson.thegame.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StatisticsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
