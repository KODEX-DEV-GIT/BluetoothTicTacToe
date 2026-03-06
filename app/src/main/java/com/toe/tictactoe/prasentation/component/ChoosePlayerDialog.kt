package com.toe.tictactoe.prasentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.toe.tictactoe.prasentation.theme.oColor
import com.toe.tictactoe.prasentation.theme.xColor
import com.toe.tictactoe.prasentation.ui.gameBoard.Player

@Composable
fun ChoosePlayerDialog(
    selectedPlayer: Player,
    onSelectPlayer: (Player) -> Unit,
    onStartClick: () -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Your Player",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Select whether you want to\nplay as X or O.",
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(50.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Player.entries.forEach { player ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = if (selectedPlayer == player)Color.Black
                                    else  Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedPlayer == player) MaterialTheme.colorScheme.primary
                                    else Color.Gray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectPlayer(player) }
                        ) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = if (player == Player.X) xColor else oColor,
                                )
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        onStartClick()
                    }
                ) {
                    Text("Create Game", fontSize = 20.sp)
                }
            }
        }
    }
}
