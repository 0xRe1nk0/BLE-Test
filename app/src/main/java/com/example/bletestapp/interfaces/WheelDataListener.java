package com.example.bletestapp.interfaces;

public interface WheelDataListener {
	void onWheelDataUpdate(float speed, float distance, float totalDistance);
	void onCadenceDataUpdate(float gearRatio, float cadence);
}
