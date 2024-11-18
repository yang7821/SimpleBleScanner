package com.lorenzofelletti.simpleblescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_CONNECT
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_SCAN
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.ServiceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BLEDeviceConnection
import com.lorenzofelletti.simpleblescanner.blescanner.model.CTF_SERVICE_UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
    private lateinit var device: BluetoothDevice
    private var discoveredCharacteristics: Map<String, List<String>> = emptyMap()
    private var curServices: List<BluetoothGattService> = emptyList()
    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    val foundTargetService = discoveredCharacteristics.contains(CTF_SERVICE_UUID.toString())
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        device = intent.getParcelableExtra("BLE Device")!!
        discoveredCharacteristics = curServices.associate { service -> Pair(service.uuid.toString(), service.characteristics.map { it.uuid.toString() }) }
        // Initialize UI components
        buttonConnect = findViewById(R.id.button_connect)
        textDeviceConnected = findViewById(R.id.text_device_connected)
        buttonDiscoverServices = findViewById(R.id.button_discover_services)
        recyclerViewServices = findViewById<RecyclerView>(R.id.recycler_view_services)
        buttonReadPassword = findViewById(R.id.button_read_password)
        textPassword = findViewById(R.id.text_password)
        buttonDisconnect = findViewById(R.id.button_disconnect)

        // Set up RecyclerView
        val adapter = ServiceAdapter(discoveredCharacteristics)
        recyclerViewServices.adapter = adapter
        recyclerViewServices.layoutManager = LinearLayoutManager(this)

        // Set up button listeners

        buttonConnect.setOnClickListener {
            setActiveDevice(device)
            connectActiveDevice()
        }
        buttonDiscoverServices.setOnClickListener { discoverActiveDeviceServices() }
        buttonReadPassword.setOnClickListener { readPasswordFromActiveDevice() }
        buttonDisconnect.setOnClickListener { disconnectActiveDevice() }

        lifecycleScope.launch {
            activeConnection.collect { connection ->
                connection?.isConnected?.collect { connected ->
                    textDeviceConnected.text = "Device connected: $connected"
                    buttonDiscoverServices.isEnabled = connected
                    buttonDisconnect.isEnabled = connected
                }
            }
        }
        lifecycleScope.launch {
            activeConnection.collect { service ->
                service?.services?.collect { deviceServices ->
                    curServices = deviceServices
                }
                discoveredCharacteristics = curServices.associate { service ->
                    Pair(service.uuid.toString(), service.characteristics.map { it.uuid.toString() })
                }

                (recyclerViewServices.adapter as ServiceAdapter).updateData(discoveredCharacteristics)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN])
    fun setActiveDevice(device: BluetoothDevice?) {
        activeConnection.value = device?.run {
            BLEDeviceConnection(this@DeviceActivity, device)
        }
    }

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


}