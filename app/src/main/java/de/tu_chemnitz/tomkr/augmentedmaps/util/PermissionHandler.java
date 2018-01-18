package de.tu_chemnitz.tomkr.augmentedmaps.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;

/**
 * Created by Tom Kretzschmar on 18.09.2017.
 *
 */

public class PermissionHandler implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private WeakReference<Activity> activityWeakReference;

    public PermissionHandler(Activity activity){
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(activityWeakReference.get(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activityWeakReference.get(), Manifest.permission.CAMERA)) {
//            showToast("Camerapermission needed");
            } else {
                ActivityCompat.requestPermissions(activityWeakReference.get(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
        if (ContextCompat.checkSelfPermission(activityWeakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activityWeakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION)) {
//            showToast("Camerapermission needed");
            } else {
                ActivityCompat.requestPermissions(activityWeakReference.get(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                showToast("No permission for Camera granted");
            }
        } else {
            activityWeakReference.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
