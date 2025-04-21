# An library for Android bluetooth LE scanning

* #### Easy to use
* #### Permissions are handled well

## Get start:
* ### 1 [Setup Jitpack in your project](https://jitpack.io/)


[This lib in JitPack](https://jitpack.io/#Zz-m/android-bluetooth-scan-activity)

* ### 2 Import this lib in your module

```
implementation 'com.github.Zz-m:android-bluetooth-scan-activity:v0.0.7'
```

* ### 3 Register scan launcher in your activity or fragment
```
import cn.denghanxi.android_bluetooth_scan.lib.BluetoothLeDevicePickContract

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        /** setup your activity **/
        
        bluetoothScanLauncher = registerForActivityResult(BluetoothLeDevicePickContract()) { bluetoothDevice ->
            val device = bluetoothDevice
            if (device == null) {
                //User did not pick any BLE device
            } else {
                //Handle the device user picked
            }
        }

        yourButton.setOnClickListener { v: View? ->
            bluetoothScanLauncher.launch(Unit)
        }
    }   
}
```

## Preview
![Preview](https://github.com/Zz-m/android-bluetooth-scan-activity/blob/main/doc/preview.gif?raw=true)

#### Contact me if find any bugs
