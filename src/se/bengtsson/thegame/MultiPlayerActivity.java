package se.bengtsson.thegame;

import android.app.Activity;
import android.os.Bundle;

public class MultiPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_player);
		getActionBar().hide();
	}

}
