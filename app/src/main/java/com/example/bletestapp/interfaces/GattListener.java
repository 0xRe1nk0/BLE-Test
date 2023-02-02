package com.example.bletestapp.interfaces;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattListener {
	void onActionConnected();

	void onActionDisconnected();

	void onActionServiceDiscovered();

	void onActionDataAvailable(BluetoothGattCharacteristic characteristic);
}
