package com.toe.tictactoe.domain.usecase

import com.toe.tictactoe.prasentation.ui.gameBoard.GameResult

class CheckGameResultUseCase {

    fun invoke(board: List<String>): GameResult {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )

        for (pattern in winPatterns) {
            val (a, b, c) = pattern
            if (board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]) {
                return if (board[a] == "X") GameResult.XWins else GameResult.OWins
            }
        }

        return if (board.all { it.isNotEmpty() }) GameResult.Draw else GameResult.InProgress
    }
}