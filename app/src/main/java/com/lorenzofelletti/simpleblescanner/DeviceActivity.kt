package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.BuildConfig.DEBUG
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_CONNECT
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.BleDeviceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BLEDeviceConnection
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleScanCallback
import kotlinx.coroutines.flow.MutableStateFlow

class DeviceActivity : AppCompatActivity() {

    // Define your UI components
    private lateinit var buttonConnect: Button
    private lateinit var textDeviceConnected: TextView
    private lateinit var buttonDiscoverServices: Button
    private lateinit var recyclerViewServices: RecyclerView
    private lateinit var buttonReadPassword: Button
    private lateinit var textPassword: TextView
    private lateinit var buttonDisconnect: Button

    // Define your data and state variables
    private var isDeviceConnected: Boolean = false
    private var discoveredCharacteristics: Map<String, List<String>> = emptyMap()
    private var password: String? = null
    private var nameWrittenTimes: Int = 0


    private lateinit var bleScanManager: BleScanManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bleScanManager = BleScanManager(bluetoothManager)
        // Initialize UI components
        buttonConnect = findViewById(R.id.button_connect)
        textDeviceConnected = findViewById(R.id.text_device_connected)
        buttonDiscoverServices = findViewById(R.id.button_discover_services)
        recyclerViewServices = findViewById(R.id.recycler_view_services)
        buttonReadPassword = findViewById(R.id.button_read_password)
        textPassword = findViewById(R.id.text_password)
        buttonDisconnect = findViewById(R.id.button_disconnect)

        // Set up RecyclerView
        recyclerViewServices.layoutManager = LinearLayoutManager(this)
//        recyclerViewServices.adapter = ServicesAdapter(discoveredCharacteristics)

        // Set up button listeners

        buttonConnect.setOnClickListener { if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
            bleScanManager.connectActiveDevice() }
        buttonDiscoverServices.setOnClickListener { if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
            bleScanManager.discoverActiveDeviceServices() }
        buttonReadPassword.setOnClickListener { if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
            bleScanManager.readPasswordFromActiveDevice() }
        buttonDisconnect.setOnClickListener { bleScanManager.disconnectActiveDevice() }


    }
}