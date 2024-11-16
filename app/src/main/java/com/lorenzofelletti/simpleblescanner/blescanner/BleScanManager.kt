package com.lorenzofelletti.simpleblescanner.blescanner

import android.Manifest
import android.app.Application
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.ui.window.application
import androidx.core.app.ActivityCompat
import com.lorenzofelletti.simpleblescanner.BuildConfig.DEBUG
import com.lorenzofelletti.simpleblescanner.blescanner.model.BLEDeviceConnection
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleScanCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


const val PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
const val PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"
/**
 * A manager for bluetooth LE scanning..
 */

class BleScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanCallback: BleScanCallback = BleScanCallback()
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner

    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    var beforeScanActions: MutableList<() -> Unit> = mutableListOf()
    var afterScanActions: MutableList<() -> Unit> = mutableListOf()

    /** True when the manager is performing the scan */
    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Scans for Bluetooth LE devices and stops the scan after [scanPeriod] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevices() {
        fun stopScan() {
            if (DEBUG) Log.d(TAG, "${::scanBleDevices.name} - scan stop")
            scanning = false
            bleScanner.stopScan(scanCallback)

            // execute all the functions to execute after scanning
            executeAfterScanActions()
        }

        // scans for bluetooth LE devices
        if (scanning) {
            stopScan()
        } else {
            // stops scanning after scanPeriod millis
            handler.postDelayed({ stopScan() }, scanPeriod)
            // execute all the functions to execute before scanning
            executeBeforeScanActions()

            // starts scanning
            if (DEBUG) Log.d(TAG, "${::scanBleDevices.name} - scan start")
            scanning = true
            bleScanner.startScan(scanCallback)
        }
    }

    private fun executeBeforeScanActions() {
        executeListOfFunctions(beforeScanActions)
    }

    private fun executeAfterScanActions() {
        executeListOfFunctions(afterScanActions)
    }

    @SuppressLint("MissingPermission")
//    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN])
//    fun setActiveDevice(device: BluetoothDevice?) {
//        activeConnection.value = device?.run { BLEDeviceConnection(application, device) }
//    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connectActiveDevice() {
        activeConnection.value?.connect()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnectActiveDevice() {
        activeConnection.value?.disconnect()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverActiveDeviceServices() {
        activeConnection.value?.discoverServices()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readPasswordFromActiveDevice() {
        activeConnection.value?.readPassword()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun writeNameToActiveDevice() {
        activeConnection.value?.writeName()
    }


    companion object {
        private val TAG = BleScanManager::class.java.simpleName

        /**
         * Constant holding the default max scan period time, i.e. the max number of millis
         * scanning will be performed.
         */
        const val DEFAULT_SCAN_PERIOD: Long = 10000

        /**
         * Function that executes a list of functions taking no arguments and returning [Unit].
         *
         * @param toExecute The list of functions to execute
         */
        private fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }
}