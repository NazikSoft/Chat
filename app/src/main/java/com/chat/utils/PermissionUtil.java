package com.chat.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {
    public static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    public static final int PERMISSION_REQUEST_CODE = 1101;

    public static boolean checkPermission(Fragment fragment, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PermissionUtil.isPermissionGranted(fragment.getContext(), permission)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            PermissionUtil.requestPermissionActivity(fragment, permission);
        } else {
            return true;
        }
        return false;
    }

    public static void requestPermissionActivity(Fragment fragment, String permission) {
        fragment.requestPermissions( new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkShouldShowRequestPermission(Fragment fragment, String permission) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
//        }
        return false;
    }
}
