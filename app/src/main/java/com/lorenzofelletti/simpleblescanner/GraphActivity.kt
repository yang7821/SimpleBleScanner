package com.lorenzofelletti.simpleblescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_CONNECT
import com.lorenzofelletti.simpleblescanner.blescanner.PERMISSION_BLUETOOTH_SCAN
import com.lorenzofelletti.simpleblescanner.blescanner.model.BLEDeviceConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class GraphActivity : AppCompatActivity() {
    private lateinit var graph: Button
    private lateinit var textTVOC: TextView
    private lateinit var device: BluetoothDevice
    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)
    private var read: Float? = null
    private lateinit var servicebtn: TextView
    private lateinit var refreshbtn: TextView
    private val entries = ArrayList<Entry>()
    private var x: Float = 0f
    val times = ArrayList<Long>()


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val (loadedEntries, loadedTimes) = loadEntries()
        entries.addAll(loadedEntries)
        times.addAll(loadedTimes)
        x = times.size.toFloat()

        device = intent.getParcelableExtra("BLE Device")!!
        setActiveDevice(device)
        connectActiveDevice()

        var lineChart = findViewById<LineChart>(R.id.lineChart)
        lineChart.xAxis.valueFormatter = DateValueFormatter(times)// Apply custom formatter
        textTVOC = findViewById(R.id.tvoc)

        val vl = LineDataSet(entries, "TVOC")

        vl.setDrawValues(false)
        vl.setDrawFilled(false)
        vl.lineWidth = 3f
        vl.fillColor = R.color.gray
        vl.fillAlpha = R.color.red

        lineChart.xAxis.labelRotationAngle = 90f
        lineChart.data = LineData(vl)
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        lineChart.xAxis.setTextSize(10f)
        lineChart.xAxis.setDrawAxisLine(true)
        lineChart.xAxis.setDrawGridLines(true)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.text = "Time"

        lineChart.animateX(1800, Easing.EaseInExpo)

        val markerView = CustomMarker(this@GraphActivity, R.layout.marker_view)
        lineChart.marker = markerView

        graph = findViewById(R.id.btn_graph)
        servicebtn = findViewById(R.id.service_button)
        refreshbtn = findViewById(R.id.refresh_btn)

        //Assign a listener to your button
        graph.setOnClickListener { //Start your second activity
            disconnectActiveDevice()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        servicebtn.setOnClickListener {
            discoverActiveDeviceServices()
        }

        refreshbtn.setOnClickListener {
            var vl = LineDataSet(entries, "TVOC")
            vl.setDrawValues(false)
            vl.setDrawFilled(false)
            vl.lineWidth = 3f
            vl.fillColor = R.color.gray
            vl.fillAlpha = R.color.red
            lineChart.data = LineData(vl)
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }

        lifecycleScope.launch {
            activeConnection.collect { device ->
                device?.characteristicRead?.collect { value ->
                    // Get current time in ms
                    textTVOC.text = "Current reading: $value ppb"
                    read = value?.toFloat()
                }
            }
        }

        lifecycleScope.launch {
            while (true) {
                if (read != null) {
                    times.add(System.currentTimeMillis())
                    entries.add(Entry(x, read!!))
                    x += 1
                }
                readCharacteristicFromActiveDevice()
                delay(1500)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        saveEntries()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveEntries()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN])
    fun setActiveDevice(device: BluetoothDevice?) {
        activeConnection.value = device?.run {
            BLEDeviceConnection(this@GraphActivity, device)
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
    fun readCharacteristicFromActiveDevice() {
        activeConnection.value?.readCharacteristic()
    }

    private val sharedPreferences by lazy {
        getSharedPreferences("GraphActivityPrefs", Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    private fun saveEntries() {
        val entriesJson = gson.toJson(entries)
        val timesJson = gson.toJson(times)
        sharedPreferences.edit()
            .putString("entries", entriesJson)
            .putString("times", timesJson)
            .apply()
    }

    private fun loadEntries(): Pair<ArrayList<Entry>, ArrayList<Long>> {
        val entriesJson = sharedPreferences.getString("entries", null)
        val timesJson = sharedPreferences.getString("times", null)

        val entriesType = object : TypeToken<ArrayList<Entry>>() {}.type
        val timesType = object : TypeToken<ArrayList<Long>>() {}.type

        val loadedEntries = if (entriesJson != null) gson.fromJson(entriesJson, entriesType) else ArrayList<Entry>()
        val loadedTimes = if (timesJson != null) gson.fromJson(timesJson, timesType) else ArrayList<Long>()

        return Pair(loadedEntries, loadedTimes)
    }
}

class DateValueFormatter constructor(times: ArrayList<Long>) : ValueFormatter() {
    private val time = times
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()) // Full date and time format

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {

        return dateFormat.format(time[value.toInt()])
    }
}
