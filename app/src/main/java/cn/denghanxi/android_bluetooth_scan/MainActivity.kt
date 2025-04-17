package cn.denghanxi.android_bluetooth_scan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import cn.denghanxi.android_bluetooth_scan.databinding.ActivityMainBinding
import cn.denghanxi.android_bluetooth_scan.lib.BluetoothDevicePickContract

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bluetoothScanLauncher =
            registerForActivityResult(BluetoothDevicePickContract()) { bluetoothDevice ->
                val device = bluetoothDevice
                if (device == null) {
                    Toast.makeText(this, R.string.fail_to_pick_device, Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvMain.text =
                        String.format(getString(R.string.picked_device), device.address)
                }
            }

        setupView()
    }

    private fun setupView() {
        binding.btnMain.setOnClickListener { v: View? ->

            bluetoothScanLauncher.launch(Unit)
        }
    }

}