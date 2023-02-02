package com.example.bletestapp.bluetoothLe.devices;

import com.example.bletestapp.bluetoothLe.GattAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public enum DeviceType {
	HRM(GattAttributes.SERVICE_HEART_RATE, "Heart rate", UUID.fromString(GattAttributes.CHARACTERISTIC_HEART_RATE_MEASUREMENT)),
	BIKE_SPEED(GattAttributes.SERVICE_CYCLING_SPEED_AND_CADENCE, "Cycle speed", UUID.fromString(GattAttributes.CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_MEASUREMENT)),
	BIKE_CADENCE(GattAttributes.SERVICE_CYCLING_SPEED_AND_CADENCE, "Cycle cadence", UUID.fromString(GattAttributes.CHARACTERISTIC_CYCLING_SPEED_AND_CADENCE_MEASUREMENT)),
	TEMPERATURE(GattAttributes.SERVICE_TEMPERATURE, "Temperature", UUID.fromString(GattAttributes.CHAR_TEMPERATURE_MEASUREMENT)),
	BLOOD_PRESSURE(GattAttributes.SERVICE_BLOOD_PRESSURE, "Blood pressure", UUID.fromString(GattAttributes.CHARACTERISTIC_BP_MEASUREMENT), UUID.fromString(GattAttributes.CHARACTERISTIC_ICP_MEASUREMENT)),
	RUNNING_SPEED(GattAttributes.SERVICE_RUNNING_SPEED_AND_CADENCE, "Running speed", UUID.fromString(GattAttributes.CHARACTERISTIC_RUNNING_SPEED_AND_CADENCE_MEASUREMENT));

	private final String uuidService;
	private final ArrayList<UUID> sensorCharacteristicUUIDs = new ArrayList<>();
	private final String name;

	DeviceType(String uuidService, String name, UUID... uuids) {
		this.uuidService = uuidService;
		this.name = name;
		this.sensorCharacteristicUUIDs.addAll(Arrays.asList(uuids));
	}

	public String getUUIDService() {
		return uuidService;
	}

	public String getName() {
		return name;
	}
}