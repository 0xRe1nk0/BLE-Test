package com.example.bletestapp.bluetoothLe.sensors;

import com.example.bletestapp.bluetoothLe.sensors.formatter.BleSensorFormatter;
import com.example.bletestapp.bluetoothLe.sensors.formatter.CadenceFormatter;
import com.example.bletestapp.bluetoothLe.sensors.formatter.IntegerFormatter;
import com.example.bletestapp.bluetoothLe.sensors.formatter.SpeedFormatter;

public enum SensorType {

	CADENCE(new CadenceFormatter()),
	HEART_RATE(new IntegerFormatter()),
	SPEED_MPS(new SpeedFormatter());

	private final BleSensorFormatter formatter;

	SensorType(BleSensorFormatter formatter) {
		this.formatter = formatter;
	}

	public BleSensorFormatter getFormatter() {
		return formatter;
	}
}
