package se.bengtsson.zerog.activities;

import se.bengtsson.zerog.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TitleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("TitleActivity", "Creating activity");
		setContentView(R.layout.activity_title);
	}

}
