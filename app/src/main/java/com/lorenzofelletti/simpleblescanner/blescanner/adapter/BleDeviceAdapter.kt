package com.lorenzofelletti.simpleblescanner.blescanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.R
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice

/**
 * Adapter for the RecyclerView that shows the found BLE devices.
 */
class BleDeviceAdapter(
    private val devices: List<BleDevice>,
    private val onButtonClick: (BleDevice) -> Unit
) : RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnAction: Button = itemView.findViewById(R.id.btn_connect)
        fun bind(device: BleDevice, onButtonClick: (BleDevice) -> Unit) {
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

    override fun onBindViewHolder(holder: BleDeviceAdapter.ViewHolder, position: Int) {
        val device = devices[position]
        val textView = holder.deviceNameTextView
        textView.text = device.name
        holder.bind(device, onButtonClick)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

}