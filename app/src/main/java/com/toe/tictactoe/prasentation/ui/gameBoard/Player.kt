package com.toe.tictactoe.prasentation.ui.gameBoard

import androidx.annotation.Keep

@Keep
enum class Player {
    X, O;

    companion object {
        fun from(symbol: String): Player? = entries.find { it.name == symbol }
    }
}