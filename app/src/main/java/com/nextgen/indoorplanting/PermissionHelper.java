package com.nextgen.indoorplanting;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String REQUIRED_PERMISSION = android.Manifest.permission.CAMERA;

    private final Activity activity;

    public PermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(activity, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{REQUIRED_PERMISSION}, REQUEST_CODE_PERMISSIONS);
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean shouldShowRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSION);
    }

    public void showPermissionsRequiredToast() {
        Toast.makeText(activity, "Camera permission is required to run this app.", Toast.LENGTH_LONG).show();
    }
}
