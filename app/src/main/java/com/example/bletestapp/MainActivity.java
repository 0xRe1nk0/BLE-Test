package com.example.bletestapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bletestapp.bluetoothLe.BluetoothLeHelper;
import com.example.bletestapp.bluetoothLe.BluetoothLeService;
import com.example.bletestapp.fragments.AvailableDevicesFragment;
import com.example.bletestapp.fragments.DeviceServicesFragment;
import com.example.bletestapp.fragments.DevicesMainFragment;
import com.example.bletestapp.fragments.PairedDevicesFragment;
import com.example.bletestapp.interfaces.PermissionRequestListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
	public final String TAG = MainActivity.class.getName();

	public BluetoothLeHelper bluetoothLeHelper;
	public BluetoothLeService bluetoothLeService;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			Log.i("serviceConnected", String.valueOf(bluetoothLeService));
			if (!bluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}

			//bluetoothLeService.connect(deviceAddress);
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
		bluetoothLeHelper = new BluetoothLeHelper(getApplicationContext());

		initBle();
		requestPermissions();

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

		DevicesMainFragment.newInstance(getSupportFragmentManager());
	}

	private void requestPermissions() {
		if (!havePermission(permission.ACCESS_FINE_LOCATION, this)) {
			ActivityCompat.requestPermissions(this, new String[] {permission.ACCESS_FINE_LOCATION}, 1);
		}
		if (!havePermission(permission.BLUETOOTH, this)) {
			ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH}, 2);
		}
		if (!havePermission(permission.BLUETOOTH_ADMIN, this)) {
			ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_ADMIN}, 3);
		}
		if (!havePermission(permission.BLUETOOTH_SCAN, this)) {
			if (VERSION.SDK_INT >= VERSION_CODES.S) {
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_SCAN}, 4);
			}
		}
		if (!havePermission(permission.BLUETOOTH_CONNECT, this)) {
			if (VERSION.SDK_INT >= VERSION_CODES.S) {
				ActivityCompat.requestPermissions(this, new String[] {permission.BLUETOOTH_CONNECT}, 5);
			}
		}

	}

	private void initBle() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			if (!bluetoothLeHelper.bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.enableBtIntent.launch(enableBtIntent);
			}
		} else {
			bluetoothLeHelper.bleAvailable = false;
			Toast.makeText(this, "Bluetooth LE isnt supported on this device", Toast.LENGTH_SHORT).show();
		}
	}

	ActivityResultLauncher<Intent> enableBtIntent = registerForActivityResult(new StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {
			bluetoothLeHelper.bleAvailable = true;
		} else {
			bluetoothLeHelper.bleAvailable = false;
			Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
		}
	});

	public static boolean havePermission(String permission, @NonNull Context context) {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}
}