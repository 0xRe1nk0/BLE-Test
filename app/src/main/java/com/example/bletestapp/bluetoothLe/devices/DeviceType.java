package com.example.bletestapp.bluetoothLe.devices;

import com.example.bletestapp.bluetoothLe.sensors.SensorType;

public enum DeviceType {
	HRM(SensorType.HEART_RATE),
	BIKE_SPEED(SensorType.SPEED_MPS),
	BIKE_CADENCE(SensorType.CADENCE);

	private final SensorType sensorType;

	DeviceType(SensorType mainSensorType) {
		this.sensorType = mainSensorType;
	}

	public SensorType getSensorType() {
		return sensorType;
	}
}