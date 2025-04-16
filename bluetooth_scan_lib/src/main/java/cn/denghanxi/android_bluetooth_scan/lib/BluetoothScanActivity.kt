package cn.denghanxi.android_bluetooth_scan.lib

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BluetoothScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scan)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, BluetoothScanFragment.newInstance())
                .commitNow()
        }

        val container = findViewById<FrameLayout>(R.id.fragment_container)
        ViewCompat.setOnApplyWindowInsetsListener(
            container,
            OnApplyWindowInsetsListener { v: View, windowInsets: WindowInsetsCompat? ->
                val insets = windowInsets!!.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )
                // Apply the insets as a margin to the view. This solution sets only the
                // bottom, left, and right dimensions, but you can apply whichever insets are
                // appropriate to your layout. You can also update the view padding if that's
                // more appropriate.
                val mlp = v.layoutParams as MarginLayoutParams
                mlp.leftMargin = insets.left
                mlp.bottomMargin = insets.bottom
                mlp.rightMargin = insets.right
                mlp.topMargin = insets.top
                v.setLayoutParams(mlp)
                WindowInsetsCompat.CONSUMED
            })
    }

    companion object {
        /**
         * 获取蓝牙ble设备
         */
        const val EXTRA_DEVICE: String = "extra_device"
    }
}