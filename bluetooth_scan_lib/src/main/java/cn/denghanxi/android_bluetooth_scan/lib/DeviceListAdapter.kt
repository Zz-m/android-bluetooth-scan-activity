package cn.denghanxi.android_bluetooth_scan.lib

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by dhx on 2021/7/14.
 */
internal class DeviceListAdapter(private val deviceList: MutableList<BluetoothDevice>) :
    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
    private var myScope = MainScope()
    private val onClickFlow = MutableSharedFlow<BluetoothDevice>()

    val onDeviceSelectedFlow: Flow<BluetoothDevice> = onClickFlow

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ble_device, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        holder.updateView(device)
        holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
            myScope.launch {
                onClickFlow.emit(device)
            }
        })
    }


    override fun getItemCount(): Int {
        return deviceList.size
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        myScope.cancel()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById<TextView>(R.id.tv_device_name)
        private val deviceAddress: TextView =
            itemView.findViewById<TextView>(R.id.tv_device_address)

        @SuppressLint("MissingPermission")
        fun updateView(device: BluetoothDevice) {
            deviceName.text = device.getName()
            deviceAddress.text = device.getAddress()
        }
    }

}
