package com.example.bletestapp.fragments;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.bletestapp.MainActivity;
import com.example.bletestapp.R;
import com.example.bletestapp.bluetoothLe.BluetoothLeHelper;
import com.example.bletestapp.bluetoothLe.GattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AvailableDevicesFragment extends Fragment {
	public final String TAG = AvailableDevicesFragment.class.getName();

	private View view;
	private LinearLayout scrollLayout;
	private Button scanButton;
	private BluetoothLeHelper bluetoothLeHelper;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity activity = (MainActivity) requireActivity();
		bluetoothLeHelper = activity.bluetoothLeHelper;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.available_devices_fragment, container);

		scanButton = view.findViewById(R.id.scan_button);
		scrollLayout = view.findViewById(R.id.scroll_layout);
		updateButton();

		bluetoothLeHelper.setScanListener(this::showDevices);
		scanButton.setOnClickListener(view -> {
			bluetoothLeHelper.scan();
			updateButton();
		});

		return view;
	}

	private void showDevices(BluetoothDevice device) {
		scrollLayout.addView(createListItemView(device));
	}

	@SuppressLint("MissingPermission")
	private View createListItemView(BluetoothDevice device) {
		View itemView = getLayoutInflater().inflate(R.layout.ble_device_item, null);
		TextView addressView = itemView.findViewById(R.id.address);
		addressView.setText(device.getAddress());

		TextView nameView = itemView.findViewById(R.id.name);
		nameView.setText(device.getName());

		CheckBox checkBox = itemView.findViewById(R.id.paired_checkbox);
		checkBox.setTag(device);
		checkBox.setOnClickListener(view -> {
			if (checkBox.isChecked()) {
				bluetoothLeHelper.bluetoothDevice = device;
				connectDevice(device);
			} else {
				bluetoothLeHelper.bluetoothDevice = null;
				disconnectDevice(device);
			}
		});

		ConstraintLayout layout = itemView.findViewById(R.id.clickable_container);
		layout.setTag(device);
		layout.setOnClickListener(view -> {
			if(device == bluetoothLeHelper.bluetoothDevice){
				DeviceServicesFragment.newInstance(requireActivity().getSupportFragmentManager());
			}
		});

		return itemView;
	}

	@SuppressLint("MissingPermission")
	private void disconnectDevice(BluetoothDevice device) {
		BluetoothGatt gatt = bluetoothLeHelper.gattMap.get(device.getAddress());
		if (gatt != null) {
			gatt.disconnect();
			gatt.close();
		}
	}

	@SuppressLint("MissingPermission")
	private void connectDevice(BluetoothDevice device) {
		bluetoothLeHelper.gattMap.put(device.getAddress(), device.connectGatt(requireActivity(), false, new BluetoothGattCallback() {

			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				Log.d(TAG, "status: " + status);
				Log.d(TAG, "newState: " + newState);
				if (status == GATT_SUCCESS) {
					if (newState == BluetoothProfile.STATE_CONNECTED) {
						int bondState = device.getBondState();

						if (bondState == BOND_NONE || bondState == BOND_BONDED) {
							Log.d(TAG, "Discovering services");
							boolean result = gatt.discoverServices();

							if (!result) {
								Log.e(TAG, "DiscoverServices failed to start");
							}
						} else if (bondState == BOND_BONDING) {
							Log.d(TAG, "Waiting for bonding to complete");
						}
					} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
						gatt.close();
					}

				} else {
					gatt.close();
				}
			}


			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				if (status == 129) {
					Log.e(TAG, "Service discovery failed");
					gatt.disconnect();
					return;
				}

				final List<BluetoothGattService> services = gatt.getServices();
				Log.d(TAG, String.format(Locale.US, "discovered %d services for '%s'", services.size(), gatt.getDevice().getName()));

				String uuid;
				String unknownServiceString = "Unknown service";
				String unknownCharaString = "Unknown characteristic";

				for (BluetoothGattService gattService : services) {
					HashMap<String, String> currentServiceData = new HashMap<>();
					uuid = gattService.getUuid().toString();
					currentServiceData.put("NAME", GattAttributes.lookup(uuid, unknownServiceString));
					currentServiceData.put("UUID", uuid);
					bluetoothLeHelper.gattServiceData.add(currentServiceData);

					ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
					List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
					ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

					for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
						charas.add(gattCharacteristic);
						HashMap<String, String> currentCharaData = new HashMap<>();
						uuid = gattCharacteristic.getUuid().toString();
						currentCharaData.put("NAME", GattAttributes.lookup(uuid, unknownCharaString));
						currentCharaData.put("UUID", uuid);
						gattCharacteristicGroupData.add(currentCharaData);
					}
					bluetoothLeHelper.gattCharacteristics.add(charas);
					bluetoothLeHelper.gattCharacteristicData.add(gattCharacteristicGroupData);
				}

			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt,
			                                 BluetoothGattCharacteristic characteristic,
			                                 int status) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					broadcastUpdate(characteristic);
				}
			}

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt,
			                                    BluetoothGattCharacteristic characteristic) {
				broadcastUpdate(characteristic);
			}

		}, BluetoothDevice.TRANSPORT_LE));
	}
	private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
		if (GattAttributes.UUID_CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_FEATURE.equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
			}
			final int speed = characteristic.getIntValue(format, 1);
			Log.d(TAG, String.format("Speed: %d", speed));
		} else {
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for(byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				Toast.makeText(requireActivity(), "Value: " + stringBuilder, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private void updateButton() {
		if (bluetoothLeHelper.isScanning) {
			scanButton.setText("Stop");
		} else {
			scrollLayout.removeAllViews();
			scanButton.setText("Scan");
		}
	}
}

