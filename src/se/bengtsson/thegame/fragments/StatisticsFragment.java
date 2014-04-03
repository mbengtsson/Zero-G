package se.bengtsson.thegame.fragments;

import se.bengtsson.thegame.R;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatisticsFragment extends Fragment {

	private SharedPreferences statistics;
	private View view;

	private boolean debriefing;

	private boolean winner;
	private int bulletsFired;
	private int hits;
	private float hitRatio;

	private int totalWins;
	private int totalLosses;
	private int totalBulletsFired;
	private int totalHits;
	private float winRatio;
	private float totalHitRatio;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_statistics, container, false);

		statistics = PreferenceManager.getDefaultSharedPreferences(getActivity());

		debriefing = getActivity().getIntent().getBooleanExtra("debriefing", false);

		winner = getActivity().getIntent().getBooleanExtra("isWinner", false);
		bulletsFired = getActivity().getIntent().getIntExtra("bulletsFired", 0);
		hits = getActivity().getIntent().getIntExtra("hits", 0);

		readStatisticsFromSharedPreferences();

		if (debriefing) {
			updateStatistics();
			writeStatisticsToSharedPreferences();

			hitRatio = calculateRatio(hits, bulletsFired);
		}

		winRatio = calculateRatio(totalWins, totalLosses + totalWins);
		totalHitRatio = calculateRatio(totalHits, totalBulletsFired);

		printStatistics();

		return view;
	}

	public void readStatisticsFromSharedPreferences() {
		totalWins = statistics.getInt("totalWins", 0);
		totalLosses = statistics.getInt("totalLosses", 0);
		totalBulletsFired = statistics.getInt("totalBulletsFired", 0);
		totalHits = statistics.getInt("totalHits", 0);
	}

	public void writeStatisticsToSharedPreferences() {
		SharedPreferences.Editor editor = statistics.edit();
		editor.putInt("totalWins", totalWins);
		editor.putInt("totalLosses", totalLosses);
		editor.putInt("totalBulletsFired", totalBulletsFired);
		editor.putInt("totalHits", totalHits);
		editor.commit();
	}

	public void updateStatistics() {
		if (winner) {
			totalWins++;
		} else {
			totalLosses++;
		}

		totalBulletsFired += bulletsFired;
		totalHits += hits;
	}

	public float calculateRatio(int firstValue, int secondValue) {
		if (secondValue > 0) {
			return (float) 100 * firstValue / secondValue;
		} else {
			return 0.0f;
		}
	}

	public void printStatistics() {

		TextView greetingText = (TextView) view.findViewById(R.id.post_fight_greeting);
		TextView bulletsFiredText = (TextView) view.findViewById(R.id.bullets_fired);
		TextView hitsText = (TextView) view.findViewById(R.id.hits);
		TextView hitRatioText = (TextView) view.findViewById(R.id.hit_ratio);

		if (debriefing) {
			greetingText.setText(winner ? "VICTORY" : "DEFEAT");
			bulletsFiredText.setText(String.format("%s%d", getText(R.string.bullets_fired), bulletsFired));
			hitsText.setText(String.format("%s%d", getText(R.string.hits), hits));
			hitRatioText.setText(String.format("%s%.1f%%", getText(R.string.hit_ratio), hitRatio));
		} else {
			view.findViewById(R.id.statistics_title).setVisibility(View.GONE);
			greetingText.setVisibility(View.GONE);
			bulletsFiredText.setVisibility(View.GONE);
			hitsText.setVisibility(View.GONE);
			hitRatioText.setVisibility(View.GONE);
		}

		TextView totalWinsText = (TextView) view.findViewById(R.id.total_winns);
		TextView totalLossesText = (TextView) view.findViewById(R.id.total_losses);
		TextView winRatioText = (TextView) view.findViewById(R.id.win_ratio);
		TextView totalBulletsFiredText = (TextView) view.findViewById(R.id.total_bullets_fired);
		TextView totalHitsText = (TextView) view.findViewById(R.id.total_hits);
		TextView totalHitRatioText = (TextView) view.findViewById(R.id.total_hit_ratio);

		totalWinsText.setText(String.format("%s%d", getText(R.string.total_wins), totalWins));
		totalLossesText.setText(String.format("%s%d", getText(R.string.total_losses), totalLosses));
		winRatioText.setText(String.format("%s%.1f%%", getText(R.string.win_ratio), winRatio));
		totalBulletsFiredText.setText(String.format("%s%d", getText(R.string.total_bullets_fired), totalBulletsFired));
		totalHitsText.setText(String.format("%s%d", getText(R.string.total_hits), totalHits));
		totalHitRatioText.setText(String.format("%s%.1f%%", getText(R.string.total_hit_ratio), totalHitRatio));
	}

}
