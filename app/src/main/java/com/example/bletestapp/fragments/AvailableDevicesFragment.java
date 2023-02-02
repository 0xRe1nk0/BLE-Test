package com.example.bletestapp.fragments;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.bletestapp.MainActivity;
import com.example.bletestapp.R;
import com.example.bletestapp.bluetoothLe.devices.DeviceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AvailableDevicesFragment extends Fragment {
	public final String TAG = AvailableDevicesFragment.class.getName();

	public final static String SCAN_MODE_PREFERENCE = "SCAN_MODE";
	public final static String MATCH_MODE_PREFERENCE = "MATCH_MODE";
	public final static String CALLBACK_TYPE_PREFERENCE = "CALLBACK_TYPE";
	public final static String MATCH_NUM_PREFERENCE = "MATCH_NUM";

	private View view;
	private LinearLayout scrollLayout;
	private final Map<BluetoothDevice, View> deviceViewMap = new HashMap<>();
	private Button scanButton;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothLeScanner bluetoothLeScanner;
	public ArrayList<BluetoothDevice> leDevices = new ArrayList<>();

	public List<DeviceType> filterList = new ArrayList<>();

	private Handler handler;
	private boolean scanning = false;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity activity = (MainActivity) requireActivity();

		if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(activity, "Ble not supported", Toast.LENGTH_SHORT).show();
			activity.finish();
		}

		BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(activity, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
			activity.finish();
			return;
		}
		bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

		handler = new Handler();
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.available_devices_fragment, container, false);

		scanButton = view.findViewById(R.id.scan_button);
		scrollLayout = view.findViewById(R.id.scroll_layout);
		updateButton();

		scanButton.setOnClickListener(view -> {
			scanLeDevice(!scanning);
			updateButton();
		});

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		scanLeDevice(false);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		inflater.inflate(R.menu.scan_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.filter_button) {
			showFilterMenu(requireActivity().findViewById(R.id.filter_button));
		} else if (id == R.id.scan_settings) {
			ScanPreferenceFragment.newInstance(requireActivity().getSupportFragmentManager());
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFilterMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(requireActivity(), view);

		for (DeviceType deviceType : DeviceType.values()) {
			int itemId = deviceType.ordinal();
			popupMenu.getMenu().add(0, itemId, 0, deviceType.getName())
					.setCheckable(true)
					.setChecked(filterList.contains(deviceType));
		}

		popupMenu.setOnMenuItemClickListener(menuItem -> {
			DeviceType type = DeviceType.values()[menuItem.getItemId()];
			if (filterList.contains(type)) {
				menuItem.setChecked(false);
				filterList.remove(type);
			} else {
				menuItem.setChecked(true);
				filterList.add(type);
			}

			if(scanning){
				scanLeDevice(false);
				handler.postDelayed(() -> {
					leDevices.clear();
					scrollLayout.removeAllViews();
					deviceViewMap.clear();
					scanLeDevice(true);
				}, 300);
			}

			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			menuItem.setActionView(new View(requireActivity()));
			menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					return false;
				}

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					return false;
				}
			});

			return false;
		});

		popupMenu.show();
	}

	@SuppressLint("MissingPermission")
	private void scanLeDevice(boolean enable) {
		if (!enable) {
			if(bluetoothLeScanner!= null){
				bluetoothLeScanner.stopScan(leScanCallback);
				scanning = false;
			}
		} else {
			MainActivity activity = (MainActivity) requireActivity();
			if(!activity.requestPermissions()){
				Toast.makeText(activity, "Permissions not granted", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!activity.requestBle()){
				Toast.makeText(activity, "Bluetooth isnt available", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!activity.locationRequest()){
				Toast.makeText(activity, "Location isnt available", Toast.LENGTH_SHORT).show();
				return;
			}

			if(!activity.requestLeService()){
				return;
			}
			leDevices.clear();

			ArrayList<UUID> serviceUUIDs = new ArrayList<>();
			for (DeviceType type : filterList) {
				serviceUUIDs.add(UUID.fromString(type.getUUIDService()));
			}
			List<ScanFilter> filters = null;
			if (!serviceUUIDs.isEmpty()) {
				filters = new ArrayList<>();
				for (UUID serviceUUID : serviceUUIDs) {
					ScanFilter filter = new ScanFilter.Builder()
							.setServiceUuid(new ParcelUuid(serviceUUID))
							.build();
					filters.add(filter);
				}
			}

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
			ScanSettings scanSettings = new ScanSettings.Builder()
					.setScanMode(Integer.parseInt(preferences.getString(SCAN_MODE_PREFERENCE, "1")))
					.setCallbackType(Integer.parseInt(preferences.getString(CALLBACK_TYPE_PREFERENCE, "1")))
					.setMatchMode(Integer.parseInt(preferences.getString(MATCH_MODE_PREFERENCE, "2")))
					.setNumOfMatches(Integer.parseInt(preferences.getString(MATCH_NUM_PREFERENCE, "1")))
					.setReportDelay(0L)
					.build();


			bluetoothLeScanner.startScan(filters, scanSettings, leScanCallback);
			scanning = true;
		}
	}

	private final ScanCallback leScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			super.onScanResult(callbackType, result);
			if (!leDevices.contains(result.getDevice())) {
				leDevices.add(result.getDevice());
				showDevice(result);
			}else{
				updateRSSILevel(result);
			}
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			super.onBatchScanResults(results);
			for (ScanResult result : results) {
				leDevices.add(result.getDevice());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			super.onScanFailed(errorCode);
		}
	};

	private void updateRSSILevel(ScanResult result) {
		View deviceView = deviceViewMap.get(result.getDevice());
		if (deviceView != null) {
			TextView rssiView = deviceView.findViewById(R.id.rssi);
			rssiView.setText("RSSI: " + result.getRssi() + "db");
		}
	}

	private void showDevice(ScanResult result) {
		scrollLayout.addView(createListItemView(result));
	}

	@SuppressLint("MissingPermission")
	private View createListItemView(ScanResult result) {
		BluetoothDevice device = result.getDevice();
		View itemView = getLayoutInflater().inflate(R.layout.ble_device_item, null);
		TextView addressView = itemView.findViewById(R.id.address);
		addressView.setText(device.getAddress());

		TextView nameView = itemView.findViewById(R.id.name);
		nameView.setText(device.getName() == null ? "N/A" : device.getName());

		TextView rssiView = itemView.findViewById(R.id.rssi);
		rssiView.setText("RSSI: " + result.getRssi() + "db");

/*		CheckBox checkBox = itemView.findViewById(R.id.paired_checkbox);
		checkBox.setTag(device);
		checkBox.setOnClickListener(view -> {
			MainActivity activity = (MainActivity) requireActivity();
			BluetoothLeService bluetoothLeService = activity.bluetoothLeService;
			if (checkBox.isChecked()) {
				device.createBond();
				//bluetoothLeHelper.bluetoothDevice = device;
				//connectDevice(device);
			} else {
				device.createBond();
				bluetoothLeService.disconnect();
				//bluetoothLeHelper.bluetoothDevice = null;
				//disconnectDevice(device);
			}
		});*/

		ConstraintLayout layout = itemView.findViewById(R.id.clickable_container);
		layout.setTag(device);
		layout.setOnClickListener(view -> {
			DeviceServicesFragment.newInstance(requireActivity().getSupportFragmentManager(), device.getName(), device.getAddress());
		});

		deviceViewMap.put(device, itemView);
		return itemView;
	}

	public static void newInstance(FragmentManager fragmentManager) {
		AvailableDevicesFragment fragment = new AvailableDevicesFragment();
		fragmentManager.beginTransaction()
				.replace(R.id.main_layout, fragment)
				.commit();
	}

	@SuppressLint("SetTextI18n")
	private void updateButton() {
		if (scanning) {
			scanButton.setText("Stop");
		} else {
			deviceViewMap.clear();
			scrollLayout.removeAllViews();
			scanButton.setText("Scan");
		}
	}
}

