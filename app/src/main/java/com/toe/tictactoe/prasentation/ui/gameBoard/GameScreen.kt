package com.toe.tictactoe.prasentation.ui.gameBoard

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toe.tictactoe.R
import com.toe.tictactoe.common.Helper.vibrate
import com.toe.tictactoe.ads.BannerAd
import com.toe.tictactoe.ads.InterstitialAdManager
import com.toe.tictactoe.prasentation.component.TopAppBar
import com.toe.tictactoe.prasentation.theme.TicTacToeTheme
import com.toe.tictactoe.prasentation.theme.oColor
import com.toe.tictactoe.prasentation.theme.xColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableStateListOf
import kotlin.random.Random


@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    backToHome: () -> Unit
) {

    val context = LocalContext.current
    val gameState = gameViewModel.gameUiState.collectAsStateWithLifecycle()
    val interstitialUnitId = stringResource(id = R.string.admob_interstitial_unit_id)
    val interstitialManager = remember(interstitialUnitId) {
        InterstitialAdManager(
            context = context,
            unitId = interstitialUnitId
        )
    }
    LaunchedEffect(Unit) {
        interstitialManager.load()
    }

    BackHandler(gameState.value is GameUiState.GameStarted) {
        Log.d("TAG", "OnBackPressed")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val gradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1F1F1F), Color(0xFF2A2A2A))
                )
                drawRect(gradient)
            }
    ) {

        TopAppBar()

        Spacer(modifier = Modifier.height(20.dp))

        when (val state = gameState.value) {
            is GameUiState.GameStarted -> {

                LaunchedEffect(Unit) {
                    context.vibrate()
                }
                GameStarted(
                    modifier = modifier,
                    state = state,
                    onCellClicked = { index -> gameViewModel.onCellClicked(index) },
                    restartGame = { gameViewModel.restartButtonClick() },
                    closeGame = { gameViewModel.disconnectFromDevice() }
                )
                LaunchedEffect(state.result) {
                    if (state.result != GameResult.InProgress) {
                        val activity = context as? Activity ?: return@LaunchedEffect
                        interstitialManager.show(activity)
                    }
                }

            }

            is GameUiState.Connecting -> {
                Connecting(isServer = state.isServer)
            }

            is GameUiState.Connected -> {
                Connected(
                    localPlayerInfo = state.localPlayerInfo,
                    opponentPlayerInfo = state.opponentPlayerInfo,
                    isServer = state.isServer,
                    startGame = {
                        gameViewModel.startButtonClick()
                    }
                )
            }

            GameUiState.Disconnected -> {
                backToHome()
            }

            is GameUiState.Error -> {
                Error(message = state.errorMessage) {
                    backToHome()
                }
            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        BannerAd(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            unitId = stringResource(id = R.string.admob_banner_unit_id)
        )
    }


}

@Composable
fun GameStarted(
    modifier: Modifier = Modifier,
    state: GameUiState.GameStarted,
    onCellClicked: (Int) -> Unit,
    restartGame: () -> Unit,
    closeGame: () -> Unit
) {

    val isDarkMode = isSystemInDarkTheme()
    val borderColor = if (isDarkMode) Color.White else Color.Black
    val configuration = LocalConfiguration.current
    val computed = (configuration.screenWidthDp.dp - 48.dp)
    val boardSize = if (computed < 240.dp) 240.dp else if (computed > 360.dp) 360.dp else computed
    val cellSize = boardSize / 3

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        val isLocalPlayerTurn = state.currentPlayer == state.localPlayerInfo.player
        val currentPlayerInfo =
            if (isLocalPlayerTurn) state.localPlayerInfo else state.opponentPlayerInfo

        LaunchedEffect(isLocalPlayerTurn) {
            if (isLocalPlayerTurn) {
                context.vibrate()
            }
        }

        Text("Current Player: ${currentPlayerInfo.name}", fontSize = 16.sp)

        PlayerProfile(
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 20.dp),
            name = state.opponentPlayerInfo.name,
            isEnable = state.opponentPlayerInfo.player == state.currentPlayer
        )

        Spacer(modifier = Modifier.height(8.dp))

        val winPattern = remember(state.board, state.result) {
            val patterns = listOf(
                listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
                listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
                listOf(0, 4, 8), listOf(2, 4, 6)
            )
            if (state.result == GameResult.XWins || state.result == GameResult.OWins) {
                patterns.firstOrNull { p ->
                    val a = state.board[p[0]]
                    a.isNotEmpty() && a == state.board[p[1]] && a == state.board[p[2]]
                }
            } else null
        }
        val winAlpha by animateFloatAsState(if (winPattern != null) 1f else 0f, animationSpec = tween(250))

        Box(
            modifier = Modifier
                .border(1.dp, borderColor)
                .height(boardSize)
                .width(boardSize)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) {
                itemsIndexed(state.board) { index, symbol ->
                    val row = index / 3
                    val col = index % 3

                    val symbolColor = when (symbol) {
                        "X" -> xColor
                        "O" -> oColor
                        else -> Color.Unspecified
                    }

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                if (col < 2) {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                                if (row < 2) {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .clickable { onCellClicked(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = symbol, animationSpec = tween(200)) { value ->
                            if (value.isNotEmpty()) {
                                Text(
                                    text = value,
                                    fontSize = 32.sp,
                                    color = symbolColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = 1.05f
                                        scaleY = 1.05f
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (winPattern != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val startIdx = winPattern.first()
                    val endIdx = winPattern.last()
                    val startRow = startIdx / 3
                    val startCol = startIdx % 3
                    val endRow = endIdx / 3
                    val endCol = endIdx % 3
                    val cs = cellSize.toPx()
                    val start = Offset(startCol * cs + cs / 2f, startRow * cs + cs / 2f)
                    val end = Offset(endCol * cs + cs / 2f, endRow * cs + cs / 2f)
                    drawLine(
                        color = Color.Yellow.copy(alpha = winAlpha),
                        start = start,
                        end = end,
                        strokeWidth = 6.dp.toPx()
                    )
                }
                ConfettiOverlay()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        PlayerProfile(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp),
            name = "You",
            isEnable = state.localPlayerInfo.player == state.currentPlayer
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (state.result) {
            GameResult.XWins -> {
                val text = if (state.localPlayerInfo.player == Player.X)
                    "You Win!"
                else
                    "Player ${state.opponentPlayerInfo.name} Wins!"
                Text(text)
            }

            GameResult.OWins -> {
                val text = if (state.localPlayerInfo.player == Player.O)
                    "You Win!"
                else
                    "Player ${state.opponentPlayerInfo.name} Wins!"
                Text(text)
            }

            GameResult.Draw -> {
                Text("It's a Draw!")
            }

            else -> {}
        }

        AnimatedVisibility(visible = state.result != GameResult.InProgress && state.localPlayerInfo.isHost) {
            Button(onClick = { restartGame() }) {
                Text("Restart Game", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { closeGame() }) {
            Text("Close Game", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ConfettiOverlay() {
    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    LaunchedEffect(Unit) {
        particles.clear()
        repeat(100) {
            particles.add(
                ConfettiParticle(
                    x = Random.nextFloat(),
                    y = 0f,
                    vx = (Random.nextFloat() - 0.5f) * 0.6f,
                    vy = Random.nextFloat() * 0.8f,
                    color = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)[Random.nextInt(5)],
                    size = Random.nextFloat().coerceIn(0.02f, 0.05f),
                    life = 1f
                )
            )
        }
        val durationMs = 2000
        var elapsed = 0
        while (elapsed < durationMs) {
            particles.forEach {
                it.vy += 0.002f
                it.x += it.vx
                it.y += it.vy
                it.life -= 0.01f
            }
            particles.removeAll { it.life <= 0f || it.y > 1.2f }
            elapsed += 16
            kotlinx.coroutines.delay(16)
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach {
            val px = it.x * size.width
            val py = it.y * size.height
            val ps = it.size * size.minDimension
            drawCircle(
                color = it.color.copy(alpha = it.life),
                radius = ps,
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun PlayerProfile(modifier: Modifier = Modifier, name: String?, isEnable: Boolean) {

    val targetAlpha = if (isEnable) 1f else 0.5f
    val alphaAnim by animateFloatAsState(targetAlpha, animationSpec = tween(200))
    Column(modifier = modifier.alpha(alphaAnim)) {

        Image(
            painter = painterResource(R.drawable.person),
            contentDescription = "Person icon",
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Text(text = name ?: "", fontSize = 12.sp)
    }
}

@Composable
fun Connecting(modifier: Modifier = Modifier, isServer: Boolean) {

    val discoverableBluetoothLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            /* Not needed */
        }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if(isServer){
            coroutineScope.launch {
                delay(1000)
                discoverableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                )
            }

        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = if (isServer) "Waiting for opponent.." else "Connecting..."

        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(20.dp))
        Text(message, fontSize = 20.sp)
    }
}

@Composable
fun Connected(
    modifier: Modifier = Modifier,
    localPlayerInfo: PlayerInfo?,
    opponentPlayerInfo: PlayerInfo?,
    isServer: Boolean,
    startGame: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 30.dp, vertical = 15.dp)
        ) {
            Text(
                text = localPlayerInfo?.name ?: "",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                ),
            )
            if (opponentPlayerInfo?.name.isNullOrBlank().not()) {
                val symbolColor = when (localPlayerInfo?.player?.name) {
                    "X" -> xColor
                    "O" -> oColor
                    else -> Color.Unspecified
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = localPlayerInfo?.player?.name ?: "",
                    fontSize = 32.sp,
                    color = symbolColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }

        }


        Spacer(modifier = Modifier.height(10.dp))

        Image(
            modifier = Modifier.size(80.dp),
            painter = painterResource(R.drawable.vs_icon),
            contentDescription = "Versus"
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 30.dp, vertical = 15.dp)
        ) {
            Text(
                text = opponentPlayerInfo?.name?.takeIf { it.isNotBlank() } ?: "?",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
            )
            if (opponentPlayerInfo?.name.isNullOrBlank().not()) {
                val symbolColor = when (opponentPlayerInfo.player.name) {
                    "X" -> xColor
                    "O" -> oColor
                    else -> Color.Unspecified
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = opponentPlayerInfo.player.name,
                    fontSize = 32.sp,
                    color = symbolColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (opponentPlayerInfo == null) return

        Spacer(modifier = Modifier.height(50.dp))

        if (isServer) {
            Button(onClick = { startGame() }) {
                Text("Start Game", fontSize = 20.sp)
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Waiting for your friend ${opponentPlayerInfo.name} to start the match..",
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }


}


@Composable
fun Error(modifier: Modifier = Modifier, message: String?, onReturnClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(message ?: "Something went wrong.", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onReturnClick() }) {
            Text("Return")
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewGameScreen() {
    TicTacToeTheme {
        GameStarted(
            state = GameUiState.GameStarted(
                localPlayerInfo = PlayerInfo(
                    name = "Milan",
                    player = Player.X,
                    isHost = true
                ),
                opponentPlayerInfo = PlayerInfo(
                    name = "Alex",
                    player = Player.O,
                    isHost = false
                )
            ),
            onCellClicked = { index -> },
            restartGame = {},
            closeGame = {}
        )
    }
}
