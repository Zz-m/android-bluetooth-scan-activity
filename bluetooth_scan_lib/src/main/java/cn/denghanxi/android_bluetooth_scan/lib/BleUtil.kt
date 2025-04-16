package cn.denghanxi.android_bluetooth_scan.lib

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by dhx on 2022/3/17.
 */
internal object BleUtil {
    private val LOGGER: Logger = LoggerFactory.getLogger(BleUtil::class.java)

    @JvmField
    val blePermissions: Array<String> = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    } else {
        arrayOf<String>(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    @JvmStatic
    fun checkBlePermission(context: Context): Boolean {
        var result = true
        for (permission in blePermissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                LOGGER.info("Permission:{} denied.", permission)
                result = false
            }
        }
        return result
    }
}
