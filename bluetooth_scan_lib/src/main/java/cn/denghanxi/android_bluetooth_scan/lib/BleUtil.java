package cn.denghanxi.android_bluetooth_scan.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dhx on 2022/3/17.
 */
class BleUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BleUtil.class);

    public static final String[] blePermissions;

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            blePermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        } else {
            blePermissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        }
    }

    public static boolean checkBlePermission(Context context) {
        boolean result = true;
        for (String permission : blePermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                LOGGER.info("Permission:{} denied.", permission);
                result = false;
            }
        }
        return result;
    }
}
