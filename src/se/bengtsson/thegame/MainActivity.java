package se.bengtsson.thegame;

import se.bengtsson.thegame.fragments.LogoFragment;
import se.bengtsson.thegame.fragments.MainFragment;
import se.bengtsson.thegame.fragments.MultiPlayerFragment;
import se.bengtsson.thegame.fragments.ShareWithContactFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	private FragmentManager fragmentManager;
	private boolean tablet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().hide();

		fragmentManager = getFragmentManager();

		if (findViewById(R.id.side_fragment_container) != null) {
			tablet = true;

			if (savedInstanceState != null) {
				return;
			}
			MainFragment mainFragment = new MainFragment();
			LogoFragment logoFragment = new LogoFragment();

			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.menu_fragment_container, mainFragment);
			transaction.add(R.id.side_fragment_container, logoFragment);
			transaction.commit();
		}

	}

	public void singlePlayerClick(View view) {
		Intent intent = new Intent(this, SingleplayerGameActivity.class);
		startActivity(intent);
	}

	public void multiPlayerClick(View view) {

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

	public void shareClick(View view) {

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
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

}
