package se.bengtsson.thegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	public void singlePlayerClick(View view) {
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}

	public void multiPlayerClick(View view) {
		Intent intent = new Intent(this, MultiPlayerActivity.class);
		startActivity(intent);
	}

}
