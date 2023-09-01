package cn.denghanxi.android_bluetooth_scan.lib;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Created by dhx on 2021/7/14.
 */
class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private final PublishSubject<BluetoothDevice> onClickSubject = PublishSubject.create();

    private final List<BluetoothDevice> deviceList;

    public DeviceListAdapter(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.updateView(device);
        holder.itemView.setOnClickListener(v -> onClickSubject.onNext(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView deviceName;
        private final TextView deviceAddress;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            deviceAddress = itemView.findViewById(R.id.tv_device_address);
        }

        void updateView(BluetoothDevice device) {
            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());
        }
    }

    public Observable<BluetoothDevice> getPositionClicks() {
        return onClickSubject;
    }


}
