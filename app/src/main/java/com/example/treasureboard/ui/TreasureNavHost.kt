package com.example.treasureboard.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.treasureboard.AppViewModel
import com.example.treasureboard.GameViewModel
import com.example.treasureboard.ui.screens.AboutScreen
import com.example.treasureboard.ui.screens.GameScreen
import com.example.treasureboard.ui.screens.HomeScreen
import com.example.treasureboard.ui.screens.HowToPlayScreen
import com.example.treasureboard.ui.screens.ResultScreen
import com.example.treasureboard.ui.screens.SettingsScreen
import com.example.treasureboard.ui.screens.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

/** Ignores taps from a screen that is already transitioning away (prevents double navigation). */
private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.goHome() {
    if (!popBackStack(Routes.HOME, inclusive = false)) {
        navigate(Routes.HOME) { popUpTo(0) }
    }
}

@Composable
fun TreasureNavHost(appViewModel: AppViewModel, gameViewModel: GameViewModel) {
    val nav = rememberNavController()
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val stats by appViewModel.stats.collectAsStateWithLifecycle()

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) { entry ->
            HomeScreen(
                stats = stats,
                onPlay = {
                    if (entry.isResumed()) {
                        gameViewModel.newGame()
                        nav.navigate(Routes.GAME)
                    }
                },
                onHowToPlay = { if (entry.isResumed()) nav.navigate(Routes.HOW_TO_PLAY) },
                onSettings = { if (entry.isResumed()) nav.navigate(Routes.SETTINGS) },
                onAbout = { if (entry.isResumed()) nav.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.GAME) { entry ->
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                onHome = { if (entry.isResumed()) nav.goHome() },
                onGameOver = {
                    if (entry.isResumed()) {
                        nav.navigate(Routes.RESULT) { popUpTo(Routes.GAME) { inclusive = true } }
                    }
                }
            )
        }
        composable(Routes.RESULT) { entry ->
            val ui by gameViewModel.ui.collectAsStateWithLifecycle()
            // After process death the match is gone; fall back to Home rather than show an empty result.
            LaunchedEffect(ui.game.isGameOver) { if (!ui.game.isGameOver) nav.goHome() }
            if (ui.game.isGameOver) {
                ResultScreen(
                    ui = ui,
                    onPlayAgain = {
                        if (entry.isResumed()) {
                            gameViewModel.newGame()
                            nav.navigate(Routes.GAME) { popUpTo(Routes.RESULT) { inclusive = true } }
                        }
                    },
                    onHome = { if (entry.isResumed()) nav.goHome() }
                )
            }
        }
        composable(Routes.HOW_TO_PLAY) { entry ->
            HowToPlayScreen(settings.boardTheme, onBack = { if (entry.isResumed()) nav.popBackStack() })
        }
        composable(Routes.SETTINGS) { entry ->
            SettingsScreen(
                settings = settings,
                stats = stats,
                onThemeMode = appViewModel::setThemeMode,
                onBoardTheme = appViewModel::setBoardTheme,
                onSound = appViewModel::setSound,
                onHaptics = appViewModel::setHaptics,
                onFastMovement = appViewModel::setFastMovement,
                onShowLabels = appViewModel::setShowSpaceLabels,
                onResetStats = appViewModel::resetStats,
                onBack = { if (entry.isResumed()) nav.popBackStack() }
            )
        }
        composable(Routes.ABOUT) { entry ->
            AboutScreen(onBack = { if (entry.isResumed()) nav.popBackStack() })
        }
    }
}
