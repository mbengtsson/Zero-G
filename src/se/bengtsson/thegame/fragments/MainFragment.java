package se.bengtsson.thegame.fragments;

import se.bengtsson.thegame.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_main, container, false);

		// view.findViewById(R.id.single_player_button).setOnClickListener(this);
		// view.findViewById(R.id.multi_player_button).setOnClickListener(this);
		// view.findViewById(R.id.share_button).setOnClickListener(this);

		return view;
	}

	// public void singlePlayerClick() {
	// Intent intent = new Intent(getActivity(),
	// SingleplayerGameActivity.class);
	// startActivity(intent);
	// }
	//
	// public void multiPlayerClick() {
	// Intent intent = new Intent(getActivity(), MultiPlayerActivity.class);
	// startActivity(intent);
	// }
	//
	// public void shareClick() {
	// Intent intent = new Intent(getActivity(),
	// ShareWithContactActivity.class);
	// startActivity(intent);
	// }
	//
	// @Override
	// public void onClick(View v) {
	// switch (v.getId()) {
	// case R.id.single_player_button:
	// singlePlayerClick();
	// break;
	// case R.id.multi_player_button:
	// multiPlayerClick();
	// break;
	// case R.id.share_button:
	// shareClick();
	// break;
	// default:
	// Log.e("MainFragment", "No valid button clicked");
	// break;
	// }
	//
	// }

}
