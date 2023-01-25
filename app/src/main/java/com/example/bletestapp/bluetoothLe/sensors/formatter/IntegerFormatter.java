package com.example.bletestapp.bluetoothLe.sensors.formatter;

public class IntegerFormatter implements BleSensorFormatter<Number> {

	@Override
	public String format(Number value) {
		return value == null ? "-" : String.valueOf(value.intValue());
	}
}
