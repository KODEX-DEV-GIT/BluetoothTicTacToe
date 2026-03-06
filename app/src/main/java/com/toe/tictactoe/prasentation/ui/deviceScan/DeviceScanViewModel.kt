package com.toe.tictactoe.prasentation.ui.deviceScan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toe.tictactoe.domain.BluetoothController
import com.toe.tictactoe.domain.model.BluetoothDeviceDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeviceScanViewModel @Inject constructor(private val bluetoothController: BluetoothController) :
    ViewModel() {

    private val _uiState = MutableStateFlow(DeviceScanUiState())
    val uiState = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        bluetoothController.isScanStarted,
        _uiState
    ) { scannedDevices, pairedDevices,isScanStarted, state ->
        state.copy(
            scannedDevices = scannedDevices.filter { it.name.isNullOrBlank().not() },
            pairedDevices = pairedDevices,
            isScanning = isScanStarted
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)

    init {
        startScan()
    }

    fun pairDevice(device: BluetoothDeviceDomain){
        bluetoothController.pairDevice(device)
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        bluetoothController.release()
    }

}