package cn.denghanxi.android_bluetooth_scan.lib;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothScanActivity extends AppCompatActivity {

    /**
     * 获取蓝牙ble设备
     */
    public static final String EXTRA_DEVICE = "extra_device";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        if (savedInstanceState != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, BluetoothScanFragment.newInstance())
                    .commitNow();
        }
    }
}