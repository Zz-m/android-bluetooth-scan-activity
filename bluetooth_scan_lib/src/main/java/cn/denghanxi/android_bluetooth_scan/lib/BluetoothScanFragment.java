package cn.denghanxi.android_bluetooth_scan.lib;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.denghanxi.android_bluetooth_scan.lib.databinding.FragmentBluetoothScanBinding;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BluetoothScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothScanFragment extends Fragment {

    private final Logger logger = LoggerFactory.getLogger(BluetoothScanFragment.class);
    private final CompositeDisposable disposables = new CompositeDisposable();

    private FragmentBluetoothScanBinding binding;
    private BluetoothScanViewModel viewModel;

    private ActivityResultLauncher<Intent> bluetoothDeviceEnableLauncher;
    private ActivityResultLauncher<String[]> blePermissionRequestLauncher;
    private BluetoothAdapter bluetoothAdapter;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // list
    private final List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private final DeviceListAdapter recyclerViewAdapter = new DeviceListAdapter(bluetoothDeviceList);

    public BluetoothScanFragment() {
        // Required empty public constructor
    }

    public static BluetoothScanFragment newInstance() {
        BluetoothScanFragment fragment = new BluetoothScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(BluetoothScanViewModel.class);
        bluetoothDeviceEnableLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), enableDeviceCallback);
        blePermissionRequestLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), requestPermissionsCallback);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupObserver();
        makeBluetoothAvailable();
    }

    @Override
    public void onStop() {
        super.onStop();
        disposables.clear();
    }


    private void setupView() {
        // list
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerViewAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerView.getContext(), layoutManager.getOrientation());
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        // swipe refresh
        binding.layoutRefresh.setColorSchemeColors(Color.CYAN, Color.DKGRAY, Color.YELLOW);
        binding.layoutRefresh.setOnRefreshListener(() -> viewModel.refreshRequest.onNext(true));
    }

    void setupObserver() {
        disposables.add(recyclerViewAdapter.getPositionClicks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(device -> {
                    Intent data = new Intent();
                    data.putExtra(BluetoothScanActivity.EXTRA_DEVICE, device);
                    requireActivity().setResult(Activity.RESULT_OK, data);
                    //stop scan
                    if (BleUtil.checkBlePermission(this.requireContext())) {
                        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                        scanner.stopScan(leScanCallback);
                    }
                    viewModel.loadingStatus.onNext(false);
                    //finish activity
                    requireActivity().finish();
                }, e -> logger.error("点击事件异常:", e)));
        disposables.add(viewModel.loadingStatus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isLoading -> {
                    binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                    binding.layoutRefresh.setRefreshing(isLoading);
                }, e -> logger.error("err:", e)));
        disposables.add(viewModel.refreshManager
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(b -> {
                    if (b) {
                        logger.debug("开始刷新蓝牙");
                        startScanDevice();
                    } else {
                        logger.debug("正在刷新蓝牙，无需再次刷新。");
                    }
                }, e -> logger.error("err:", e)));
    }

    private void makeBluetoothAvailable() {
        final BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothDeviceEnableLauncher.launch(enableBtIntent);
        } else {
            checkPermissionAndStartScan();
        }
    }

    private void checkPermissionAndStartScan() {
        if (BleUtil.checkBlePermission(requireContext())) {
            startScanDevice();
        } else {
            String message;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                message = "扫描附近蓝牙设备需要定位权限";
            } else {
                message = "扫描附近蓝牙设备需要蓝牙与定位权限";
            }
            new AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton("好", (dialog, which) -> blePermissionRequestLauncher.launch(BleUtil.blePermissions))
                    .setNegativeButton("取消", ((dialog, which) -> requireActivity().finish()))
                    .setCancelable(false)
                    .show();
        }
    }

    private void startScanDevice() {
        if (!BleUtil.checkBlePermission(this.requireContext())) {
            Toast.makeText(requireContext(), "需要蓝牙与定位权限以扫描周围设备...", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(() -> {
            logger.debug("stop scan bluetooth device...");
            scanner.stopScan(leScanCallback);
            viewModel.loadingStatus.onNext(false);
        }, SCAN_PERIOD);
        logger.debug("start scan bluetooth device...");
        viewModel.loadingStatus.onNext(true);
        scanner.startScan(leScanCallback);
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BleUtil.checkBlePermission(BluetoothScanFragment.this.requireContext());
            BluetoothDevice device = result.getDevice();
            logger.debug("-------------------------");
            logger.debug("name:{}", device.getName());
            logger.debug("address:{}", device.getAddress());
            logger.debug("bond:{}", device.getBondState());

            boolean addFlag = true;
            for (BluetoothDevice savedDevice : bluetoothDeviceList) {
                if (savedDevice.getAddress().equals(device.getAddress())) {
                    addFlag = false;
                    break;
                }
            }
            if (addFlag && !TextUtils.isEmpty(device.getName())) {
                bluetoothDeviceList.add(device);
                recyclerViewAdapter.notifyItemChanged(bluetoothDeviceList.size() - 1);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            logger.error("scan failed code:{}", errorCode);
        }
    };

    private final ActivityResultCallback<ActivityResult> enableDeviceCallback = result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            logger.debug("打开蓝牙设备成功");
            final BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                logger.error("重新获取bluetoothAdapter，任然异常");
                Toast.makeText(requireContext(), "打开蓝牙设备失败", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else {
                checkPermissionAndStartScan();
            }
        } else {
            Toast.makeText(requireContext(), "打开设备蓝牙失败", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }
    };

    private final ActivityResultCallback<Map<String, Boolean>> requestPermissionsCallback = resultMap -> {
        AtomicBoolean success = new AtomicBoolean(true);
        Set<String> deniedSet = new HashSet<>();
        resultMap.forEach((key, value) -> {
            logger.debug("Permission:{}, Granted = {}", key, value);
            if (!value) {
                success.set(false);
                deniedSet.add(key);
            }
        });
        if (!success.get()) {
            boolean neverAskAgain = false;
            Set<String> neverAskSet = new HashSet<>();
            for (String permission : deniedSet) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    neverAskSet.add(permission);
                    neverAskAgain = true;
                }
            }
            if (neverAskAgain) {
                String message;
                if (neverAskSet.contains(Manifest.permission.BLUETOOTH_SCAN) || neverAskSet.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
                    message = "缺少权限，请在设置中允许蓝牙及连接附近设备权限";
                } else {
                    message = "扫描蓝牙设备需要位置权限，请在设置中开启位置权限";
                }
                new AlertDialog.Builder(requireContext())
                        .setMessage(message)
                        .setPositiveButton("去设置", ((dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }))
                        .setNegativeButton("放弃", ((dialog, which) -> requireActivity().finish()))
                        .setCancelable(false)
                        .show();
            } else {
                checkPermissionAndStartScan();
            }
        } else {
            checkPermissionAndStartScan();
        }
    };
}