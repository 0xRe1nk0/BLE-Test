package com.example.bletestapp.interfaces;

import androidx.annotation.Nullable;

public interface HeartRateDataListener {
	void onHeartRateDataUpdate(int heartRateValue, @Nullable String bodyPart);
	void onBloodPressureDataUpdate(float systolic, float diastolic, float arterialPressure, float cuffPressure, int unit, String timestamp, float pulseRate);
}
