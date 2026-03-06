package com.toe.tictactoe.prasentation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
@Keep
@Serializable
enum class GameMode {
    Bluetooth,
    LocalAi
}