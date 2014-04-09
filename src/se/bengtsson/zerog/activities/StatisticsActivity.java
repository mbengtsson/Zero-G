package se.bengtsson.zerog.activities;

import se.bengtsson.zerog.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StatisticsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("StatisticsActivity", "Activity created");
		setContentView(R.layout.activity_statistics);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		Log.d("StatisticsActivity", "Returning to MainActivity");
		startActivity(intent);
	}

}
