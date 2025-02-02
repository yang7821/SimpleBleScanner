package com.lorenzofelletti.simpleblescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Intent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class DeviceActivity : AppCompatActivity() {
    private var timer: Timer? = null

    // Define your UI components
    private lateinit var buttonConnect: Button
    private lateinit var textDeviceConnected: TextView
    private lateinit var buttonDiscoverServices: Button
    private lateinit var recyclerViewServices: RecyclerView
    private lateinit var buttonReadCharateristic: Button
    private lateinit var textCharacteristic: TextView
    private lateinit var buttonDisconnect: Button

    // Define your data and state variables
    private lateinit var device: BluetoothDevice
    private var curServices: List<BluetoothGattService> = emptyList()
    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        device = intent.getParcelableExtra("BLE Device")!!
        // Initialize UI components
        buttonConnect = findViewById(R.id.button_connect)
        textDeviceConnected = findViewById(R.id.text_device_connected)
        buttonDiscoverServices = findViewById(R.id.button_discover_services)
        recyclerViewServices = findViewById(R.id.recycler_view_services)
        buttonReadCharateristic = findViewById(R.id.button_read_password)
        textCharacteristic = findViewById(R.id.text_character)
        buttonDisconnect = findViewById(R.id.button_disconnect)

        // Set up RecyclerView
        val adapter = ServiceAdapter(emptyList())
        recyclerViewServices.adapter = adapter
        recyclerViewServices.layoutManager = LinearLayoutManager(this)

        startRepeatingTask()
        // Set up button listeners
        setActiveDevice(device)
        connectActiveDevice()
        discoverActiveDeviceServices()

        buttonConnect.setOnClickListener {
            setActiveDevice(device)
            connectActiveDevice()
        }
        buttonDiscoverServices.setOnClickListener { discoverActiveDeviceServices() }
        buttonReadCharateristic.setOnClickListener { readCharacteristicFromActiveDevice() }
        buttonDisconnect.setOnClickListener {
            disconnectActiveDevice()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            activeConnection.collect { device ->
                device?.isConnected?.collect { connected ->
                    textDeviceConnected.text = "Device connected: $connected"
                    buttonDiscoverServices.isEnabled = connected
                    buttonDisconnect.isEnabled = connected
                    buttonReadCharateristic.isEnabled = connected

                    if (!connected) {
                        adapter.updateData(emptyList()) // Clear services
                    }
                }
            }
        }

        lifecycleScope.launch {
            activeConnection.collect { device ->
                device?.services?.collect { deviceServices ->
                    curServices = deviceServices
                    adapter.updateData(curServices)
                }
            }
        }

        lifecycleScope.launch {
            activeConnection.collect { device ->
                device?.characteristicRead?.collect { value ->
                    textCharacteristic.text = "Characteristic value: $value"
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startRepeatingTask() {
        timer = Timer()
        timer?.schedule(0, 1000) { // Delay 0ms, repeat every 1000ms (1 second)
            runOnUiThread {
                readCharacteristicFromActiveDevice()
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
        activeConnection.value?.discoverServices()
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
    fun readCharacteristicFromActiveDevice() {
        activeConnection.value?.readCharacteristic()
    }

}