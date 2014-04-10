package se.bengtsson.zerog.fragments;

import se.bengtsson.zerog.R;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author Marcus Bengtsson
 * 
 */

public class StatisticsFragment extends Fragment {

	private SharedPreferences statistics;
	private View view;

	private boolean debriefing;
	private boolean winner;
	private boolean multiPlayer;

	private int bulletsFired;
	private int hits;
	private float hitRatio;

	private int totalWinsMp;
	private int totalLossesMp;
	private int totalBulletsFiredMp;
	private int totalHitsMp;
	private float winRatioMp;
	private float totalHitRatioMp;

	private int totalWinsSp;
	private int totalLossesSp;
	private int totalBulletsFiredSp;
	private int totalHitsSp;
	private float winRatioSp;
	private float totalHitRatioSp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("StatisticsFragment", "Creating fragment");

		view = inflater.inflate(R.layout.fragment_statistics, container, false);

		statistics = PreferenceManager.getDefaultSharedPreferences(getActivity());

		debriefing = getActivity().getIntent().getBooleanExtra("debriefing", false);
		winner = getActivity().getIntent().getBooleanExtra("isWinner", false);
		multiPlayer = getActivity().getIntent().getBooleanExtra("multiPlayer", false);
		bulletsFired = getActivity().getIntent().getIntExtra("bulletsFired", 0);
		hits = getActivity().getIntent().getIntExtra("hits", 0);

		if (debriefing) {
			if (multiPlayer) {
				readMultiPlayerStatisticsFromSharedPreferences();
				updateMultiPlayerStatistics();
				writeMultiPlayerStatisticsToSharedPreferences();
				calculateMultiPlayerRatios();
			} else {
				readSinglePlayerStatisticsFromSharedPreferences();
				updateSinglePlayerStatistics();
				writeSinglePlayerStatisticsToSharedPreferences();
				calculateSinglePlayerRatios();
			}
			hitRatio = calculateRatio(hits, bulletsFired);
		} else {
			readMultiPlayerStatisticsFromSharedPreferences();
			readSinglePlayerStatisticsFromSharedPreferences();
			calculateMultiPlayerRatios();
			calculateSinglePlayerRatios();
		}

		displayStatistics();

