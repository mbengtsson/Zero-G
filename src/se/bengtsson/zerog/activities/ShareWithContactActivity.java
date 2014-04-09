package se.bengtsson.zerog.activities;

import se.bengtsson.zerog.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ShareWithContactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ShareWithContactActivity", "Activity created");
		setContentView(R.layout.activity_share_with_contact);
	}

}
