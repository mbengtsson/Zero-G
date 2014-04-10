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
		Log.d("StatisticsActivity", "Creating activity");
		setContentView(R.layout.activity_statistics);
	}

	@Override
	public void onBackPressed() {
		Log.d("StatisticsActivity", "Returning to MainActivity");
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

}
