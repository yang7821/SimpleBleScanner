package com.lorenzofelletti.simpleblescanner.blescanner.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.R

class ServiceAdapter (
    private var discoveredCharacteristics: Map<String, List<String>>
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {
    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textServiceUuid: TextView = itemView.findViewById(R.id.service_uuid)
        val textCharacteristics: TextView = itemView.findViewById(R.id.characteristc_name)

        /**
         * Binds the service and its characteristics to the UI elements.
         */
        fun bind(serviceUuid: String, characteristics: List<String>) {
            textServiceUuid.text = "Service: $serviceUuid"
            textCharacteristics.text = characteristics.joinToString(separator = "\n") { "- $it" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.characteristic_row_layout, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val serviceUuid = discoveredCharacteristics.keys.elementAt(position)
        val characteristics = discoveredCharacteristics[serviceUuid].orEmpty()
        Log.d("ServiceAdapter", "Binding service: $serviceUuid, Characteristics: $characteristics")
        holder.bind(serviceUuid, characteristics)
    }

    override fun getItemCount(): Int = discoveredCharacteristics.size

    fun updateData(newData: Map<String, List<String>>) {
        discoveredCharacteristics = newData
        notifyDataSetChanged() // Notify RecyclerView to refresh the list
    }


}