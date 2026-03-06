package com.toe.tictactoe.prasentation.ui.gameBoard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.toe.tictactoe.domain.BluetoothServerController
import com.toe.tictactoe.domain.model.BluetoothDeviceDomain
import com.toe.tictactoe.domain.model.BluetoothMessage
import com.toe.tictactoe.domain.model.ConnectionResult
import com.toe.tictactoe.domain.usecase.CheckGameResultUseCase
import com.toe.tictactoe.domain.usecase.MakeMoveUseCase
import com.toe.tictactoe.prasentation.GameMode
import com.toe.tictactoe.prasentation.MainDest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val makeMoveUseCase: MakeMoveUseCase,
    private val bluetoothServerController: BluetoothServerController,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameBoard: MainDest.GameBoard = savedStateHandle.toRoute()
    private val gameMode: GameMode = gameBoard.mode
    private val checkGameResultUseCase = CheckGameResultUseCase()

    private val _gameUiState = MutableStateFlow<GameUiState>(GameUiState.Connecting(false))
    val gameUiState: StateFlow<GameUiState> = _gameUiState

    private var localPlayerInfo: PlayerInfo = PlayerInfo("", Player.X, true)
    private var opponentPlayerInfo: PlayerInfo = PlayerInfo("", Player.O, false)
    private var isHostDevice = true

    private var deviceConnectionJob: Job? = null
    private var computerMoveJob: Job? = null


    init {

        if (gameMode == GameMode.LocalAi) {
            isHostDevice = true
            localPlayerInfo = PlayerInfo(
                name = gameBoard.name?.takeIf { it.isNotBlank() } ?: "You",
                player = Player.from(gameBoard.selectedPlayer ?: "") ?: Player.X,
                isHost = true
            )
            opponentPlayerInfo = PlayerInfo(
                name = "Computer",
                player = localPlayerInfo.player.opponent(),
                isHost = false
            )
            _gameUiState.update {
                GameUiState.GameStarted.create(
                    localPlayerInfo = localPlayerInfo,
                    opponentPlayerInfo = opponentPlayerInfo
                )
            }
            maybeMakeComputerMoveIfNeeded()
        } else {
            if (gameBoard.deviceAddress != null) {
                isHostDevice = false
                setPlayerRole(
                    name = gameBoard.name?.takeIf { it.isNotBlank() }
                        ?: bluetoothServerController.getDeviceName(),
                    player = Player.X,
                    isFromLocal = true,
                )
                connectToDevice(BluetoothDeviceDomain(gameBoard.deviceName, gameBoard.deviceAddress))
            } else {
                isHostDevice = true
                setPlayerRole(
                    name = gameBoard.name?.takeIf { it.isNotBlank() }
                        ?: bluetoothServerController.getDeviceName(),
                    player = Player.from(gameBoard.selectedPlayer ?: "") ?: Player.X,
                    isFromLocal = true,
                )

                waitForIncomingConnections()
            }
        }

        if (gameMode == GameMode.Bluetooth) {
            bluetoothServerController.isConnected.onEach { isConnected ->
                Log.d("Connection", "isConnected observer: $isConnected")
            }.launchIn(viewModelScope)

            bluetoothServerController.errors.onEach { error ->
                _gameUiState.update {
                    GameUiState.Error(
                        errorMessage = error
                    )
                }
            }.launchIn(viewModelScope)
        }
    }


    fun connectToDevice(device: BluetoothDeviceDomain) {
        if (gameMode != GameMode.Bluetooth) return
        _gameUiState.update { GameUiState.Connecting(isServer = false) }
        deviceConnectionJob = bluetoothServerController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        computerMoveJob?.cancel()
        if (gameMode == GameMode.Bluetooth) {
            bluetoothServerController.closeConnection()
        }
        _gameUiState.update { GameUiState.Disconnected }
    }

    fun waitForIncomingConnections() {
        if (gameMode != GameMode.Bluetooth) return
        _gameUiState.update { GameUiState.Connecting(isServer = true) }
        deviceConnectionJob = bluetoothServerController
            .startBluetoothServer()
            .listen()
    }

    fun setPlayerRole(name: String, player: Player?, isFromLocal: Boolean) {

        if (isFromLocal) {
            localPlayerInfo = localPlayerInfo.copy(
                name = name,
                player = player ?: localPlayerInfo.player,
                isHost = isHostDevice
            )
            opponentPlayerInfo = opponentPlayerInfo.copy(
                player = player?.opponent() ?: opponentPlayerInfo.player,
                isHost = isHostDevice
            )
        } else {
            localPlayerInfo = localPlayerInfo.copy(
                player = player?.opponent() ?: localPlayerInfo.player,
                isHost = isHostDevice
            )
            opponentPlayerInfo = opponentPlayerInfo.copy(
                name = name,
                player = player ?: opponentPlayerInfo.player,
                isHost = isHostDevice
            )
        }

        val state = _gameUiState.value as? GameUiState.Connected ?: return
        _gameUiState.update {
            state.copy(
                localPlayerInfo = localPlayerInfo,
                opponentPlayerInfo = opponentPlayerInfo
            )
        }
    }

    private fun startGame() {
        _gameUiState.update {
            GameUiState.GameStarted.create(
                localPlayerInfo = localPlayerInfo,
                opponentPlayerInfo = opponentPlayerInfo
            )
        }
        maybeMakeComputerMoveIfNeeded()
    }

    fun startButtonClick() {
        if (!localPlayerInfo.isHost) return
        startGame()
        if (gameMode == GameMode.Bluetooth) {
            sendMessage(BluetoothMessage.Start(localPlayerInfo.player))
        }
    }

    fun restartButtonClick() {
        if (!localPlayerInfo.isHost) return
        startGame()
        if (gameMode == GameMode.Bluetooth) {
            sendMessage(BluetoothMessage.Restart)
        }
    }

    fun onCellClicked(index: Int) {
        val state = _gameUiState.value as? GameUiState.GameStarted ?: return
        if (state.currentPlayer != localPlayerInfo.player) return
        if (state.result != GameResult.InProgress) return
        if (state.board[index].isNotEmpty()) return

        val newState = makeMoveUseCase.invoke(state, index)
        _gameUiState.update { newState }
        if (gameMode == GameMode.Bluetooth) {
            sendMessage(BluetoothMessage.Turn(index = index, player = localPlayerInfo.player))
        } else {
            maybeMakeComputerMoveIfNeeded()
        }
    }

    fun sendMessage(bluetoothMessage: BluetoothMessage) {
        if (gameMode != GameMode.Bluetooth) return
        viewModelScope.launch {
            val bluetoothMessage = bluetoothServerController.trySendMessage(bluetoothMessage)
            if (bluetoothMessage != null) {
                Log.d("message", "message Sent: $bluetoothMessage")
            }
        }
    }


    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    // _gameUiState.update { GameUiState.Connected(playerName = null) }
                    Log.d("Connection", "ConnectionEstablished: ${localPlayerInfo.name}")
                    _gameUiState.update {
                        GameUiState.Connected(
                            localPlayerInfo = localPlayerInfo,
                            opponentPlayerInfo = opponentPlayerInfo,
                            isServer = isHostDevice
                        )
                    }
                    sendMessage(
                        BluetoothMessage.Name(
                            name = localPlayerInfo.name,
                            player = if (isHostDevice) localPlayerInfo.player else null,
                            isLocal = true
                        )
                    )

                }

                is ConnectionResult.TransferSucceeded -> {
                    handleMessage(result.message)
                }

                is ConnectionResult.Error -> {
                    _gameUiState.update {
                        GameUiState.Error(result.message)
                    }
                }
            }
        }.catch { throwable ->
            bluetoothServerController.closeConnection()
            _gameUiState.update { GameUiState.Disconnected }
        }.launchIn(viewModelScope)
    }

    private fun handleMessage(msg: BluetoothMessage) {
        Log.d("message", "message received: $msg")
        when (msg) {
            is BluetoothMessage.Start -> {
                startGame()
            }

            is BluetoothMessage.Turn -> {
                val state = _gameUiState.value as? GameUiState.GameStarted ?: return
                if (state.currentPlayer == msg.player && state.board[msg.index].isEmpty()) {
                    _gameUiState.update { makeMoveUseCase.invoke(state, msg.index) }
                }
            }

            is BluetoothMessage.Restart -> {
                startGame()
            }

            is BluetoothMessage.Name -> {
                _gameUiState.value as? GameUiState.Connected ?: return
                setPlayerRole(name = msg.name, player = msg.player, isFromLocal = msg.isLocal)
            }

            BluetoothMessage.Unknown -> {}
        }
    }

    private fun maybeMakeComputerMoveIfNeeded() {
        if (gameMode != GameMode.LocalAi) return
        val state = _gameUiState.value as? GameUiState.GameStarted ?: return
        if (state.result != GameResult.InProgress) return
        if (state.currentPlayer != opponentPlayerInfo.player) return

        computerMoveJob?.cancel()
        computerMoveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            val latest = _gameUiState.value as? GameUiState.GameStarted ?: return@launch
            if (latest.result != GameResult.InProgress) return@launch
            if (latest.currentPlayer != opponentPlayerInfo.player) return@launch

            val moveIndex = chooseComputerMove(latest.board, opponentPlayerInfo.player) ?: return@launch
            if (latest.board[moveIndex].isNotEmpty()) return@launch
            _gameUiState.update { makeMoveUseCase.invoke(latest, moveIndex) }
        }
    }

    private fun chooseComputerMove(board: List<String>, computerPlayer: Player): Int? {
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isEmpty()) return null

        fun isWinAfter(index: Int, symbol: String): Boolean {
            val next = board.toMutableList()
            next[index] = symbol
            val result = checkGameResultUseCase.invoke(next)
            return when (symbol) {
                Player.X.name -> result == GameResult.XWins
                Player.O.name -> result == GameResult.OWins
                else -> false
            }
        }

        val computerSymbol = computerPlayer.name
        val opponentSymbol = computerPlayer.opponent().name

        emptyIndices.firstOrNull { isWinAfter(it, computerSymbol) }?.let { return it }
        emptyIndices.firstOrNull { isWinAfter(it, opponentSymbol) }?.let { return it }

        if (board[4].isEmpty()) return 4

        val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
        if (corners.isNotEmpty()) return corners.random()

        return emptyIndices.random()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothServerController.release()
    }
}
