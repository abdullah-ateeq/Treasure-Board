package com.example.treasureboard.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class Sound { DICE, MOVE, COIN, TREASURE, GOLD, TRAP, BONUS, WIN }

/**
 * Subtle synthesized effects — no audio assets needed. Each sound is rendered once into a
 * static [AudioTrack] and replayed from the start on demand.
 */
class SoundPlayer {

    private val tracks = mutableMapOf<Sound, AudioTrack>()

    init {
        Sound.entries.forEach { sound ->
            runCatching { tracks[sound] = buildTrack(render(sound)) }
        }
    }

    fun play(sound: Sound) {
        val track = tracks[sound] ?: return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    fun release() {
        tracks.values.forEach { runCatching { it.release() } }
        tracks.clear()
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    private fun render(sound: Sound): ShortArray = when (sound) {
        Sound.DICE -> mix(260) {
            noise(0, 28, 0.30f); noise(70, 24, 0.24f); noise(140, 22, 0.20f); noise(205, 20, 0.14f)
        }
        Sound.MOVE -> mix(45) { bell(0, 40, 880f, 0.10f, decay = 90f); noise(0, 8, 0.05f) }
        Sound.COIN -> mix(240) { bell(0, 90, 1568f, 0.20f); bell(70, 170, 2093f, 0.20f) }
        Sound.TREASURE -> mix(420) {
            bell(0, 160, 1047f, 0.18f); bell(80, 180, 1319f, 0.18f); bell(160, 260, 1568f, 0.20f)
        }
        Sound.GOLD -> mix(620) {
            bell(0, 200, 784f, 0.18f); bell(90, 220, 988f, 0.18f)
            bell(180, 240, 1175f, 0.18f); bell(270, 350, 1568f, 0.22f); bell(270, 350, 784f, 0.10f)
        }
        Sound.TRAP -> mix(340) { bell(0, 170, 233f, 0.26f, decay = 8f); bell(120, 220, 175f, 0.26f, decay = 8f) }
        Sound.BONUS -> mix(200) { bell(0, 70, 659f, 0.14f); bell(55, 70, 880f, 0.14f); bell(110, 90, 1175f, 0.14f) }
        Sound.WIN -> mix(900) {
            bell(0, 300, 523f, 0.16f); bell(120, 300, 659f, 0.16f); bell(240, 320, 784f, 0.16f)
            bell(380, 520, 1047f, 0.20f); bell(380, 520, 523f, 0.10f)
        }
    }

    private class Mixer(val buffer: FloatArray) {
        private val random = Random(11)

        fun bell(startMs: Int, lengthMs: Int, freq: Float, volume: Float, decay: Float = 14f) {
            val start = startMs * SAMPLE_RATE / 1000
            val length = lengthMs * SAMPLE_RATE / 1000
            val attack = SAMPLE_RATE / 200
            for (i in 0 until length) {
                val idx = start + i
                if (idx >= buffer.size) break
                val t = i.toFloat() / SAMPLE_RATE
                val env = min(1f, i.toFloat() / attack) * exp(-decay * t * (1000f / lengthMs) / 10f)
                val phase = 2 * PI * freq * t
                val tone = sin(phase) + 0.35 * sin(2 * phase) + 0.12 * sin(3 * phase)
                buffer[idx] += (tone * env * volume).toFloat()
            }
        }

        fun noise(startMs: Int, lengthMs: Int, volume: Float) {
            val start = startMs * SAMPLE_RATE / 1000
            val length = lengthMs * SAMPLE_RATE / 1000
            var last = 0f
            for (i in 0 until length) {
                val idx = start + i
                if (idx >= buffer.size) break
                val env = exp(-6f * i / length)
                last = last * 0.55f + (random.nextFloat() * 2f - 1f) * 0.45f // soft low-pass, wooden click
                buffer[idx] += last * env * volume
            }
        }
    }

    private inline fun mix(lengthMs: Int, block: Mixer.() -> Unit): ShortArray {
        val mixer = Mixer(FloatArray(lengthMs * SAMPLE_RATE / 1000))
        mixer.block()
        val fadeOut = min(mixer.buffer.size, SAMPLE_RATE / 100)
        return ShortArray(mixer.buffer.size) { i ->
            val tail = mixer.buffer.size - i
            val gain = if (tail < fadeOut) tail.toFloat() / fadeOut else 1f
            (mixer.buffer[i].coerceIn(-1f, 1f) * gain * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 22050
    }
}
