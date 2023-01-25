package com.example.bletestapp.bluetoothLe;

import android.bluetooth.BluetoothDevice;

public interface ScanListener {
	void onDeviceFound(BluetoothDevice device);
}
