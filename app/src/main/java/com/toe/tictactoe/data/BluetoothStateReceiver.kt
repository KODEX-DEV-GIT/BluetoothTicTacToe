package com.toe.tictactoe.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BluetoothStateReceiver : BroadcastReceiver() {

    var onStateChanged: ((isConnected: Boolean, BluetoothDevice) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        when(intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.d("Connection","ACTION_ACL_CONNECTED")
                onStateChanged?.invoke(true, device ?: return)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Log.d("Connection","ACTION_ACL_DISCONNECTED")
                onStateChanged?.invoke(false, device ?: return)
            }
        }
    }
}