package com.example.bletestapp.bluetoothLe.sensors.formatter;

import java.util.Locale;

public class SpeedFormatter implements BleSensorFormatter<Number> {
	@Override
	public String format(Number speedMps) {
		if (speedMps == null) {
			return "-";
		}

		double value = 0;
		value = speedMps.doubleValue() * 3.6;
		//value = speedMps.doubleValue() * 2.23693629;

		return String.format(Locale.US, "%.1f", value);
	}
}
