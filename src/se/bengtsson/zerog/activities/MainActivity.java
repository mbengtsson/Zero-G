package se.bengtsson.zerog.activities;

import se.bengtsson.zerog.R;
import se.bengtsson.zerog.activities.game.SingleplayerGameActivity;
import se.bengtsson.zerog.fragments.MainFragment;
import se.bengtsson.zerog.fragments.MultiPlayerFragment;
import se.bengtsson.zerog.fragments.ShareWithContactFragment;
import se.bengtsson.zerog.fragments.StatisticsFragment;
import se.bengtsson.zerog.fragments.TitleFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

	private FragmentManager fragmentManager;
	private boolean tablet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MainActivity", "Activity created");
		setContentView(R.layout.activity_main);

		fragmentManager = getFragmentManager();

		if (findViewById(R.id.side_fragment_container) != null) {
			tablet = true;

			if (savedInstanceState != null) {
				return;
			}
			MainFragment mainFragment = new MainFragment();
			TitleFragment logoFragment = new TitleFragment();

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.menu_fragment_container, mainFragment);
			transaction.add(R.id.side_fragment_container, logoFragment);
			transaction.commit();
		}

	}

	public void titleClick(View view) {
		Log.d("MainActivity", "Title clicked");
		TitleFragment titleFragment = new TitleFragment();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (tablet) {
			transaction.replace(R.id.side_fragment_container, titleFragment);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			Intent intent = new Intent(this, TitleActivity.class);
			startActivity(intent);
		}
	}

	public void singlePlayerClick(View view) {
		Log.d("MainActivity", "Single-player clicked");
		Intent intent = new Intent(this, SingleplayerGameActivity.class);
		startActivity(intent);
	}

	public void multiPlayerClick(View view) {
		Log.d("MainActivity", "Multi-player clicked");

		if (tablet) {
			MultiPlayerFragment multiplayerFragment = new MultiPlayerFragment();

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.side_fragment_container, multiplayerFragment);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			Intent intent = new Intent(this, MultiPlayerActivity.class);
			startActivity(intent);
		}
	}

	public void statisticsClick(View view) {
		Log.d("MainActivity", "Statistics clicked");

		if (tablet) {
			StatisticsFragment statFragmet = new StatisticsFragment();

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.side_fragment_container, statFragmet);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			Intent intent = new Intent(this, StatisticsActivity.class);
			startActivity(intent);
		}
	}

	public void shareClick(View view) {
		Log.d("MainActivity", "Share clicked");

		if (tablet) {
			ShareWithContactFragment shareFragment = new ShareWithContactFragment();

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.side_fragment_container, shareFragment);
			transaction.addToBackStack(null);
			transaction.commit();

		} else {
			Intent intent = new Intent(this, ShareWithContactActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed() {
		Log.d("MainActivity", "Exiting app");
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

}
