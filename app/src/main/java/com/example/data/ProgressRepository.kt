package com.example.data

import android.content.Context
import com.example.engine.AmbientTheme
import com.example.engine.AppSettings
import com.example.engine.PlayerProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline persistence repository backed by SharedPreferences.
 * Stores unlocked levels, best star ratings per level, tutorial flags, and user settings.
 */
class ProgressRepository(context: Context) {

  private val prefs =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _progress = MutableStateFlow(loadProgress())
  val progress: StateFlow<PlayerProgress> = _progress.asStateFlow()

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<AppSettings> = _settings.asStateFlow()

  private fun loadProgress(): PlayerProgress {
    val unlocked = prefs.getInt(KEY_UNLOCKED_LEVEL, 1).coerceIn(1, MAX_LEVEL)
    val rawStars = prefs.getString(KEY_STARS_MAP, "") ?: ""
    val starsMap =
      rawStars
        .split(";")
        .mapNotNull { entry ->
          val parts = entry.split(":")
          if (parts.size == 2) {
            val id = parts[0].toIntOrNull()
            val stars = parts[1].toIntOrNull()
            if (id != null && stars != null && stars in 1..3) id to stars else null
          } else {
            null
          }
        }
        .toMap()

    val rawTimes = prefs.getString(KEY_BEST_TIMES_MAP, "") ?: ""
    val timesMap =
      rawTimes
        .split(";")
        .mapNotNull { entry ->
          val parts = entry.split(":")
          if (parts.size == 2) {
            val id = parts[0].toIntOrNull()
            val sec = parts[1].toIntOrNull()
            if (id != null && sec != null && sec >= 0) id to sec else null
          } else {
            null
          }
        }
        .toMap()

    val seenTutorial = prefs.getBoolean(KEY_SEEN_TUTORIAL, false)
    val seenTips =
      (prefs.getString(KEY_SEEN_TIPS, "") ?: "")
        .split(",")
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    return PlayerProgress(
      unlockedLevel = unlocked,
      starsByLevel = starsMap,
      bestTimeSecondsByLevel = timesMap,
      seenTutorial = seenTutorial,
      seenMechanicTips = seenTips,
    )
  }

  private fun loadSettings(): AppSettings {
    val rawTheme = prefs.getString(KEY_AMBIENT_THEME, null)
    val theme =
      AmbientTheme.entries.firstOrNull { it.name == rawTheme } ?: AmbientTheme.GLASS_PRISM
    return AppSettings(
      soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
      vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true),
      ambientTheme = theme,
    )
  }

  /**
   * Records level completion, preserving best stars and fastest time.
   * Returns true if [elapsedSeconds] set a new personal best time for [levelId].
   */
  fun recordLevelComplete(levelId: Int, stars: Int, elapsedSeconds: Int): Boolean {
    val current = _progress.value
    val prevStars = current.starsByLevel[levelId] ?: 0
    val bestStars = maxOf(prevStars, stars.coerceIn(1, 3))
    val nextStarsMap = current.starsByLevel.toMutableMap().apply { put(levelId, bestStars) }

    val prevBestTime = current.bestTimeSecondsByLevel[levelId]
    val isNewBestTime = prevBestTime == null || elapsedSeconds < prevBestTime
    val bestTime = if (prevBestTime == null || elapsedSeconds < prevBestTime) elapsedSeconds else prevBestTime
    val nextTimesMap =
      current.bestTimeSecondsByLevel.toMutableMap().apply { put(levelId, bestTime) }

    val nextUnlocked =
      if (levelId >= current.unlockedLevel) (levelId + 1).coerceAtMost(MAX_LEVEL)
      else current.unlockedLevel

    val updated =
      current.copy(
        unlockedLevel = nextUnlocked,
        starsByLevel = nextStarsMap,
        bestTimeSecondsByLevel = nextTimesMap,
      )
    _progress.value = updated

    val serializedStars = nextStarsMap.entries.joinToString(";") { "${it.key}:${it.value}" }
    val serializedTimes = nextTimesMap.entries.joinToString(";") { "${it.key}:${it.value}" }
    prefs
      .edit()
      .putInt(KEY_UNLOCKED_LEVEL, nextUnlocked)
      .putString(KEY_STARS_MAP, serializedStars)
      .putString(KEY_BEST_TIMES_MAP, serializedTimes)
      .apply()

    return isNewBestTime
  }

  fun markTutorialSeen() {
    val current = _progress.value
    if (!current.seenTutorial) {
      _progress.value = current.copy(seenTutorial = true)
      prefs.edit().putBoolean(KEY_SEEN_TUTORIAL, true).apply()
    }
  }

  fun markMechanicTipSeen(levelId: Int) {
    val current = _progress.value
    if (levelId !in current.seenMechanicTips) {
      val nextTips = current.seenMechanicTips + levelId
      _progress.value = current.copy(seenMechanicTips = nextTips)
      prefs.edit().putString(KEY_SEEN_TIPS, nextTips.joinToString(",")).apply()
    }
  }

  fun setSoundEnabled(enabled: Boolean) {
    _settings.value = _settings.value.copy(soundEnabled = enabled)
    prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
  }

  fun setVibrationEnabled(enabled: Boolean) {
    _settings.value = _settings.value.copy(vibrationEnabled = enabled)
    prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
  }

  fun setAmbientTheme(theme: AmbientTheme) {
    _settings.value = _settings.value.copy(ambientTheme = theme)
    prefs.edit().putString(KEY_AMBIENT_THEME, theme.name).apply()
  }

  fun resetProgress() {
    val updated =
      _progress.value.copy(
        unlockedLevel = 1,
        starsByLevel = emptyMap(),
        bestTimeSecondsByLevel = emptyMap(),
        seenMechanicTips = emptySet(),
      )
    _progress.value = updated
    prefs
      .edit()
      .putInt(KEY_UNLOCKED_LEVEL, 1)
      .putString(KEY_STARS_MAP, "")
      .putString(KEY_BEST_TIMES_MAP, "")
      .putString(KEY_SEEN_TIPS, "")
      .apply()
  }

  companion object {
    const val MAX_LEVEL = 50
    private const val PREFS_NAME = "prism_path_prefs"
    private const val KEY_UNLOCKED_LEVEL = "unlocked_level"
    private const val KEY_STARS_MAP = "stars_map"
    private const val KEY_BEST_TIMES_MAP = "best_times_map"
    private const val KEY_SEEN_TUTORIAL = "seen_tutorial"
    private const val KEY_SEEN_TIPS = "seen_mechanic_tips"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_AMBIENT_THEME = "ambient_theme"
  }
}
