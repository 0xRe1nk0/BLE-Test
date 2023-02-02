package com.example.bletestapp.interfaces;


public interface RunningSpeedDataListener {
	void onRunningSpeedDataUpdate(float speed, int cadence, float totalDistance, float strideLength, boolean isRunning);
}
