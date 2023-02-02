package com.example.bletestapp;

import static com.example.bletestapp.bluetoothLe.BluetoothLeService.BLUETOOTH_CHARACTERISTIC_KEY;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_ARTERIAL_PRESSURE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_BATTERY_LEVEL;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_CADENCE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_CUFF_PRESSURE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_DIASTOLIC;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_DISTANCE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_GEAR_RATIO;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_HEART_RATE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_HEART_RATE_BODY_PART;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_PULSE_RATE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_RUNNING_CADENCE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_RUNNING_IS_RUNNING;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_RUNNING_SPEED;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_RUNNING_STRIDE_LENGTH;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_RUNNING_TOTAL_DISTANCE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_SPEED;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_SYSTOLIC;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_TEMPERATURE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_TIMESTAMP;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_TOTAL_DISTANCE;
import static com.example.bletestapp.bluetoothLe.BluetoothLeService.EXTRA_UNIT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bletestapp.bluetoothLe.BluetoothLeService;
import com.example.bletestapp.interfaces.BatteryLevelDataListener;
import com.example.bletestapp.interfaces.GattListener;
import com.example.bletestapp.interfaces.HeartRateDataListener;
import com.example.bletestapp.interfaces.RunningSpeedDataListener;
import com.example.bletestapp.interfaces.TemperatureDataListener;
import com.example.bletestapp.interfaces.WheelDataListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
	public final String TAG = MainActivity.class.getName();

	public BluetoothLeService bluetoothLeService;

	private final ArrayList<GattListener> gattListeners = new ArrayList<>();
	private final ArrayList<WheelDataListener> wheelDataListeners = new ArrayList<>();
	private final ArrayList<HeartRateDataListener> heartRateDataListeners = new ArrayList<>();
	private final ArrayList<TemperatureDataListener> temperatureDataListeners = new ArrayList<>();
	private final ArrayList<BatteryLevelDataListener> batteryLevelDataListeners = new ArrayList<>();
	private final ArrayList<RunningSpeedDataListener> runningSpeedDataListeners = new ArrayList<>();

	public final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			Log.i("serviceConnected", String.valueOf(bluetoothLeService));
			if (!bluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.i("serviceDisconnected", String.valueOf(bluetoothLeService));
			bluetoothLeService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

		PreferenceManager.setDefaultValues(this, R.xml.scan_preference, false);

		requestPermissions();
		setupActionBar();

	}

	private void setupActionBar() {
		getSupportFragmentManager().addOnBackStackChangedListener(() -> {
			getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() >= 1);
		});
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.BROADCAST_WHEEL_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_HEART_RATE_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_HEART_RATE_PLUS_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_CADENCE_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_TEMPERATURE_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_BATTERY_LEVEL_DATA);
		intentFilter.addAction(BluetoothLeService.BROADCAST_RUNNING_SPEED_DATA);
		return intentFilter;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
		bluetoothLeService = null;
	}

	public void registerGattListener(GattListener gattListener) {
		gattListeners.add(gattListener);
	}

	public void unregisterGattListener(GattListener gattListener) {
		gattListeners.remove(gattListener);
	}

	public void registerWheelDataListener(WheelDataListener wheelDataListener) {
		wheelDataListeners.add(wheelDataListener);
	}

	public void unregisterWheelDataListener(WheelDataListener wheelDataListener) {
		wheelDataListeners.remove(wheelDataListener);
	}

	private void notifyWheelDataListeners(float speed, float distance, float totalDistance) {
		for (WheelDataListener listener : wheelDataListeners) {
			listener.onWheelDataUpdate(speed, distance, totalDistance);
		}
	}

	public void registerTemperatureDataListener(TemperatureDataListener temperatureDataListener) {
		temperatureDataListeners.add(temperatureDataListener);
	}

	public void unregisterTemperatureDataListener(TemperatureDataListener temperatureDataListener) {
		temperatureDataListeners.remove(temperatureDataListener);
	}

	private void notifyTemperatureDataListeners(float temperature) {
		for (TemperatureDataListener listener : temperatureDataListeners) {
			listener.onTemperatureDataUpdate(temperature);
		}
	}

	public void registerBatteryLevelDataListener(BatteryLevelDataListener batteryLevelDataListener) {
		batteryLevelDataListeners.add(batteryLevelDataListener);
	}

	public void unregisterBatteryLevelDataListener(BatteryLevelDataListener batteryLevelDataListener) {
		batteryLevelDataListeners.remove(batteryLevelDataListener);
	}

	private void notifyBatteryLevelDataListeners(int batteryLevel) {
		for (BatteryLevelDataListener listener : batteryLevelDataListeners) {
			listener.onBatteryLevelDataUpdate(batteryLevel);
		}
	}

	public void registerRunningSpeedDataListener(RunningSpeedDataListener runningSpeedDataListener) {
		runningSpeedDataListeners.add(runningSpeedDataListener);
	}

	public void unregisterRunningSpeedDataListener(RunningSpeedDataListener runningSpeedDataListener) {
		runningSpeedDataListeners.remove(runningSpeedDataListener);
	}

	private void notifyRunningSpeedDataListeners(float speed, int cadence, float totalDistance, float strideLength, boolean isRunning) {
		for (RunningSpeedDataListener listener : runningSpeedDataListeners) {
			listener.onRunningSpeedDataUpdate(speed, cadence, totalDistance, strideLength, isRunning);
		}
	}

	private void notifyCadenceDataListeners(float gearRatio, float cadence) {
		for (WheelDataListener listener : wheelDataListeners) {
			listener.onCadenceDataUpdate(gearRatio, cadence);
		}
	}

	public void registerHeartRateDataListener(HeartRateDataListener heartRateDataListener) {
		heartRateDataListeners.add(heartRateDataListener);
	}

	public void unregisterHeartRateDataListener(HeartRateDataListener heartRateDataListener) {
		heartRateDataListeners.remove(heartRateDataListener);
	}

	private void notifyHeartRateDataListeners(int heartRateValue, @Nullable String bodyPart) {
		for (HeartRateDataListener listener : heartRateDataListeners) {
			listener.onHeartRateDataUpdate(heartRateValue, bodyPart);
		}
	}

	private void notifyBloodPressureDataListeners(float heartRateValue, float diastolic, float arterialPressure, float cuffPressure, int unit, @Nullable String timestamp, float pulseRate) {
		for (HeartRateDataListener listener : heartRateDataListeners) {
			listener.onBloodPressureDataUpdate(heartRateValue, diastolic, arterialPressure, cuffPressure, unit, timestamp, pulseRate);
		}
	}

	private void notifyGattListeners(String action, Intent intent) {
		for (GattListener listener : gattListeners) {
			switch (action) {
				case BluetoothLeService.ACTION_GATT_CONNECTED:
					listener.onActionConnected();
					break;
				case BluetoothLeService.ACTION_GATT_DISCONNECTED:
					listener.onActionDisconnected();
					break;
				case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
					listener.onActionServiceDiscovered();
					break;
				case BluetoothLeService.ACTION_DATA_AVAILABLE:
					BluetoothGattCharacteristic characteristic = intent.getParcelableExtra(BLUETOOTH_CHARACTERISTIC_KEY);
					listener.onActionDataAvailable(characteristic);
					break;
			}
		}
	}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			notifyGattListeners(action, intent);
			if (BluetoothLeService.BROADCAST_WHEEL_DATA.equals(action)) {
				float speed = intent.getFloatExtra(EXTRA_SPEED, 0);
				float distance = intent.getFloatExtra(EXTRA_DISTANCE, 0);
				float totalDistance = intent.getFloatExtra(EXTRA_TOTAL_DISTANCE, 0);

				notifyWheelDataListeners(speed, distance, totalDistance);
			} else if (BluetoothLeService.BROADCAST_HEART_RATE_DATA.equals(action)) {
				int heartRateValue = intent.getIntExtra(EXTRA_HEART_RATE, 0);
				String bodyPart = intent.getStringExtra(EXTRA_HEART_RATE_BODY_PART);

				notifyHeartRateDataListeners(heartRateValue, bodyPart);
			} else if (BluetoothLeService.BROADCAST_CADENCE_DATA.equals(action)) {
				float gearRatio = intent.getFloatExtra(EXTRA_GEAR_RATIO, 0);
				float cadence = intent.getFloatExtra(EXTRA_CADENCE, 0);

				notifyCadenceDataListeners(gearRatio, cadence);
			} else if (BluetoothLeService.BROADCAST_HEART_RATE_PLUS_DATA.equals(action)) {
				float systolic = intent.getFloatExtra(EXTRA_SYSTOLIC, 0);
				float diastolic = intent.getFloatExtra(EXTRA_DIASTOLIC, 0);
				float arterialPressure = intent.getFloatExtra(EXTRA_ARTERIAL_PRESSURE, 0);
				float cuffPressure = intent.getFloatExtra(EXTRA_CUFF_PRESSURE, 0);
				int unit = intent.getIntExtra(EXTRA_UNIT, 0);
				String timestamp = intent.getStringExtra(EXTRA_TIMESTAMP);
				float pulseRate = intent.getFloatExtra(EXTRA_PULSE_RATE, 0);

				notifyBloodPressureDataListeners(systolic, diastolic, arterialPressure, cuffPressure, unit, timestamp, pulseRate);
			} else if (BluetoothLeService.BROADCAST_TEMPERATURE_DATA.equals(action)) {
				float temperature = intent.getFloatExtra(EXTRA_TEMPERATURE, 0);

				notifyTemperatureDataListeners(temperature);
			} else if (BluetoothLeService.BROADCAST_BATTERY_LEVEL_DATA.equals(action)) {
				int batteryLevel = intent.getIntExtra(EXTRA_BATTERY_LEVEL, 0);

				notifyBatteryLevelDataListeners(batteryLevel);
			} else if (BluetoothLeService.BROADCAST_RUNNING_SPEED_DATA.equals(action)) {
				float speed = intent.getFloatExtra(EXTRA_RUNNING_SPEED, 0);
				int cadence = intent.getIntExtra(EXTRA_RUNNING_CADENCE, 0);
				float totalDistance = intent.getFloatExtra(EXTRA_RUNNING_TOTAL_DISTANCE, 0);
				float strideLength = intent.getFloatExtra(EXTRA_RUNNING_STRIDE_LENGTH, 0);
				boolean isRunning = intent.getBooleanExtra(EXTRA_RUNNING_IS_RUNNING, false);

				notifyRunningSpeedDataListeners(speed, cadence, totalDistance, strideLength, isRunning);
			}
		}
	};

	public boolean requestLeService() {
		if (bluetoothLeService == null) {
			Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
			bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
			Toast.makeText(this, "Ble service unavailable", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public boolean requestPermissions() {
		boolean hasNeededPermissions = true;

		if (VERSION.SDK_INT >= VERSION_CODES.S) {
			if (!havePermission(permission.BLUETOOTH_SCAN, this)) {
				hasNeededPermissions = false;
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_SCAN}, 4);
			}
			if (!havePermission(permission.BLUETOOTH_CONNECT, this)) {
				hasNeededPermissions = false;
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_CONNECT}, 5);
			}
		} else {
			if (!havePermission(permission.BLUETOOTH, this)) {
				hasNeededPermissions = false;
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH}, 2);
			}
			if (!havePermission(permission.BLUETOOTH_ADMIN, this)) {
				hasNeededPermissions = false;
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_ADMIN}, 3);
			}
		}
		if (!havePermission(permission.ACCESS_FINE_LOCATION, this)) {
			hasNeededPermissions = false;
			ActivityCompat.requestPermissions(this, new String[] {permission.ACCESS_FINE_LOCATION}, 1);
		}
		if (!havePermission(permission.ACCESS_COARSE_LOCATION, this)) {
			hasNeededPermissions = false;
			ActivityCompat.requestPermissions(this, new String[] {permission.ACCESS_COARSE_LOCATION}, 1);
		}

		return hasNeededPermissions;
	}

	public boolean locationRequest() {
		boolean locationEnabled = true;

		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationEnabled = false;
			buildAlertMessageNoGps();
		}
		return locationEnabled;
	}

	private void buildAlertMessageNoGps() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
				.setNegativeButton("No", (dialog, id) -> dialog.cancel());
		AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean requestBle() {
		boolean bluetoothEnabled = true;
		if (bluetoothLeService != null && getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			if (!bluetoothLeService.bluetoothAdapter.isEnabled()) {
				bluetoothEnabled = false;
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.enableBtIntent.launch(enableBtIntent);
			}
		} else {
			bluetoothEnabled = false;
			Toast.makeText(this, "Bluetooth LE isnt supported on this device", Toast.LENGTH_SHORT).show();
		}
		return bluetoothEnabled;
	}

	ActivityResultLauncher<Intent> enableBtIntent = registerForActivityResult(new StartActivityForResult(), result -> {
		if (result.getResultCode() != Activity.RESULT_OK) {
			Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
		}
	});

	@Nullable
	@Override
	public ActionBar getSupportActionBar() {
		return super.getSupportActionBar();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			if (getSupportFragmentManager().getBackStackEntryCount() >= 1) {
				getSupportFragmentManager().popBackStack();
			}
			return true;
		}
		return false;
	}

	public static boolean havePermission(String permission, @NonNull Context context) {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}
}