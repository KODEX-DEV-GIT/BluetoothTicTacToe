package com.toe.tictactoe.prasentation.ui.home

import com.toe.tictactoe.prasentation.ui.gameBoard.Player

data class HomeUiState(
    var name:String = "",
    var selectedPlayer: Player = Player.X,
    var isShowDialog: Boolean = false,
    var dialogMode: HomeDialogMode = HomeDialogMode.BluetoothHost
)
