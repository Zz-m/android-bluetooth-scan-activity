package cn.denghanxi.android_bluetooth_scan.lib;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class BluetoothScanActivity extends AppCompatActivity {

    /**
     * 获取蓝牙ble设备
     */
    public static final String EXTRA_DEVICE = "extra_device";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, BluetoothScanFragment.newInstance())
                .commitNow();
    }
}