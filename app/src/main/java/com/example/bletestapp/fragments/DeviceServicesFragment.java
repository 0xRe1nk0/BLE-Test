package com.example.bletestapp.fragments;

import static com.example.bletestapp.bluetoothLe.GattAttributes.CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_MEASUREMENT;
import static com.example.bletestapp.bluetoothLe.GattAttributes.UUID_CHARACTERISTIC_CLIENT_CONFIG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bletestapp.MainActivity;
import com.example.bletestapp.R;
import com.example.bletestapp.bluetoothLe.BluetoothLeService;
import com.example.bletestapp.bluetoothLe.GattAttributes;
import com.example.bletestapp.interfaces.BatteryLevelDataListener;
import com.example.bletestapp.interfaces.GattListener;
import com.example.bletestapp.interfaces.HeartRateDataListener;
import com.example.bletestapp.interfaces.RunningSpeedDataListener;
import com.example.bletestapp.interfaces.TemperatureDataListener;
import com.example.bletestapp.interfaces.WheelDataListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeviceServicesFragment extends Fragment {
	private View view;
	private static final String DEVICE_NAME_KEY = "DEVICE_NAME_KEY";
	private static final String DEVICE_ADDRESS_KEY = "DEVICE_ADDRESS_KEY";
	private static final String HEART_RATE_KEY = "HEART_RATE_KEY";
	private static final String SYSTOLIC_KEY = "SYSTOLIC_KEY";
	private static final String DIASTOLIC_KEY = "DIASTOLIC_KEY";
	private static final String ARTERIAL_PRESSURE_KEY = "ARTERIAL_PRESSURE_KEY";
	private static final String CUFF_PRESSURE_KEY = "CUFF_PRESSURE_KEY";
	private static final String TIMESTAMP_KEY = "TIMESTAMP_KEY";
	private static final String PULSE_RATE_KEY = "PULSE_RATE_KEY";
	private static final String RUNNING_SPEED_KEY = "RUNNING_SPEED_KEY";
	private static final String RUNNING_CADENCE_KEY = "RUNNING_CADENCE_KEY";
	private static final String RUNNING_IS_RUNNING_KEY = "RUNNING_IS_RUNNING_KEY";
	private static final String RUNNING_STRIDE_LENGTH_KEY = "RUNNING_STRIDE_LENGTH_KEY";
	private static final String RUNNING_TOTAL_DISTANCE_KEY = "RUNNING_TOTAL_DISTANCE_KEY";
	private static final String BATTERY_LEVEL_KEY = "BATTERY_LEVEL_KEY";
	private static final String TEMPERATURE_KEY = "TEMPERATURE_KEY";
	private static final String GEAR_RATIO_KEY = "GEAR_RATIO_KEY";
	private static final String CADENCE_KEY = "CADENCE_KEY";
	private static final String SPEED_KEY = "SPEED_KEY";
	private static final String DISTANCE_KEY = "DISTANCE_KEY";
	private static final String TOTAL_DISTANCE_KEY = "TOTAL_DISTANCE_KEY";

	Map<String, View> valueViews = new HashMap<>();
	private GattListener gattListener;
	private WheelDataListener wheelDataListener;
	private HeartRateDataListener heartRateDataListener;
	private TemperatureDataListener temperatureDataListener;
	private BatteryLevelDataListener batteryLevelDataListener;
	private RunningSpeedDataListener runningSpeedDataListener;

	private String deviceName;
	private String deviceAddress;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private BluetoothLeService bluetoothLeService;
	private TextView statusView;
	private TextView nameView;
	private Button connectButton;
	private LinearLayout valuesContainer;

	private final List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();

	private int deviceStatus = BluetoothProfile.STATE_DISCONNECTED;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity activity = (MainActivity) requireActivity();
		bluetoothLeService = activity.bluetoothLeService;

		if (savedInstanceState != null) {
			deviceName = savedInstanceState.getString(DEVICE_NAME_KEY);
			deviceAddress = savedInstanceState.getString(DEVICE_ADDRESS_KEY);
		}

		if (getArguments() != null) {
			deviceName = getArguments().getString(DEVICE_NAME_KEY);
			deviceAddress = getArguments().getString(DEVICE_ADDRESS_KEY);
		}
	}

	private void getCharacteristics() {
		List<BluetoothGattService> services = bluetoothLeService.getSupportedGattServices();
		if (services != null) {
			for (BluetoothGattService gattService : bluetoothLeService.getSupportedGattServices()) {
				List<BluetoothGattCharacteristic> gattCharas = gattService.getCharacteristics();
				characteristics.addAll(gattCharas);
			}
		}
	}

	private void updateItemView(String key, String value, String format, String measureUnit) {
		if (valueViews.containsKey(key)) {
			updateItemView(key, value);
		} else {
			valueViews.put(key, createItemView(format, value, measureUnit));
		}

		valuesContainer.removeAllViews();
		for (View view1 : valueViews.values()) {
			valuesContainer.addView(view1);
		}
	}

	@SuppressLint("SetTextI18n")
	private View createItemView(String format, String value, String measureUnit) {
		View itemView = View.inflate(requireActivity(), R.layout.characteristic_value_item, null);
		TextView valueInfo = itemView.findViewById(R.id.value_info);
		valueInfo.setText(format + ":");

		TextView valueMeasureUnit = itemView.findViewById(R.id.value_measure_unit);
		valueMeasureUnit.setText(measureUnit);

		if (value != null) {
			TextView valueView = itemView.findViewById(R.id.value);
			valueView.setText(value);
		}
		return itemView;
	}

	private void updateItemView(String key, String value) {
		View itemView = valueViews.get(key);
		if (itemView != null) {
			TextView textView = itemView.findViewById(R.id.value);
			textView.setText(value);
		}
	}

	private void requestCharacteristic() {
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			if (GattAttributes.SUPPORTED_CHARACTERISTICS.contains(characteristic.getUuid())) {
				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					if (mNotifyCharacteristic != null) {
						setCharacteristicNotification(mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					readCharacteristic(characteristic);
				}
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					setCharacteristicNotification(characteristic, true);
				}
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		MainActivity activity = (MainActivity) requireActivity();
		view = inflater.inflate(R.layout.gatt_services_fragment, container, false);
		valuesContainer = view.findViewById(R.id.values_container);

		gattListener = new GattListener() {
			@Override
			public void onActionConnected() {
				BluetoothLeService bluetoothLeService = activity.bluetoothLeService;
				if (bluetoothLeService != null && bluetoothLeService.device != null) {
					deviceStatus = BluetoothProfile.STATE_CONNECTED;
					statusView.setText("Status: Connected");
					connectButton.setText("Disconnect");
				}
			}

			@Override
			public void onActionDisconnected() {
				BluetoothLeService bluetoothLeService = activity.bluetoothLeService;
				if (bluetoothLeService != null) {
					deviceStatus = BluetoothProfile.STATE_DISCONNECTED;
					statusView.setText("Status: Disconnected");
					connectButton.setText("Connect");
					valuesContainer.removeAllViews();
				}
			}

			@Override
			public void onActionServiceDiscovered() {
				BluetoothLeService bluetoothLeService = activity.bluetoothLeService;
				if (bluetoothLeService != null && bluetoothLeService.device != null) {
					statusView.setText("Status: Connected & Discovered");
					getCharacteristics();
					requestCharacteristic();
				}
			}

			@Override
			public void onActionDataAvailable(BluetoothGattCharacteristic characteristic) {
			}
		};

		wheelDataListener = new WheelDataListener() {
			@Override
			public void onWheelDataUpdate(float speed, float distance, float totalDistance) {
				DecimalFormat speedFormat = new DecimalFormat("0.##");
				speedFormat.setRoundingMode(RoundingMode.DOWN);

				DecimalFormat distanceFormat = new DecimalFormat("0");
				distanceFormat.setRoundingMode(RoundingMode.DOWN);

				updateItemView(SPEED_KEY, speedFormat.format(speed), "Speed", "km/h");
				updateItemView(DISTANCE_KEY, distanceFormat.format(distance), "Distance", "m");
				updateItemView(TOTAL_DISTANCE_KEY, distanceFormat.format(totalDistance), "Total distance", "m");
			}

			@Override
			public void onCadenceDataUpdate(float gearRatio, float cadence) {
				DecimalFormat gearRatioFormat = new DecimalFormat("0.##");
				gearRatioFormat.setRoundingMode(RoundingMode.DOWN);

				DecimalFormat cadenceFormat = new DecimalFormat("0");
				cadenceFormat.setRoundingMode(RoundingMode.DOWN);
				updateItemView(GEAR_RATIO_KEY, gearRatioFormat.format(gearRatio), "Gear ratio", "");
				updateItemView(CADENCE_KEY, cadenceFormat.format(cadence), "Cadence", "");
			}
		};

		heartRateDataListener = new HeartRateDataListener() {
			@Override
			public void onHeartRateDataUpdate(int heartRateValue, @Nullable String bodyPart) {
				updateItemView(HEART_RATE_KEY, String.valueOf(heartRateValue), "Heart rate", "bpm");
				updateItemView(HEART_RATE_KEY, String.valueOf(bodyPart), "Body part", "");
			}

			@Override
			public void onBloodPressureDataUpdate(float systolic, float diastolic, float arterialPressure, float cuffPressure, int unit, String timestamp, float pulseRate) {
				String unitString = unit == 0 ? "mmHG" : "kPa";
				updateItemView(SYSTOLIC_KEY, String.valueOf(systolic), "Systolic", unitString);
				updateItemView(DIASTOLIC_KEY, String.valueOf(diastolic), "Diastolic", unitString);
				updateItemView(ARTERIAL_PRESSURE_KEY, String.valueOf(arterialPressure), "Arterial pressure", unitString);
				updateItemView(CUFF_PRESSURE_KEY, String.valueOf(cuffPressure), "Cuff pressure", unitString);
				updateItemView(TIMESTAMP_KEY, String.valueOf(timestamp), "Timestamp", "");
				updateItemView(PULSE_RATE_KEY, String.valueOf(pulseRate), "Pulse rate", "bpm");
			}
		};

		temperatureDataListener = temperature -> updateItemView(TEMPERATURE_KEY, String.valueOf(temperature), "Temperature", "°C");

		batteryLevelDataListener = batteryLevel -> updateItemView(BATTERY_LEVEL_KEY, String.valueOf(batteryLevel), "Battery level", "%");

		runningSpeedDataListener = (speed, cadence, totalDistance, strideLength, isRunning) -> {
			updateItemView(RUNNING_SPEED_KEY, String.valueOf(speed), "Speed", "km/h");
			updateItemView(RUNNING_CADENCE_KEY, String.valueOf(cadence), "Cadence", "");
			updateItemView(RUNNING_TOTAL_DISTANCE_KEY, String.valueOf(totalDistance), "Total distance", "m");
			updateItemView(RUNNING_IS_RUNNING_KEY, isRunning ? "Running" : "Walking", "Type", "");
			updateItemView(RUNNING_STRIDE_LENGTH_KEY, String.valueOf(strideLength), "Stride length", "cm");
		};

		setupDeviceHeader();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity activity = (MainActivity) requireActivity();
		activity.registerGattListener(gattListener);
		activity.registerWheelDataListener(wheelDataListener);
		activity.registerHeartRateDataListener(heartRateDataListener);
		activity.registerTemperatureDataListener(temperatureDataListener);
		activity.registerBatteryLevelDataListener(batteryLevelDataListener);
		activity.registerRunningSpeedDataListener(runningSpeedDataListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		MainActivity activity = (MainActivity) requireActivity();
		activity.unregisterGattListener(gattListener);
		activity.unregisterWheelDataListener(wheelDataListener);
		activity.unregisterHeartRateDataListener(heartRateDataListener);
		activity.unregisterTemperatureDataListener(temperatureDataListener);
		activity.unregisterBatteryLevelDataListener(batteryLevelDataListener);
		activity.unregisterRunningSpeedDataListener(runningSpeedDataListener);
	}

	@SuppressLint("MissingPermission")
	private void setupDeviceHeader() {
		nameView = view.findViewById(R.id.device_name);
		nameView.setText("Name: " + deviceName);

		statusView = view.findViewById(R.id.device_status);
		statusView.setText("Status: Disconnected");

		connectButton = view.findViewById(R.id.connect_button);
		connectButton.setOnClickListener(view -> {
			MainActivity activity = (MainActivity) requireActivity();
			BluetoothLeService bluetoothLeService = activity.bluetoothLeService;
			if (deviceStatus == BluetoothProfile.STATE_DISCONNECTED) {
				if(bluetoothLeService.connect(deviceAddress)){
					connectButton.setText("Connecting");
				}
			} else {
				bluetoothLeService.disconnect();
			}
		});
	}

	@SuppressLint("MissingPermission")
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (bluetoothLeService.bluetoothAdapter == null || bluetoothLeService.bluetoothGatt == null) {
			return;
		}
		bluetoothLeService.bluetoothGatt.readCharacteristic(characteristic);
	}

	@SuppressLint("MissingPermission")
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
	                                          boolean enabled) {
		if (bluetoothLeService.bluetoothAdapter == null || bluetoothLeService.bluetoothGatt == null) {
			return;
		}
		bluetoothLeService.bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (GattAttributes.SUPPORTED_CHARACTERISTICS.contains(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_CLIENT_CONFIG);
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			bluetoothLeService.bluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public static void newInstance(FragmentManager fragmentManager, String name, String address) {
		DeviceServicesFragment nextFrag = new DeviceServicesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME_KEY, name);
		bundle.putString(DEVICE_ADDRESS_KEY, address);
		nextFrag.setArguments(bundle);
		fragmentManager.beginTransaction()
				.add(R.id.main_layout, nextFrag)
				.addToBackStack(null)
				.commit();
	}
}
