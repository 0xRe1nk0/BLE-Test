package com.example.bletestapp.fragments;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bletestapp.MainActivity;
import com.example.bletestapp.R;
import com.example.bletestapp.bluetoothLe.BluetoothLeHelper;
import com.example.bletestapp.bluetoothLe.BluetoothLeService;

public class DeviceServicesFragment extends Fragment {
	private View view;
	private BluetoothLeHelper bluetoothLeHelper;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private BluetoothLeService bluetoothLeService;
	private ExpandableListView expandableListView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MainActivity activity = (MainActivity) requireActivity();
		bluetoothLeHelper = activity.bluetoothLeHelper;
		bluetoothLeService = activity.bluetoothLeService;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.gatt_services_fragment, container, false);

		expandableListView = view.findViewById(R.id.gatt_services_list);
		expandableListView.setOnChildClickListener(servicesListClickListener);

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				requireActivity(),
				bluetoothLeHelper.gattServiceData,
				android.R.layout.simple_expandable_list_item_2,
				new String[] {"NAME", "UUID"},
				new int[] {android.R.id.text1, android.R.id.text2},
				bluetoothLeHelper.gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2,
				new String[] {"NAME", "UUID"},
				new int[] {android.R.id.text1, android.R.id.text2}
		);
		expandableListView.setAdapter(gattServiceAdapter);

		return view;
	}

	private final ExpandableListView.OnChildClickListener servicesListClickListener =
			new ExpandableListView.OnChildClickListener() {
				@SuppressLint("MissingPermission")
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				                            int childPosition, long id) {
					BluetoothGatt gatt = bluetoothLeHelper.gattMap.get(bluetoothLeHelper.bluetoothDevice.getAddress());
					if (bluetoothLeHelper.gattCharacteristics != null && gatt != null) {
						final BluetoothGattCharacteristic characteristic = bluetoothLeHelper.gattCharacteristics.get(groupPosition).get(childPosition);
						final int charaProp = characteristic.getProperties();
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
							if (mNotifyCharacteristic != null) {
								gatt.setCharacteristicNotification(mNotifyCharacteristic, false);
								mNotifyCharacteristic = null;
							}
							gatt.readCharacteristic(characteristic);
						}
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mNotifyCharacteristic = characteristic;
							gatt.setCharacteristicNotification(characteristic, true);
						}
						return true;
					}
					return false;
				}
			};

	public static void newInstance(FragmentManager fragmentManager) {
		DeviceServicesFragment nextFrag = new DeviceServicesFragment();
		fragmentManager.beginTransaction()
				.add(R.id.main_layout, nextFrag)
				.addToBackStack(null)
				.commit();
	}
}