		return view;
	}

	private void readMultiPlayerStatisticsFromSharedPreferences() {
		Log.d("StatisticsFragment", "Reading multi-player statistics");
		totalWinsMp = statistics.getInt("totalWinsMp", 0);
		totalLossesMp = statistics.getInt("totalLossesMp", 0);
		totalBulletsFiredMp = statistics.getInt("totalBulletsFiredMp", 0);
		totalHitsMp = statistics.getInt("totalHitsMp", 0);
	}

	private void readSinglePlayerStatisticsFromSharedPreferences() {
		Log.d("StatisticsFragment", "Reading single-player statistics");
		totalWinsSp = statistics.getInt("totalWinsSp", 0);
		totalLossesSp = statistics.getInt("totalLossesSp", 0);
		totalBulletsFiredSp = statistics.getInt("totalBulletsFiredSp", 0);
		totalHitsSp = statistics.getInt("totalHitsSp", 0);
	}

	private void writeMultiPlayerStatisticsToSharedPreferences() {
		Log.d("StatisticsFragment", "Writing multi-player statistics");
		SharedPreferences.Editor editor = statistics.edit();
		editor.putInt("totalWinsMp", totalWinsMp);
		editor.putInt("totalLossesMp", totalLossesMp);
		editor.putInt("totalBulletsFiredMp", totalBulletsFiredMp);
		editor.putInt("totalHitsMp", totalHitsMp);
		editor.commit();
	}

	private void writeSinglePlayerStatisticsToSharedPreferences() {
		Log.d("StatisticsFragment", "Writing single-player statistics");
		SharedPreferences.Editor editor = statistics.edit();
		editor.putInt("totalWinsSp", totalWinsSp);
		editor.putInt("totalLossesSp", totalLossesSp);
		editor.putInt("totalBulletsFiredSp", totalBulletsFiredSp);
		editor.putInt("totalHitsSp", totalHitsSp);
		editor.commit();
	}

	private void updateMultiPlayerStatistics() {
		Log.d("StatisticsFragment", "Updating multi-player statistics");
		if (winner) {
			totalWinsMp++;
		} else {
			totalLossesMp++;
		}

		totalBulletsFiredMp += bulletsFired;
		totalHitsMp += hits;
	}

	private void updateSinglePlayerStatistics() {
		Log.d("StatisticsFragment", "Updating single-player statistics");
		if (winner) {
			totalWinsSp++;
		} else {
			totalLossesSp++;
		}

		totalBulletsFiredSp += bulletsFired;
		totalHitsSp += hits;
	}

	private void calculateMultiPlayerRatios() {
		Log.d("StatisticsFragment", "Calculating multi-player ratios");
		winRatioMp = calculateRatio(totalWinsMp, totalLossesMp + totalWinsMp);
		totalHitRatioMp = calculateRatio(totalHitsMp, totalBulletsFiredMp);
	}

	private void calculateSinglePlayerRatios() {
		Log.d("StatisticsFragment", "Calculating Single-player ratios");
		winRatioSp = calculateRatio(totalWinsSp, totalLossesSp + totalWinsSp);
		totalHitRatioSp = calculateRatio(totalHitsSp, totalBulletsFiredSp);
	}

	private float calculateRatio(int firstValue, int secondValue) {
		if (secondValue > 0) {
			return (float) 100 * firstValue / secondValue;
		} else {
			return 0.0f;
		}
	}

	private void displayStatistics() {
		Log.d("StatisticsFragment", "Displaying statistics");

		TextView greetingText = (TextView) view.findViewById(R.id.post_fight_greeting);
		TextView bulletsFiredText = (TextView) view.findViewById(R.id.bullets_fired);
		TextView hitsText = (TextView) view.findViewById(R.id.hits);
		TextView hitRatioText = (TextView) view.findViewById(R.id.hit_ratio);

		if (debriefing) {
			greetingText.setText(winner ? getActivity().getString(R.string.victory_message) : getActivity().getString(
					R.string.defeat_message));
			bulletsFiredText.setText(String.format("%s%d", getText(R.string.bullets_fired), bulletsFired));
			hitsText.setText(String.format("%s%d", getText(R.string.hits), hits));
			hitRatioText.setText(String.format("%s%.1f%%", getText(R.string.hit_ratio), hitRatio));

			if (multiPlayer) {
				displayMultiPlayerStatistics();
			} else {
				displaySinglePlayerStatistics();
			}

		} else {
			view.findViewById(R.id.statistics_title).setVisibility(View.GONE);
			greetingText.setVisibility(View.GONE);
			bulletsFiredText.setVisibility(View.GONE);
			hitsText.setVisibility(View.GONE);
			hitRatioText.setVisibility(View.GONE);

			displayMultiPlayerStatistics();
			displaySinglePlayerStatistics();
		}

	}

	private void displayMultiPlayerStatistics() {
		Log.d("StatisticsFragment", "Displaying multi-player statistics");

		TextView totalWinsTextMp = (TextView) view.findViewById(R.id.total_winns_mp);
		TextView totalLossesTextMp = (TextView) view.findViewById(R.id.total_losses_mp);
		TextView winRatioTextMp = (TextView) view.findViewById(R.id.win_ratio_mp);
		TextView totalBulletsFiredTextMp = (TextView) view.findViewById(R.id.total_bullets_fired_mp);
		TextView totalHitsTextMp = (TextView) view.findViewById(R.id.total_hits_mp);
		TextView totalHitRatioTextMp = (TextView) view.findViewById(R.id.total_hit_ratio_mp);

		totalWinsTextMp.setText(String.format("%s%d", getText(R.string.total_wins), totalWinsMp));
		totalLossesTextMp.setText(String.format("%s%d", getText(R.string.total_losses), totalLossesMp));
		winRatioTextMp.setText(String.format("%s%.1f%%", getText(R.string.win_ratio), winRatioMp));
		totalBulletsFiredTextMp.setText(String.format("%s%d", getText(R.string.total_bullets_fired),
				totalBulletsFiredMp));
		totalHitsTextMp.setText(String.format("%s%d", getText(R.string.total_hits), totalHitsMp));
		totalHitRatioTextMp.setText(String.format("%s%.1f%%", getText(R.string.total_hit_ratio), totalHitRatioMp));

		if (debriefing) {
			view.findViewById(R.id.total_statistics_sp_title).setVisibility(View.GONE);
			view.findViewById(R.id.total_winns_sp).setVisibility(View.GONE);
			view.findViewById(R.id.total_losses_sp).setVisibility(View.GONE);
			view.findViewById(R.id.win_ratio_sp).setVisibility(View.GONE);
			view.findViewById(R.id.total_bullets_fired_sp).setVisibility(View.GONE);
			view.findViewById(R.id.total_hits_sp).setVisibility(View.GONE);
			view.findViewById(R.id.total_hit_ratio_sp).setVisibility(View.GONE);
		}
	}

	private void displaySinglePlayerStatistics() {
		Log.d("StatisticsFragment", "Displaying single-player statistics");

		TextView totalWinsTextSp = (TextView) view.findViewById(R.id.total_winns_sp);
		TextView totalLossesTextSp = (TextView) view.findViewById(R.id.total_losses_sp);
		TextView winRatioTextSp = (TextView) view.findViewById(R.id.win_ratio_sp);
		TextView totalBulletsFiredTextSp = (TextView) view.findViewById(R.id.total_bullets_fired_sp);
		TextView totalHitsTextSp = (TextView) view.findViewById(R.id.total_hits_sp);
		TextView totalHitRatioTextSp = (TextView) view.findViewById(R.id.total_hit_ratio_sp);

		totalWinsTextSp.setText(String.format("%s%d", getText(R.string.total_wins), totalWinsSp));
		totalLossesTextSp.setText(String.format("%s%d", getText(R.string.total_losses), totalLossesSp));
		winRatioTextSp.setText(String.format("%s%.1f%%", getText(R.string.win_ratio), winRatioSp));
		totalBulletsFiredTextSp.setText(String.format("%s%d", getText(R.string.total_bullets_fired),
				totalBulletsFiredSp));
		totalHitsTextSp.setText(String.format("%s%d", getText(R.string.total_hits), totalHitsSp));
		totalHitRatioTextSp.setText(String.format("%s%.1f%%", getText(R.string.total_hit_ratio), totalHitRatioSp));

		if (debriefing) {
			view.findViewById(R.id.total_statistics_mp_title).setVisibility(View.GONE);
			view.findViewById(R.id.total_winns_mp).setVisibility(View.GONE);
			view.findViewById(R.id.total_losses_mp).setVisibility(View.GONE);
			view.findViewById(R.id.win_ratio_mp).setVisibility(View.GONE);
			view.findViewById(R.id.total_bullets_fired_mp).setVisibility(View.GONE);
			view.findViewById(R.id.total_hits_mp).setVisibility(View.GONE);
			view.findViewById(R.id.total_hit_ratio_mp).setVisibility(View.GONE);
		}
	}

}
