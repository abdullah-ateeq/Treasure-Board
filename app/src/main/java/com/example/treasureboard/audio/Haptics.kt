package com.example.treasureboard.audio

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

enum class Haptic { VERY_LIGHT, SUCCESS, STRONG_SUCCESS, WARNING, WIN }

/** Maps game moments to platform haptic constants; needs no VIBRATE permission. */
class Haptics(private val view: View) {

    fun perform(haptic: Haptic) {
        when (haptic) {
            Haptic.VERY_LIGHT -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            Haptic.SUCCESS -> view.performHapticFeedback(confirm)
            Haptic.STRONG_SUCCESS -> {
                view.performHapticFeedback(confirm)
                view.postDelayed({ view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }, 90)
            }
            Haptic.WARNING -> view.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
                else HapticFeedbackConstants.LONG_PRESS
            )
            Haptic.WIN -> {
                view.performHapticFeedback(confirm)
                view.postDelayed({ view.performHapticFeedback(confirm) }, 140)
                view.postDelayed({ view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }, 300)
            }
        }
    }

    private val confirm: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.VIRTUAL_KEY
}
