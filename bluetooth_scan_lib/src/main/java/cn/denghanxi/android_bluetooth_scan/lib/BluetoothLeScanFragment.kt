package cn.denghanxi.android_bluetooth_scan.lib

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.denghanxi.android_bluetooth_scan.lib.BleUtil.checkBlePermission
import cn.denghanxi.android_bluetooth_scan.lib.databinding.FragmentBluetoothLeScanBinding
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A simple [Fragment] subclass.
 * Use the [BluetoothLeScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BluetoothLeScanFragment : Fragment() {
    private val logger: Logger = LoggerFactory.getLogger(BluetoothLeScanFragment::class.java)
    private val viewModel: BluetoothLeScanViewModel by activityViewModels()

    private lateinit var binding: FragmentBluetoothLeScanBinding

    private lateinit var bluetoothDeviceEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var blePermissionRequestLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val handler = Handler(Looper.getMainLooper())

    // list
    private val bluetoothDeviceList: MutableList<BluetoothDevice> = ArrayList<BluetoothDevice>()
    private val recyclerViewAdapter = DeviceListAdapter(bluetoothDeviceList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothDeviceEnableLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), enableDeviceCallback
        )
        blePermissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), requestPermissionsCallback
        )

        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothLeScanBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onStart() {
        super.onStart()
        makeBluetoothAvailable()
    }

    override fun onStop() {
        super.onStop()
        stopScanDevice()
    }

    private fun setupView() {
        // list
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setLayoutManager(layoutManager)
        binding.recyclerView.setAdapter(recyclerViewAdapter)
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerView.context, layoutManager.orientation
        )
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        // swipe refresh
        binding.layoutRefresh.setColorSchemeColors(Color.CYAN, Color.DKGRAY, Color.YELLOW)
        binding.layoutRefresh.setOnRefreshListener {

            lifecycleScope.launch {
                viewModel.requestRefresh()
            }
        }
    }

    fun setupViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //loading ui
                launch {
                    viewModel.isScanFlow.collect { isScan ->
                        binding.progressBar.visibility = if (isScan) View.VISIBLE else View.GONE
                        binding.layoutRefresh.isRefreshing = isScan
                    }
                }
                //handle refresh request
                launch {
                    viewModel.startScanFlow.collect { startScan ->
                        if (startScan) {
                            logger.debug("Now start scan bluetooth device...")
                            startScanDevice()
                        } else {
                            logger.debug("Already scanning, not restart")
                        }
                    }
                }
                //Device selected
                launch {
                    recyclerViewAdapter.onDeviceSelectedFlow.collect { device ->
                        val data = Intent()
                        data.putExtra(BluetoothLeScanActivity.EXTRA_DEVICE, device)
                        requireActivity().setResult(Activity.RESULT_OK, data)

                        //stop scan
                        context?.let {
                            if (checkBlePermission(it)) {
                                bluetoothAdapter.bluetoothLeScanner?.apply {
                                    stopScan(leScanCallback)
                                }
                            }
                        }
                        viewModel.setIsScan(false)

                        //finish activity
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun makeBluetoothAvailable() {
        if (!bluetoothAdapter.isEnabled) {
            if (checkBlePermission(requireContext())) {
                //enable bluetooth adapter require permission
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothDeviceEnableLauncher.launch(enableBtIntent)
            } else {
                checkPermissionAndStartScan()
            }
        } else {
            checkPermissionAndStartScan()
        }
    }

    private fun checkPermissionAndStartScan() {
        if (checkBlePermission(requireContext())) {
            startScanDevice()
        } else {
            val messageStringRes = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                R.string.scan_request_permission_location
            } else {
                R.string.scan_request_permission_bluetooth_and_location
            }
            AlertDialog.Builder(requireContext()).setMessage(messageStringRes).setPositiveButton(
                R.string.ok
            ) { dialog: DialogInterface?, which: Int ->
                blePermissionRequestLauncher.launch(BleUtil.blePermissions)
            }.setNegativeButton(
                R.string.cancel,
                (DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> requireActivity().finish() })
            ).setCancelable(false).show()
        }
    }

    private fun startScanDevice() {
        if (!checkBlePermission(this.requireContext())) {
            Toast.makeText(
                requireContext(),
                R.string.warning_bluetooth_scan_permission_required,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val scanner = bluetoothAdapter.getBluetoothLeScanner()
        if (scanner == null) {
            logger.warn("scanner is null")
            return
        }
        // Stops scanning after a pre-defined scan period.
        handler.postDelayed({
            logger.debug("stop scan bluetooth device...")
            stopScanDevice()
        }, SCAN_PERIOD)
        logger.debug("start scan bluetooth device...")
        lifecycleScope.launch {
            viewModel.setIsScan(true)
            scanner.startScan(leScanCallback)
        }
    }

    private fun stopScanDevice() {
        context?.let { context ->
            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (checkBlePermission(context)) {
                scanner?.stopScan(leScanCallback)
                lifecycleScope.launch {
                    viewModel.setIsScan(false)
                }
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val context = getContext()
            if (context == null) {
                logger.warn("onScanResult, but context is finalized.")
                return
            }
            checkBlePermission(context)
            val device = result.device
            logger.debug("-------------------------")
            logger.debug("name:{}", device.getName())
            logger.debug("address:{}", device.getAddress())
            logger.debug("bond:{}", device.getBondState())

            var addFlag = true
            for (savedDevice in bluetoothDeviceList) {
                if (savedDevice.getAddress() == device.getAddress()) {
                    addFlag = false
                    break
                }
            }
            if (addFlag && !TextUtils.isEmpty(device.getName())) {
                bluetoothDeviceList.add(device)
                recyclerViewAdapter.notifyItemInserted(bluetoothDeviceList.size - 1)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            logger.warn("scan failed code:{}", errorCode)
        }
    }

    private val enableDeviceCallback = ActivityResultCallback { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            logger.debug("Enable bluetooth adapter success")
            val bluetoothManager =
                requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter
            bluetoothAdapter.let {
                if (it.isEnabled) {
                    checkPermissionAndStartScan()
                } else {
                    logger.error("bluetoothAdapter，still not enabled")
                    Toast.makeText(
                        requireContext(),
                        R.string.warning_fail_to_enable_bluetooth_adapter,
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().finish()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.warning_fail_to_enable_bluetooth_adapter,
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
        }
    }

    private val requestPermissionsCallback =
        ActivityResultCallback { resultMap: Map<String, Boolean> ->
            val success = AtomicBoolean(true)
            val deniedSet: MutableSet<String> = HashSet<String>()
            resultMap.forEach { (key: String?, value: Boolean?) ->
                logger.debug("Permission:{}, Granted = {}", key, value)
                if (!value!!) {
                    success.set(false)
                    deniedSet.add(key!!)
                }
            }
            if (!success.get()) {
                var neverAskAgain = false
                val neverAskSet: MutableSet<String?> = HashSet<String?>()
                for (permission in deniedSet) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        neverAskSet.add(permission)
                        neverAskAgain = true
                    }
                }
                if (neverAskAgain) {
                    val messageRes =
                        if (neverAskSet.contains(Manifest.permission.BLUETOOTH_SCAN) || neverAskSet.contains(
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        ) {
                            R.string.permission_rationale_bluetooth_scan_and_connect
                        } else {
                            R.string.permission_rationale_location
                        }
                    AlertDialog.Builder(requireContext()).setMessage(messageRes).setPositiveButton(
                        R.string.settings,
                        (DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts(
                                "package", requireContext().packageName, null
                            )
                            intent.setData(uri)
                            startActivity(intent)
                        })
                    ).setNegativeButton(
                        R.string.cancel,
                        (DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> requireActivity().finish() })
                    ).setCancelable(false).show()
                } else {
                    checkPermissionAndStartScan()
                }
            } else {
                checkPermissionAndStartScan()
            }
        }

    companion object {
        // Stops scanning after 10 seconds.
        private const val SCAN_PERIOD: Long = 10000
        fun newInstance(): BluetoothLeScanFragment {
            val fragment = BluetoothLeScanFragment()
            return fragment
        }
    }
}