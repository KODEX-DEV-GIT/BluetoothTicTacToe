package com.toe.tictactoe.data.mapper

import com.toe.tictactoe.domain.model.BluetoothMessage
import com.toe.tictactoe.prasentation.ui.gameBoard.Player

/*
| Command       | Format        | Description                                     | Sender         |
| ------------- | ------------- | ------------------------------------------------| -------------- |
| `NAME`        | `NAME:Alex:X` | Send name and symbol (X or O) after connection  | Server         |
| `NAME`        | `Name:John`   | Send name after connection                      | Client         |
| `START`       | `START:X`     | Start game and assign symbol (X or O)           | Server         |
| `TURN`        | `TURN:4:X`    | Player placed `X` at index 4                    | Current player |
| `RESTART_REQ` | `RESTART_REQ` | Player requests game restart                    | Server         |
*/


fun String.toBluetoothMessage(isLocal: Boolean): BluetoothMessage {
    val parts = split(":")
    return when (parts[0]) {
        "NAME" -> {
            val name = parts.getOrNull(1) ?: return BluetoothMessage.Unknown
            val player = Player.from(parts.getOrNull(2) ?: "")
            BluetoothMessage.Name(name, player, isLocal)
        }

        "START" -> Player.from(parts.getOrNull(1) ?: "")?.let {
            BluetoothMessage.Start(it)
        } ?: BluetoothMessage.Unknown

        "TURN" -> {
            val index = parts.getOrNull(1)?.toIntOrNull() ?: -1
            val player = Player.from(parts.getOrNull(2) ?: "") ?: return BluetoothMessage.Unknown
            BluetoothMessage.Turn(index, player)
        }

        "RESTART_REQ" -> BluetoothMessage.Restart
        else -> BluetoothMessage.Unknown
    }
}


fun BluetoothMessage.toByteArray(): ByteArray {
    val str = when (this) {
        is BluetoothMessage.Name -> "NAME:$name${if (player != null) ":${player.name}" else ""}"
        is BluetoothMessage.Start -> "START:${assignedPlayer.name}"
        is BluetoothMessage.Turn -> "TURN:${index}:${player.name}"
        BluetoothMessage.Restart -> "RESTART_REQ"
        BluetoothMessage.Unknown -> "UNKNOWN"
    }
    return str.encodeToByteArray()
}