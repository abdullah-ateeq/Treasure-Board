package com.example.treasureboard

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.treasureboard.ui.TreasureNavHost
import com.example.treasureboard.ui.theme.TreasureBoardTheme
import com.example.treasureboard.ui.theme.isAppInDarkTheme

class MainActivity : ComponentActivity() {

    private val repository get() = (application as TreasureBoardApp).repository

    private val appViewModel: AppViewModel by viewModels {
        viewModelFactory { initializer { AppViewModel(repository) } }
    }

    private val gameViewModel: GameViewModel by viewModels {
        viewModelFactory { initializer { GameViewModel(repository) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            val dark = isAppInDarkTheme(settings.themeMode)
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            TreasureBoardTheme(darkTheme = dark) {
                TreasureNavHost(appViewModel, gameViewModel)
            }
        }
    }
}
