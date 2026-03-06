package com.toe.tictactoe.domain

import com.toe.tictactoe.domain.model.BluetoothDeviceDomain
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isScanStarted: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>

    fun pairDevice(device: BluetoothDeviceDomain)
    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}