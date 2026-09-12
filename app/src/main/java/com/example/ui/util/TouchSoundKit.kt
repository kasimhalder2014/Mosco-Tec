package com.example.ui.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * TouchSoundKit provides immediate, crisp, high-fidelity tactile click and touch sound
 * effects using native Android AudioManager sound effects and ToneGenerator.
 *
 * This eliminates heavy disk IO and MediaCodec hardware component query errors.
 */
object TouchSoundKit {

    private var audioManager: AudioManager? = null
    private var toneGenerator: ToneGenerator? = null
    private var isInitialized = false

    // User preferences
    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true
    var volumeLevel: Float = 0.85f

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val appCtx = context.applicationContext
            audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, (volumeLevel * 90).toInt().coerceIn(1, 100))
            } catch (_: Exception) {
                toneGenerator = null
            }
            isInitialized = true
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    /**
     * Standard button, card, and tab touch click.
     */
    fun playTap(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volumeLevel)
        } catch (_: Exception) {}
        triggerHaptic(context, 12)
    }

    /**
     * Soft pop for filters, dropdowns, switches.
     */
    fun playPop(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            audioManager?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, volumeLevel)
        } catch (_: Exception) {}
        triggerHaptic(context, 15)
    }

    /**
     * Melodic chime when marking a beneficiary as SERVED.
     */
    fun playServed(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            if (toneGenerator != null) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } else {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN, volumeLevel)
            }
        } catch (_: Exception) {}
        triggerHaptic(context, 35)
    }

    /**
     * Soft safety tone when reverting to UNSERVED.
     */
    fun playUnserved(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            if (toneGenerator != null) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 80)
            } else {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, volumeLevel)
            }
        } catch (_: Exception) {}
        triggerHaptic(context, 20)
    }

    /**
     * Crisp micro-tick for keyboard and PIN number pad typing.
     */
    fun playKeypad(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volumeLevel)
        } catch (_: Exception) {}
        triggerHaptic(context, 6)
    }

    /**
     * Harmonic confirmation tone for save, export, import.
     */
    fun playSuccess(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            if (toneGenerator != null) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            } else {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN, volumeLevel)
            }
        } catch (_: Exception) {}
        triggerHaptic(context, 40)
    }

    /**
     * Negative crisp click for delete actions.
     */
    fun playDelete(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            ensureInit(context)
            if (toneGenerator != null) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 70)
            } else {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, volumeLevel)
            }
        } catch (_: Exception) {}
        triggerHaptic(context, 25)
    }

    private fun ensureInit(context: Context?) {
        if (!isInitialized && context != null) {
            init(context)
        }
    }

    private fun triggerHaptic(context: Context?, durationMs: Long) {
        if (!isHapticEnabled || context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
