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

public class ShareWithContactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ShareWithContactActivity", "Creating activity");
		setContentView(R.layout.activity_share_with_contact);
	}

}
