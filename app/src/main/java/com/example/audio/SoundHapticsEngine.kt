package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.engine.AmbientTheme
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Pure offline procedural sound synthesizer (via Android AudioTrack) and haptic feedback helper.
 * Synthesizes both short UI SFX and seamless looping ambient soundscapes with zero audio files.
 */
class SoundHapticsEngine(context: Context) {

  private val appContext = context.applicationContext
  private val audioExecutor = Executors.newSingleThreadExecutor()
  private val ambientLock = Any()

  private var ambientTrack: AudioTrack? = null
  private var activeAmbientTheme: AmbientTheme = AmbientTheme.OFF

  private val vibrator: Vibrator? by lazy {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager =
          appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
    } catch (_: Throwable) {
      null
    }
  }

  fun updateAmbientAudio(theme: AmbientTheme, isForeground: Boolean) {
    val desired = if (isForeground) theme else AmbientTheme.OFF
    audioExecutor.execute {
      synchronized(ambientLock) {
        if (desired == activeAmbientTheme &&
          (desired == AmbientTheme.OFF || ambientTrack != null)
        ) {
          return@execute
        }

        stopAmbientInternal()
        activeAmbientTheme = desired
        if (desired == AmbientTheme.OFF) return@execute

        try {
          val samples = synthesizeAmbientLoop(desired)
          val byteCount = samples.size * 2
          val track =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(SAMPLE_RATE)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(byteCount)
              .setTransferMode(AudioTrack.MODE_STATIC)
              .build()

          track.write(samples, 0, samples.size)
          track.setLoopPoints(0, samples.size, -1)
          track.play()
          ambientTrack = track
        } catch (_: Throwable) {
          stopAmbientInternal()
        }
      }
    }
  }

  private fun stopAmbientInternal() {
    try {
      ambientTrack?.let { track ->
        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
          track.stop()
        }
        track.release()
      }
    } catch (_: Throwable) {
      // Safe no-op on headless/test devices
    } finally {
      ambientTrack = null
    }
  }

  fun playRotateClick(soundEnabled: Boolean, vibrationEnabled: Boolean) {
    if (vibrationEnabled) vibrateShort(14L, 70)
    if (soundEnabled) {
      playPcmAsync { synthesizeClick() }
    }
  }

  fun playTargetLitChime(soundEnabled: Boolean, vibrationEnabled: Boolean) {
    if (vibrationEnabled) vibrateShort(22L, 110)
    if (soundEnabled) {
      playPcmAsync { synthesizeChime(880.0, 1320.0, 0.16) }
    }
  }

  fun playHintPing(soundEnabled: Boolean, vibrationEnabled: Boolean) {
    if (vibrationEnabled) vibrateShort(18L, 80)
    if (soundEnabled) {
      playPcmAsync { synthesizeChime(740.0, 1110.0, 0.12) }
    }
  }

  fun playLevelCompleteJingle(soundEnabled: Boolean, vibrationEnabled: Boolean) {
    if (vibrationEnabled) vibrateSuccessPattern()
    if (soundEnabled) {
      playPcmAsync { synthesizeJingle() }
    }
  }

  private fun vibrateShort(durationMs: Long, amplitude: Int) {
    try {
      val v = vibrator ?: return
      if (!v.hasVibrator()) return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
      } else {
        @Suppress("DEPRECATION")
        v.vibrate(durationMs)
      }
    } catch (_: Throwable) {
      // Safe no-op on devices/tests without vibrator hardware
    }
  }

  private fun vibrateSuccessPattern() {
    try {
      val v = vibrator ?: return
      if (!v.hasVibrator()) return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(
          VibrationEffect.createWaveform(longArrayOf(0, 25, 45, 40), intArrayOf(0, 110, 0, 190), -1)
        )
      } else {
        @Suppress("DEPRECATION")
        v.vibrate(longArrayOf(0, 25, 45, 40), -1)
      }
    } catch (_: Throwable) {
      // Safe no-op
    }
  }

  private fun playPcmAsync(generator: () -> ShortArray) {
    audioExecutor.execute {
      try {
        val samples = generator()
        val byteCount = samples.size * 2
        val track =
          AudioTrack.Builder()
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
            .setBufferSizeInBytes(byteCount)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        val durationMs = (samples.size * 1000L) / SAMPLE_RATE
        Thread.sleep(durationMs + 40L)
        track.stop()
        track.release()
      } catch (_: Throwable) {
        // Ignore audio errors on headless/test environments
      }
    }
  }

  /**
   * Generates a seamless 6-second loop buffer where every harmonic & LFO completes
   * an integer number of cycles over the loop duration so there is zero click at wraparound.
   */
  internal fun synthesizeAmbientLoop(theme: AmbientTheme): ShortArray {
    val loopSec = 6.0
    val count = (SAMPLE_RATE * loopSec).toInt()

    // Snap frequency to nearest integer cycle count over loopSec for zero-seam looping.
    fun snappedFreq(hz: Double): Double = (hz * loopSec).roundToInt() / loopSec

    return when (theme) {
      AmbientTheme.OFF -> ShortArray(0)

      AmbientTheme.GLASS_PRISM -> {
        // Warm Cmaj9 crystalline pad: C3, G3, D4, E4, B4
        val freqs =
          doubleArrayOf(
            snappedFreq(130.81),
            snappedFreq(196.00),
            snappedFreq(293.66),
            snappedFreq(329.63),
            snappedFreq(493.88),
          )
        val weights = doubleArrayOf(0.34, 0.24, 0.16, 0.16, 0.10)
        val lfoHz = 1.0 / loopSec
        val shimmerHz = 3.0 / loopSec

        ShortArray(count) { i ->
          val t = i.toDouble() / SAMPLE_RATE
          val breath = 0.78 + 0.22 * sin(2.0 * PI * lfoHz * t)
          val shimmer = 0.55 + 0.45 * sin(2.0 * PI * shimmerHz * t)
          var sum = 0.0
          for (idx in freqs.indices) {
            val mod = if (idx >= 3) shimmer else breath
            sum += sin(2.0 * PI * freqs[idx] * t) * weights[idx] * mod
          }
          val sample = (sum * 0.12).coerceIn(-0.85, 0.85)
          (sample * Short.MAX_VALUE).toInt().toShort()
        }
      }

      AmbientTheme.NIGHT_OBSERVATORY -> {
        // Deep meditative A minor 9 drone: A2, E3, A3, B3
        val freqs =
          doubleArrayOf(
            snappedFreq(110.00),
            snappedFreq(164.81),
            snappedFreq(220.00),
            snappedFreq(246.94),
          )
        val weights = doubleArrayOf(0.42, 0.28, 0.18, 0.12)
        val slowLfo = 1.0 / loopSec
        val subLfo = 2.0 / loopSec

        ShortArray(count) { i ->
          val t = i.toDouble() / SAMPLE_RATE
          val swell = 0.72 + 0.28 * sin(2.0 * PI * slowLfo * t)
          val drift = 0.70 + 0.30 * sin(2.0 * PI * subLfo * t + 1.1)
          var sum = 0.0
          for (idx in freqs.indices) {
            val mod = if (idx < 2) swell else drift
            sum += sin(2.0 * PI * freqs[idx] * t) * weights[idx] * mod
          }
          val sample = (sum * 0.13).coerceIn(-0.85, 0.85)
          (sample * Short.MAX_VALUE).toInt().toShort()
        }
      }

      AmbientTheme.SOLAR_DRIFT -> {
        // Luminous D major add9 amber pulse: D3, A3, E4, F#4
        val freqs =
          doubleArrayOf(
            snappedFreq(146.83),
            snappedFreq(220.00),
            snappedFreq(329.63),
            snappedFreq(369.99),
          )
        val weights = doubleArrayOf(0.34, 0.26, 0.22, 0.18)
        val pulseLfo = 2.0 / loopSec
        val highLfo = 4.0 / loopSec

        ShortArray(count) { i ->
          val t = i.toDouble() / SAMPLE_RATE
          val pad = 0.74 + 0.26 * sin(2.0 * PI * pulseLfo * t)
          val glint = 0.5 + 0.5 * sin(2.0 * PI * highLfo * t + 0.6)
          var sum = 0.0
          for (idx in freqs.indices) {
            val mod = if (idx >= 2) glint else pad
            sum += sin(2.0 * PI * freqs[idx] * t) * weights[idx] * mod
          }
          val sample = (sum * 0.11).coerceIn(-0.85, 0.85)
          (sample * Short.MAX_VALUE).toInt().toShort()
        }
      }
    }
  }

  private fun synthesizeClick(): ShortArray {
    val durationSec = 0.042
    val count = (SAMPLE_RATE * durationSec).toInt()
    return ShortArray(count) { i ->
      val t = i.toDouble() / SAMPLE_RATE
      val freq = 720.0 - 340.0 * (i.toDouble() / count)
      val env = exp(-t * 95.0)
      val sample = sin(2.0 * PI * freq * t) * env * 0.45
      (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
  }

  private fun synthesizeChime(f1: Double, f2: Double, durationSec: Double): ShortArray {
    val count = (SAMPLE_RATE * durationSec).toInt()
    return ShortArray(count) { i ->
      val t = i.toDouble() / SAMPLE_RATE
      val attack = (t / 0.008).coerceAtMost(1.0)
      val env = attack * exp(-t * 16.0)
      val wave = 0.65 * sin(2.0 * PI * f1 * t) + 0.35 * sin(2.0 * PI * f2 * t)
      val sample = wave * env * 0.42
      (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
  }

  private fun synthesizeJingle(): ShortArray {
    val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
    val noteSpacing = 0.085
    val totalSec = 0.56
    val count = (SAMPLE_RATE * totalSec).toInt()
    return ShortArray(count) { i ->
      val t = i.toDouble() / SAMPLE_RATE
      var sum = 0.0
      for (idx in notes.indices) {
        val localT = t - idx * noteSpacing
        if (localT >= 0.0) {
          val attack = (localT / 0.01).coerceAtMost(1.0)
          val decay = exp(-localT * 8.5)
          val freq = notes[idx]
          sum += (0.7 * sin(2.0 * PI * freq * localT) + 0.3 * sin(2.0 * PI * freq * 2.0 * localT)) *
            attack * decay
        }
      }
      val sample = (sum * 0.32).coerceIn(-0.95, 0.95)
      (sample * Short.MAX_VALUE).toInt().toShort()
    }
  }

  fun release() {
    synchronized(ambientLock) {
      stopAmbientInternal()
    }
    audioExecutor.shutdownNow()
  }

  companion object {
    private const val SAMPLE_RATE = 22050
  }
}
