package se.bengtsson.zerog.fragments;

import se.bengtsson.zerog.R;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TitleFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("TitleFragment", "Fragment created");

		View view = inflater.inflate(R.layout.fragment_title, container, false);

		return view;
	}

}
