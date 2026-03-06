package com.toe.tictactoe.domain.usecase

import com.toe.tictactoe.prasentation.ui.gameBoard.GameResult
import com.toe.tictactoe.prasentation.ui.gameBoard.GameUiState
import com.toe.tictactoe.prasentation.ui.gameBoard.Player
import javax.inject.Inject

class MakeMoveUseCase @Inject constructor() {

    fun invoke(state: GameUiState.GameStarted, index: Int): GameUiState.GameStarted {
        if (state.board[index].isNotEmpty() || state.result != GameResult.InProgress) return state

        val newBoard = state.board.toMutableList()
        newBoard[index] = state.currentPlayer.name

        val newResult = CheckGameResultUseCase().invoke(newBoard)

        return state.copy(
            board = newBoard,
            currentPlayer = if (state.currentPlayer == Player.X) Player.O else Player.X,
            result = newResult
        )
    }
}