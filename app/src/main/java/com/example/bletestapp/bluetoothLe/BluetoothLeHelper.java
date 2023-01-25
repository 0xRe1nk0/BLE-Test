package com.example.bletestapp.bluetoothLe;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothLeHelper {
	Context context;
	public boolean isScanning = false;
	public boolean bleAvailable = false;

	public BluetoothManager bluetoothManager;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothLeScanner bluetoothLeScanner;
	private ScanListener scanListener = null;
	public BluetoothDevice bluetoothDevice = null;

	public ArrayList<BluetoothDevice> leDevices = new ArrayList<>();
	public Map<String, BluetoothGatt> gattMap = new HashMap<>();

	public ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
	public ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
	public ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics = new ArrayList<>();


	public BluetoothLeHelper(Context applicationContext) {
		context = applicationContext;
		bluetoothManager = context.getSystemService(BluetoothManager.class);
		bluetoothAdapter = bluetoothManager.getAdapter();
		bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
	}

	@SuppressLint("MissingPermission")
	public void scan(){
		if(isScanning){
			bluetoothLeScanner.stopScan(leScanCallback);
			isScanning = false;
		}else{
			leDevices.clear();
			bluetoothLeScanner.startScan(leScanCallback);
			isScanning = true;
		}
	}

	public void setScanListener(ScanListener scanListener) {
		this.scanListener = scanListener;
	}

	private final ScanCallback leScanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				super.onScanResult(callbackType, result);
				if (!leDevices.contains(result.getDevice())) {
					leDevices.add(result.getDevice());

					if(scanListener != null){
						scanListener.onDeviceFound(result.getDevice());
					}
				}
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				super.onBatchScanResults(results);
				for (ScanResult result : results) {
					leDevices.add(result.getDevice());

					if(scanListener != null){
						scanListener.onDeviceFound(result.getDevice());
					}
				}
			}

			@Override
			public void onScanFailed(int errorCode) {
				super.onScanFailed(errorCode);
			}
	};

}

