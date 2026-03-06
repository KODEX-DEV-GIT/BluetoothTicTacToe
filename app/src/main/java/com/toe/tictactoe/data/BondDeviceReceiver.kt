package com.toe.tictactoe.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BondDeviceReceiver: BroadcastReceiver() {

    var onBondStatus: ((isPaired: Boolean,BluetoothDevice) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        when (intent?.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)) {
            BluetoothDevice.BOND_BONDED -> {
                onBondStatus?.invoke(true,device?:return)
            }
            BluetoothDevice.BOND_NONE -> {
                onBondStatus?.invoke(false,device?:return)
            }
        }
    }
}