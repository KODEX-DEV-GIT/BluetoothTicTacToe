package com.toe.tictactoe.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import com.toe.tictactoe.domain.model.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address,
        type = getDeviceType(this),
        isPairing = false
    )
}

@SuppressLint("MissingPermission")
fun getDeviceType(device: BluetoothDevice): String {
    val bluetoothClass = device.bluetoothClass ?: return "Unknown"

    return when (bluetoothClass.majorDeviceClass) {
        BluetoothClass.Device.Major.PHONE -> "Phone"
        BluetoothClass.Device.Major.COMPUTER -> "Computer"
        BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio/Video"
        BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
        BluetoothClass.Device.Major.IMAGING -> "Imaging"
        BluetoothClass.Device.Major.NETWORKING -> "Networking"
        BluetoothClass.Device.Major.HEALTH -> "Health"
        BluetoothClass.Device.Major.WEARABLE -> "Wearable"
        else -> "Other"
    }
}