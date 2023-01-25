package com.example.bletestapp.bluetoothLe;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.UUID;

public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "EXTRA_DATA";

	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private String bluetoothDeviceAddress;
	private BluetoothGatt bluetoothGatt;
	private Binder binder = new LocalBinder();
	private int connectionState = 0;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public boolean initialize() {
		if (bluetoothManager == null) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager");
				return false;
			}
		}

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter");
			return false;
		}

		return true;
	}

	@SuppressLint("MissingPermission")
	public boolean connect(final String address) {
		if (bluetoothAdapter == null || address == null) {
			Log.i(TAG, "BluetoothAdapter not initialized or wrong address");
			return false;
		}

		if (address.equals(bluetoothDeviceAddress) && bluetoothGatt != null) {
			if (bluetoothGatt.connect()) {
				connectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.i(TAG, "Device not found");
			return false;
		}

		bluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create new connection");
		bluetoothDeviceAddress = address;
		connectionState = STATE_CONNECTING;
		return true;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//close();
		return super.onUnbind(intent);
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@SuppressLint("MissingPermission")
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
		                                 BluetoothGattCharacteristic characteristic,
		                                 int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
		                                    BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
	};

	@SuppressLint("MissingPermission")
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
	                                          boolean enabled) {
		if (bluetoothAdapter == null || bluetoothGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (GattAttributes.UUID_CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_FEATURE.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
					UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			bluetoothGatt.writeDescriptor(descriptor);
		}
	}

	@SuppressLint("MissingPermission")
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (bluetoothAdapter == null || bluetoothGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		bluetoothGatt.readCharacteristic(characteristic);
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
	                             final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		if (GattAttributes.UUID_CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_FEATURE.equals(characteristic.getUuid())) {

		} else {

		}

		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService(){
			return BluetoothLeService.this;
		}
	}
}
