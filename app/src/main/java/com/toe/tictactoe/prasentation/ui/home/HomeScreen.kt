package com.toe.tictactoe.prasentation.ui.home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toe.tictactoe.prasentation.component.ChoosePlayerDialog
import com.toe.tictactoe.prasentation.component.TopAppBar
import com.toe.tictactoe.prasentation.ui.gameBoard.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    createGame: (String, Player) -> Unit,
    playVsComputer: (String, Player) -> Unit,
    joinGame: (String) -> Unit,
) {

    var permissionGranted by remember { mutableStateOf(false) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val bluetoothManager = remember {
        context.getSystemService(BluetoothManager::class.java)
    }
    val isBluetoothEnabled = remember {
        derivedStateOf { bluetoothManager?.adapter?.isEnabled == true }
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val enableBluetoothLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            /* Not needed */
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->

            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if (canEnableBluetooth && !isBluetoothEnabled.value) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }

            isPermanentlyDenied = perms.filterValues { !it }.keys.any { permission ->
                !ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)
            }
            permissionGranted = perms.all { it.value }
        }

    LaunchedEffect(Unit) {

        val notGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
            permissionGranted = false
        } else {
            permissionGranted = true

        }
    }

    Home(
        modifier = modifier,
        name = uiState.name,
        permissionGranted = permissionGranted,
        isPermanentlyDenied = isPermanentlyDenied,
        createGame = {
            if (bluetoothManager?.adapter?.isEnabled == true) {
                homeViewModel.showDialog(HomeDialogMode.BluetoothHost)
            } else {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        },
        joinGame = {
            if (bluetoothManager?.adapter?.isEnabled == true)
                joinGame(it)
            else {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        },
        playVsComputer = {
            homeViewModel.showDialog(HomeDialogMode.LocalVsComputer)
        },
        requestPermission = {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        },
        onNameChange = {
            homeViewModel.onNameChanged(it)
        }
    )

    if (uiState.isShowDialog) {
        ChoosePlayerDialog(
            selectedPlayer = uiState.selectedPlayer,
            onSelectPlayer = { homeViewModel.onSelectPlayerChange(it) },
            onStartClick = {
                homeViewModel.dismissDialog()

                coroutineScope.launch {
                    delay(100)
                    when (uiState.dialogMode) {
                        HomeDialogMode.BluetoothHost -> createGame(
                            uiState.name,
                            uiState.selectedPlayer
                        )

                        HomeDialogMode.LocalVsComputer -> playVsComputer(
                            uiState.name,
                            uiState.selectedPlayer
                        )
                    }
                }

            },
            onDismissRequest = {
                homeViewModel.dismissDialog()
            }
        )
    }

}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    name: String,
    permissionGranted: Boolean,
    isPermanentlyDenied: Boolean,
    createGame: (String) -> Unit,
    joinGame: (String) -> Unit,
    playVsComputer: (String) -> Unit,
    requestPermission: () -> Unit,
    onNameChange: (String) -> Unit
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxSize()) {

        TopAppBar()

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            value = name,
            onValueChange = {
                onNameChange(it)
            },
            label = {
                Text("Enter name")
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (!permissionGranted && !isPermanentlyDenied) {
                Button(onClick = requestPermission) {
                    Text("Grant Permissions", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { playVsComputer(name) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Play vs Computer", fontSize = 20.sp)
                }

            } else if (isPermanentlyDenied) {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val settingUri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = settingUri
                    context.startActivity(intent)
                }) {
                    Text("Goto Setting")
                }
                Text(
                    "Please check the necessary permissions are granted!",
                    color = Red,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { playVsComputer(name) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Play vs Computer", fontSize = 20.sp)
                }
            } else {

                Button(onClick = { createGame(name) }) {
                    Text("Create Game", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { joinGame(name) }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray
                    )
                ) {
                    Text("Join Game", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { playVsComputer(name) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Play vs Computer", fontSize = 20.sp)
                }
            }

        }
    }


}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewHomeScreen() {
    Home(
        name = "",
        permissionGranted = false,
        isPermanentlyDenied = false,
        createGame = {},
        joinGame = {},
        playVsComputer = {},
        requestPermission = {},
        onNameChange = {})
}
