package se.bengtsson.zerog.activities;

import se.bengtsson.zerog.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class MultiPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MultiPlayerActivity", "Creating activity");
		setContentView(R.layout.activity_multi_player);
	}

}
