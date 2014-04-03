package se.bengtsson.thegame.activities;

import se.bengtsson.thegame.R;
import se.bengtsson.thegame.R.layout;
import android.app.Activity;
import android.os.Bundle;

public class ShareWithContactActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_with_contact);
		getActionBar().hide();

	}

}
