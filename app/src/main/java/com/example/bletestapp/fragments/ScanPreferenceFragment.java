package com.example.bletestapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;

import com.example.bletestapp.MainActivity;
import com.example.bletestapp.R;

public class ScanPreferenceFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		addPreferencesFromResource(R.xml.scan_preference);
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view.setBackgroundColor(getResources().getColor(R.color.white, requireActivity().getTheme()));

		setHasOptionsMenu(false);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		menu.clear();
	}

	public static void newInstance(FragmentManager fragmentManager) {
		ScanPreferenceFragment fragment = new ScanPreferenceFragment();
		fragmentManager.beginTransaction()
				.add(R.id.main_layout, fragment)
				.addToBackStack(null)
				.commit();
	}
}
