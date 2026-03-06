package com.toe.tictactoe.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.toe.tictactoe.data.mapper.toBluetoothDeviceDomain
import com.toe.tictactoe.domain.BluetoothController
import com.toe.tictactoe.domain.model.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _isScanStarted = MutableStateFlow(false)
    override val isScanStarted: StateFlow<Boolean>
        get() = _isScanStarted.asStateFlow()


    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()


    private val foundDeviceReceiver = FoundDeviceReceiver()
    private val bondDeviceReceiver = BondDeviceReceiver()

    init {
        foundDeviceReceiver.onDeviceFound = { device ->
            _scannedDevices.update { devices ->
                val newDevice = device.toBluetoothDeviceDomain()
                if (newDevice in devices)
                    devices
                else if (newDevice in _pairedDevices.value)
                    devices
                else
                    devices + newDevice

            }

        }
        foundDeviceReceiver.onScanStatus = { isStarted ->
            _isScanStarted.update { isStarted }

        }
        bondDeviceReceiver.onBondStatus = { isPaired, device ->

            val newDevice = device.toBluetoothDeviceDomain()
            if (isPaired) {
                _scannedDevices.update { devices ->
                    devices.filterNot { it.address == newDevice.address }
                }
                updatePairedDevices()
            } else {
                _scannedDevices.update { devices ->
                    devices.map {
                        if (it.address == newDevice.address)
                            it.copy(isPairing = false)
                        else
                            it
                    }
                }
            }
        }

        updatePairedDevices()

    }


    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return
            }
        } else {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return
            }
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun pairDevice(device: BluetoothDeviceDomain) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                return
            }
        }

        context.registerReceiver(
            bondDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )

        val status = bluetoothAdapter?.getRemoteDevice(device.address)?.createBond()
        if (status == true) {
            _scannedDevices.update { devices ->
                devices.map {
                    if (it.address == device.address)
                        it.copy(isPairing = true)
                    else
                        it
                }
            }
        }
    }

    override fun release() {
        try {
            foundDeviceReceiver.onDeviceFound = null
            foundDeviceReceiver.onScanStatus = null
            bondDeviceReceiver.onBondStatus = null
            context.unregisterReceiver(foundDeviceReceiver)
            context.unregisterReceiver(bondDeviceReceiver)
        } catch (_: Exception) {
        }
    }

    private fun updatePairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                return
            }
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
