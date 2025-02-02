package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.checkPermissions
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnDenied
import com.lorenzofelletti.permissions.dispatcher.dsl.doOnGranted
import com.lorenzofelletti.permissions.dispatcher.dsl.showRationaleDialog
import com.lorenzofelletti.permissions.dispatcher.dsl.withRequestCode
import com.lorenzofelletti.simpleblescanner.BuildConfig.DEBUG
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_CONNECT
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_SCAN
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.BluetoothDeviceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BLEDeviceConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button

    private lateinit var permissionManager: PermissionManager

    private lateinit var foundDevices: MutableStateFlow<List<BluetoothDevice>>

//    private lateinit var graph: Button

    private lateinit var bleScanManager: BleScanManager

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bleScanManager = BleScanManager(this)
        btnStartScan = findViewById(R.id.btn_start_scan)
        permissionManager = PermissionManager(this)
        permissionManager buildRequestResultsDispatcher {
            withRequestCode(BLE_PERMISSION_REQUEST_CODE) {
                checkPermissions(blePermissions)
                showRationaleDialog(getString(R.string.ble_permission_rationale))
                doOnGranted { if (bleScanManager.isScanning.value) {
                    bleScanManager.stopScanning()
                    btnStartScan.text = "Start Scan"
                } else {
                    bleScanManager.startScanning()
                    btnStartScan.text = "Stop Scan"
                }
                }
                doOnDenied {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.ble_permissions_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        // RecyclerView handling
        val rvFoundDevices = findViewById<View>(R.id.rv_found_devices) as RecyclerView
        foundDevices = bleScanManager.foundDevices
        val adapter = BluetoothDeviceAdapter(emptyList()) { device -> handleDeviceButtonClick(device)}
        rvFoundDevices.adapter = adapter

        rvFoundDevices.layoutManager = LinearLayoutManager(this)
        // Adding the onclick listener to the start scan button
        btnStartScan = findViewById(R.id.btn_start_scan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")

            // Checks if the required permissions are granted and starts the scan if so, otherwise it requests them
            permissionManager checkRequestAndDispatch BLE_PERMISSION_REQUEST_CODE
        }
//        graph = findViewById(R.id.btn_graph)

        //Assign a listener to your button
//        graph.setOnClickListener { //Start your second activity
//            val intent = Intent(this, GraphActivity::class.java)
//            startActivity(intent)
//        }

        lifecycleScope.launch {
            foundDevices.collect { devices ->
                adapter.submitList(devices)  // Update the adapter with the new list
            }
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }

    @SuppressLint("Missing Permissions")
    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    private fun handleDeviceButtonClick(device: BluetoothDevice) {
        val intent = Intent(this, GraphActivity::class.java)
        intent.putExtra("BLE Device", device)
        startActivity(intent)
    }
    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val BLE_PERMISSION_REQUEST_CODE = 1
        @RequiresApi(Build.VERSION_CODES.S)
        private val blePermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
}