package com.toe.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.toe.tictactoe.prasentation.MainNavigation
import com.toe.tictactoe.prasentation.theme.TicTacToeTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)

        gamesSignInClient.isAuthenticated()
            .addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult?>? ->
                val isAuthenticated =
                    (isAuthenticatedTask?.isSuccessful == true &&
                            isAuthenticatedTask.getResult()?.isAuthenticated == true)
                if (isAuthenticated) {
                    // Continue with Play Games Services
                } else {
                    // Show a sign-in button to ask players to authenticate. Clicking it should
                    // call GamesSignInClient.signIn().
                }
            }

        setContent {
            TicTacToeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
