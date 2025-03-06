package cn.denghanxi.android_bluetooth_scan

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.denghanxi.android_bluetooth_scan.databinding.ActivityMainBinding
import cn.denghanxi.android_bluetooth_scan.lib.BluetoothScanActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bluetoothScanLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val device =
                        result.data!!.getParcelableExtra<BluetoothDevice>(
                            BluetoothScanActivity.EXTRA_DEVICE
                        )
                    if (device != null) {
                        val address = device.address
                        Log.d(TAG, "获取address: $address")
                    } else {
                        Log.e(TAG, "返回device null！")
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "获取蓝牙设备失败",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setupView()
    }

    private fun setupView() {
        binding.btnMain.setOnClickListener { v: View? ->
            val intent = Intent(
                this,
                BluetoothScanActivity::class.java
            )
            bluetoothScanLauncher.launch(intent)
        }
    }

    companion object {
        private val TAG: String = MainActivity::class.java.name
    }
}