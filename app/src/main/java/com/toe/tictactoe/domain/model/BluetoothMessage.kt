package com.toe.tictactoe.domain.model

import com.toe.tictactoe.prasentation.ui.gameBoard.Player

//data class BluetoothMessage(
//    val index: Int,
//    val senderName: String,
//    val isFromLocalUser: Boolean
//)

sealed class BluetoothMessage {
    data class Name(val name: String, val player: Player?, val isLocal: Boolean) :
        BluetoothMessage()

    data class Start(val assignedPlayer: Player) : BluetoothMessage()
    data class Turn(val index: Int, val player: Player) : BluetoothMessage()
    object Restart : BluetoothMessage()
    object Unknown : BluetoothMessage()
}