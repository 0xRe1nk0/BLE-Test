package com.example.bletestapp.interfaces;

import androidx.annotation.NonNull;

public interface PermissionRequestListener {
	void onRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
