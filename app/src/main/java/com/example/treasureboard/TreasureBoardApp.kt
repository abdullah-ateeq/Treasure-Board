package com.example.treasureboard

import android.app.Application
import com.example.treasureboard.data.SettingsRepository

class TreasureBoardApp : Application() {
    val repository: SettingsRepository by lazy { SettingsRepository(this) }
}
