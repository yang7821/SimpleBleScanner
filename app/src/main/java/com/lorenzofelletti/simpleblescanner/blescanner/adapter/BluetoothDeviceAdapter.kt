package com.lorenzofelletti.simpleblescanner.blescanner.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.R

/**
 * Adapter for the RecyclerView that shows the found BLE devices.
 */
class BluetoothDeviceAdapter(
    private var devices: List<BluetoothDevice>,
    private val onButtonClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnAction: Button = itemView.findViewById(R.id.btn_connect)
        fun bind(device: BluetoothDevice, onButtonClick: (BluetoothDevice) -> Unit) {
            btnAction.setOnClickListener {onButtonClick(device)}
        }

        val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceAdapter.ViewHolder, position: Int) {
        val device = devices[position]
        val textView = holder.deviceNameTextView
        textView.text = device.name
        holder.bind(device, onButtonClick)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun submitList(newDevices: List<BluetoothDevice>) {
        devices = newDevices.filter { !it.name.isNullOrEmpty() }.distinct()
        notifyDataSetChanged()  // Notify the adapter that data has changed
    }
}