package com.toe.tictactoe.prasentation.ui.deviceScan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toe.tictactoe.domain.model.BluetoothDeviceDomain


@Composable
fun DeviceScanScreen(modifier: Modifier = Modifier, deviceScanViewModel: DeviceScanViewModel, onPairedDeviceClick: (BluetoothDeviceDomain) -> Unit) {

    val uiState = deviceScanViewModel.uiState.collectAsStateWithLifecycle()

    DeviceScan(
        modifier = modifier,
        isScanning = uiState.value.isScanning,
        pairedDevices = uiState.value.pairedDevices,
        scannedDevices = uiState.value.scannedDevices,
        onStartScan = deviceScanViewModel::startScan,
        onStopScan = deviceScanViewModel::stopScan,
        onPairedDeviceClick = onPairedDeviceClick,
        onScanDeviceClick = deviceScanViewModel::pairDevice
    )

}

@Composable
fun DeviceScan(
    modifier: Modifier = Modifier,
    isScanning: Boolean,
    pairedDevices: List<BluetoothDeviceDomain>,
    scannedDevices: List<BluetoothDeviceDomain>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onPairedDeviceClick: (BluetoothDeviceDomain) -> Unit,
    onScanDeviceClick: (BluetoothDeviceDomain) -> Unit,
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BluetoothDeviceList(
            isScanning = isScanning,
            pairedDevices = pairedDevices,
            scannedDevices = scannedDevices,
            onPairedDeviceClick = onPairedDeviceClick,
            onScanDeviceClick = onScanDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onStartScan, enabled = isScanning.not()) {
                Text(text = "Start scan")
            }

            Spacer(modifier = Modifier.width(30.dp))
            Button(onClick = onStopScan, enabled = isScanning) {
                Text(text = "Stop scan")
            }

        }
        Spacer(modifier = Modifier.height(20.dp))

    }

}

@Composable
fun BluetoothDeviceList(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    pairedDevices: List<BluetoothDeviceDomain>,
    scannedDevices: List<BluetoothDeviceDomain>,
    onPairedDeviceClick: (BluetoothDeviceDomain) -> Unit,
    onScanDeviceClick: (BluetoothDeviceDomain) -> Unit,
) {

    LazyColumn(modifier = modifier) {

        item {
            Text(
                text = "Paired Devices",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(pairedDevices) { device ->
            Text(
                text = "${device.name ?: device.address} (${device.type})",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPairedDeviceClick(device) }
                    .padding(16.dp)
            )
        }

        item {

            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Scanned Devices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
                if(isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                }
            }

        }
        items(scannedDevices) { device ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${device.name ?: device.address} (${device.type})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = device.isPairing.not()) { onScanDeviceClick(device) }
                        .alpha(if(device.isPairing) 0.5f else 1f)
                )
                if(device.isPairing){
                    Text(text = "Pairing...", )
                }
            }

        }


    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewHomeScreen() {
    DeviceScan(
        isScanning = false,
        pairedDevices = emptyList(),
        scannedDevices = emptyList(),
        onStartScan = {},
        onStopScan = {},
        onPairedDeviceClick = {},
        onScanDeviceClick = {})
}