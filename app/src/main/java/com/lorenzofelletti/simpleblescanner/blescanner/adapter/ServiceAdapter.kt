package com.lorenzofelletti.simpleblescanner.blescanner.adapter

import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.R

class ServiceAdapter (
    private var services: List<BluetoothGattService>
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {
    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textServiceUuid: TextView = itemView.findViewById(R.id.service_uuid)
        val textCharacteristics: TextView = itemView.findViewById(R.id.characteristc_name)

        /**
         * Binds the service and its characteristics to the UI elements.
         */
        fun bind(service: BluetoothGattService) {
            val serviceUuid = service.uuid.toString()
            val characteristics = service.characteristics.map { it.uuid.toString() }

            textServiceUuid.text = "Service: $serviceUuid"
            textCharacteristics.text = if (characteristics.isNotEmpty()) {
                characteristics.joinToString(separator = "\n") { "- $it" }
            } else {
                "No characteristics found."
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.characteristic_row_layout, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int = services.size

    fun updateData(newServices: List<BluetoothGattService>) {
        services = newServices
        notifyDataSetChanged() // Notify RecyclerView to refresh the list
    }


}