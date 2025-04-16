package cn.denghanxi.android_bluetooth_scan.lib

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

/**
 * Created by dhx on 2021/7/14.
 */
internal class BluetoothScanViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(BluetoothScanViewModel::class.java)

    // in
    private val _refreshRequestFlow = MutableSharedFlow<Boolean>()
    private val _isScanFlow = MutableStateFlow<Boolean>(false)

    val isScanFlow: Flow<Boolean> = _isScanFlow

    val startScanFlow: Flow<Boolean> = _refreshRequestFlow.map {
        !_isScanFlow.value
    }

    suspend fun requestRefresh() {
        logger.error("requestRefresh()")
        val r = _refreshRequestFlow.emit(true)
        logger.error("request result:{}", r)
    }

    suspend fun setIsScan(isScan: Boolean) {
        logger.error("setIsScan({})", isScan)
        _isScanFlow.emit(isScan)
    }

}
