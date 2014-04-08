package se.bengtsson.thegame.activities;

import se.bengtsson.thegame.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TitleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("TitleActivity", "Activity created");
		setContentView(R.layout.activity_title);
	}

}
