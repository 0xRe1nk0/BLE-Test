package com.example.bletestapp.bluetoothLe.sensors.formatter;

public class CadenceFormatter implements BleSensorFormatter<Number> {
	@Override
	public String format(Number value) {
		return value == null ? "-" : Integer.toString(value.intValue());
	}
}
