package com.toe.tictactoe.domain.usecase

import com.toe.tictactoe.domain.BluetoothController
import javax.inject.Inject

class StartBluetoothDiscoveryUseCase @Inject constructor(private val bluetoothController: BluetoothController
) {

    operator fun invoke() {
        bluetoothController.startDiscovery()
    }
}