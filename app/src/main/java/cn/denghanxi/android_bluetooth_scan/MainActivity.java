package cn.denghanxi.android_bluetooth_scan;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import cn.denghanxi.android_bluetooth_scan.databinding.ActivityMainBinding;
import cn.denghanxi.android_bluetooth_scan.lib.BluetoothScanActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Intent> bluetoothScanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bluetoothScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            BluetoothDevice device = result.getData().getParcelableExtra(BluetoothScanActivity.EXTRA_DEVICE);
                            if (device != null) {
                                String address = device.getAddress();
                                Log.d(TAG, "获取address: " + address);
                            } else {
                                Log.e(TAG, "返回device null！");
                            }
                        }
                    } else {
                        Toast.makeText(this, "获取蓝牙设备失败", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        setupView();
    }

    private void setupView() {
        binding.btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, BluetoothScanActivity.class);
            bluetoothScanLauncher.launch(intent);
        });
    }
}