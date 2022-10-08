package com.lorenzofelletti.simpleblescanner.blescanner.model

/**
 * A class that represents a BLE device.
 */
data class BleDevice(val name: String) {
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()
        }
    }
}
