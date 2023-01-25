package com.example.bletestapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bletestapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy;

import java.util.ArrayList;
import java.util.List;

public class DevicesMainFragment extends Fragment {
	private ViewPager2 viewPager2;
	private TabLayout tabLayout;
	private List<Fragment> fragments = new ArrayList<>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.device_main_fragment, container, false);

		fragments.add(new AvailableDevicesFragment());
		fragments.add(new PairedDevicesFragment());
		viewPager2 = view.findViewById(R.id.view_pager);

		FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(this) {
			@NonNull
			@Override
			public Fragment createFragment(int position) {
				return fragments.get(position);
			}

			@Override
			public int getItemCount() {
				return fragments.size();
			}
		};
		viewPager2.setAdapter(fragmentStateAdapter);

		tabLayout = view.findViewById(R.id.tabLayout);
		new TabLayoutMediator(tabLayout, viewPager2, new TabConfigurationStrategy() {
			@Override
			public void onConfigureTab(@NonNull Tab tab, int position) {
				tab.setText(position == 0 ? "Available devices" : "Paired Devices");
			}
		}).attach();


		return view;
	}

	public static void newInstance(FragmentManager fragmentManager) {
		DevicesMainFragment fragment = new DevicesMainFragment();
		fragmentManager.beginTransaction()
				.replace(R.id.main_layout, fragment)
				.commit();
	}
}
