package com.lorenzofelletti.simpleblescanner.blescanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A class that represents a BLE device.
 */
@Parcelize
data class BleDevice(val name: String, val address: String) : Parcelable{
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()


        }
    }
}

