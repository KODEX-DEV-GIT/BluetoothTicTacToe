package com.toe.tictactoe.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class FoundDeviceReceiver() : BroadcastReceiver() {

    var onDeviceFound: ((BluetoothDevice) -> Unit)? = null
    var onScanStatus: ((isStarted: Boolean) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let {
                    onDeviceFound?.invoke(it)
                }
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
               onScanStatus?.invoke(true)
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onScanStatus?.invoke(false)
            }
        }
    }
}