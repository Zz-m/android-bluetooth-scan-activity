package cn.denghanxi.android_bluetooth_scan.lib

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BluetoothLeScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_le_scan)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, BluetoothLeScanFragment.newInstance())
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
         * Extra for the selected device
         */
        const val EXTRA_DEVICE: String = "extra_device"
    }
}

class BluetoothLeDevicePickContract : ActivityResultContract<Unit, BluetoothDevice?>() {
    override fun createIntent(
        context: Context,
        input: Unit
    ): Intent {
        return Intent(context, BluetoothLeScanActivity::class.java)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): BluetoothDevice? {
        return if (resultCode != Activity.RESULT_OK) {
            null
        } else {
            intent?.getParcelableExtra(BluetoothLeScanActivity.EXTRA_DEVICE)
        }
    }

}