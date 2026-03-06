package com.toe.tictactoe.prasentation.ui.gameBoard

import androidx.compose.ui.graphics.Color

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var life: Float
)