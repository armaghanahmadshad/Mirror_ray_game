package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundHapticsEngine
import com.example.data.ProgressRepository
import com.example.engine.AmbientTheme
import com.example.engine.AppScreen
import com.example.engine.AppSettings
import com.example.engine.GridPos
import com.example.engine.LevelDef
import com.example.engine.MirrorOrientation
import com.example.engine.PlayerProgress
import com.example.engine.Rules
import com.example.engine.TraceBeam
import com.example.engine.TraceResult
import com.example.levels.Levels
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MoveSnapshot(
  val orientations: Map<GridPos, MirrorOrientation>,
  val visualAngles: Map<GridPos, Float>,
)

data class GameUiState(
  val currentScreen: AppScreen = AppScreen.Home,
  val activeLevel: LevelDef = Levels.all.first(),
  val orientations: Map<GridPos, MirrorOrientation> = Rules.initialOrientations(Levels.all.first()),
  val visualAngles: Map<GridPos, Float> = emptyMap(),
  val traceResult: TraceResult =
    TraceBeam.trace(Levels.all.first(), Rules.initialOrientations(Levels.all.first())),
  val moves: Int = 0,
  val elapsedSeconds: Int = 0,
  val isNewBestTime: Boolean = false,
  val canUndo: Boolean = false,
  val hintsRemaining: Int = 3,
  val activeHintCell: GridPos? = null,
  val showCompleteModal: Boolean = false,
  val earnedStars: Int = 0,
  val mechanicTipLevelId: Int? = null,
  val showFirstLaunchTutorial: Boolean = false,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ProgressRepository(application)
  private val soundHaptics = SoundHapticsEngine(application)

  val progress: StateFlow<PlayerProgress> =
    repository.progress.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = repository.progress.value,
    )

  val settings: StateFlow<AppSettings> =
    repository.settings.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = repository.settings.value,
    )

  private val undoStack = ArrayDeque<MoveSnapshot>()
  private var isTransitioningComplete = false
  private var isAppForeground = true
  private var timerJob: Job? = null
  private var settingsReturnScreen: AppScreen = AppScreen.Home

  private val _uiState =
    MutableStateFlow(
      GameUiState(
        showFirstLaunchTutorial = !repository.progress.value.seenTutorial,
      )
    )
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  companion object {
    const val MAX_HINTS_PER_LEVEL = 3
    val TUTORIAL_TIP_LEVELS = setOf(1, 6, 11, 16, 26, 41)
  }

  private fun syncTimerRunning() {
    val state = _uiState.value
    val shouldRun =
      state.currentScreen is AppScreen.Game &&
        state.mechanicTipLevelId == null &&
        !state.showCompleteModal &&
        !isTransitioningComplete &&
        isAppForeground

    if (shouldRun) {
      if (timerJob?.isActive == true) return
      timerJob =
        viewModelScope.launch {
          while (isActive) {
            delay(1000L)
            val curr = _uiState.value
            val stillActive =
              curr.currentScreen is AppScreen.Game &&
                curr.mechanicTipLevelId == null &&
                !curr.showCompleteModal &&
                !isTransitioningComplete &&
                isAppForeground
            if (stillActive) {
              _uiState.value = curr.copy(elapsedSeconds = curr.elapsedSeconds + 1)
            } else {
              break
            }
          }
        }
    } else {
      timerJob?.cancel()
      timerJob = null
    }
  }

  fun setAppForeground(inForeground: Boolean) {
    isAppForeground = inForeground
    syncTimerRunning()
    soundHaptics.updateAmbientAudio(settings.value.ambientTheme, inForeground)
  }

  fun dismissFirstLaunchTutorial() {
    repository.markTutorialSeen()
    _uiState.value = _uiState.value.copy(showFirstLaunchTutorial = false)
  }

  fun openHowToPlay() {
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.HowToPlay)
  }

  fun openLevelSelect() {
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.LevelSelect)
    syncTimerRunning()
  }

  fun openSettings() {
    val current = _uiState.value.currentScreen
    settingsReturnScreen = if (current is AppScreen.Game) current else AppScreen.Home
    _uiState.value = _uiState.value.copy(currentScreen = AppScreen.Settings)
    syncTimerRunning()
  }

  fun navigateBackFromSettings() {
    val target = settingsReturnScreen
    _uiState.value = _uiState.value.copy(currentScreen = target)
    syncTimerRunning()
  }

  fun navigateHome() {
    isTransitioningComplete = false
    settingsReturnScreen = AppScreen.Home
    _uiState.value =
      _uiState.value.copy(
        currentScreen = AppScreen.Home,
        showCompleteModal = false,
      )
    syncTimerRunning()
  }

  fun playContinue() {
    val unlocked = progress.value.unlockedLevel.coerceIn(1, Levels.all.size)
    startLevel(unlocked)
  }

  fun startLevel(levelId: Int) {
    val level = Levels.byId(levelId)
    val initial = Rules.initialOrientations(level)
    val initialAngles =
      initial.mapValues { (_, orient) ->
        if (orient == MirrorOrientation.SLASH) 0f else 90f
      }
    val initialTrace = TraceBeam.trace(level, initial)
    undoStack.clear()
    isTransitioningComplete = false

    val shouldShowTip =
      level.id in TUTORIAL_TIP_LEVELS && level.id !in progress.value.seenMechanicTips

    _uiState.value =
      _uiState.value.copy(
        currentScreen = AppScreen.Game(level.id),
        activeLevel = level,
        orientations = initial,
        visualAngles = initialAngles,
        traceResult = initialTrace,
        moves = 0,
        elapsedSeconds = 0,
        isNewBestTime = false,
        canUndo = false,
        hintsRemaining = MAX_HINTS_PER_LEVEL,
        activeHintCell = null,
        showCompleteModal = false,
        earnedStars = 0,
        mechanicTipLevelId = if (shouldShowTip) level.id else null,
      )
    timerJob?.cancel()
    timerJob = null
    syncTimerRunning()
  }

  fun dismissMechanicTip() {
    val tipLevel = _uiState.value.mechanicTipLevelId ?: return
    repository.markMechanicTipSeen(tipLevel)
    _uiState.value = _uiState.value.copy(mechanicTipLevelId = null)
    syncTimerRunning()
  }

  fun showMechanicTipForCurrentLevel() {
    val id = _uiState.value.activeLevel.id
    if (id in TUTORIAL_TIP_LEVELS) {
      _uiState.value = _uiState.value.copy(mechanicTipLevelId = id)
      syncTimerRunning()
    }
  }

  fun tapCell(pos: GridPos) {
    val state = _uiState.value
    if (state.showCompleteModal || isTransitioningComplete) return

    val spec = state.activeLevel.cellMap[pos] ?: return
    if (!Rules.isRotatable(spec)) return

    val currentOrient = state.orientations[pos] ?: return
    val prevLitCount = state.traceResult.litTargets.size

    undoStack.addLast(MoveSnapshot(state.orientations, state.visualAngles))

    val nextOrientations =
      state.orientations.toMutableMap().apply {
        this[pos] = currentOrient.toggled()
      }
    val nextAngles =
      state.visualAngles.toMutableMap().apply {
        this[pos] = (this[pos] ?: 0f) + 90f
      }
    val nextTrace = TraceBeam.trace(state.activeLevel, nextOrientations)
    val nextMoves = state.moves + 1

    val audio = settings.value
    soundHaptics.playRotateClick(audio.soundEnabled, audio.vibrationEnabled)

    if (!nextTrace.isSolved && nextTrace.litTargets.size > prevLitCount) {
      soundHaptics.playTargetLitChime(audio.soundEnabled, audio.vibrationEnabled)
    }

    val clearedHint = if (state.activeHintCell == pos) null else state.activeHintCell

    _uiState.value =
      state.copy(
        orientations = nextOrientations,
        visualAngles = nextAngles,
        traceResult = nextTrace,
        moves = nextMoves,
        canUndo = undoStack.isNotEmpty(),
        activeHintCell = clearedHint,
      )

    if (nextTrace.isSolved) {
      isTransitioningComplete = true
      syncTimerRunning()
      val stars = Rules.computeStars(nextMoves, state.activeLevel.par)
      val isNewBest =
        repository.recordLevelComplete(
          levelId = state.activeLevel.id,
          stars = stars,
          elapsedSeconds = state.elapsedSeconds,
        )
      soundHaptics.playLevelCompleteJingle(audio.soundEnabled, audio.vibrationEnabled)
      viewModelScope.launch {
        delay(280L)
        _uiState.value =
          _uiState.value.copy(
            showCompleteModal = true,
            earnedStars = stars,
            isNewBestTime = isNewBest,
          )
      }
    }
  }

  fun undo() {
    val state = _uiState.value
    if (state.showCompleteModal || isTransitioningComplete) return
    val prev = undoStack.removeLastOrNull() ?: return

    val nextTrace = TraceBeam.trace(state.activeLevel, prev.orientations)
    val audio = settings.value
    soundHaptics.playRotateClick(audio.soundEnabled, audio.vibrationEnabled)

    _uiState.value =
      state.copy(
        orientations = prev.orientations,
        visualAngles = prev.visualAngles,
        traceResult = nextTrace,
        moves = (state.moves - 1).coerceAtLeast(0),
        canUndo = undoStack.isNotEmpty(),
        activeHintCell = null,
      )
  }

  fun resetLevel() {
    startLevel(_uiState.value.activeLevel.id)
  }

  fun useHint() {
    val state = _uiState.value
    if (state.showCompleteModal || isTransitioningComplete) return
    if (state.hintsRemaining <= 0) return

    val hintPos = Rules.findHintCell(state.activeLevel, state.orientations) ?: return
    val audio = settings.value
    soundHaptics.playHintPing(audio.soundEnabled, audio.vibrationEnabled)

    _uiState.value =
      state.copy(
        hintsRemaining = state.hintsRemaining - 1,
        activeHintCell = hintPos,
      )
  }

  fun nextLevel() {
    val currentId = _uiState.value.activeLevel.id
    val nextId = if (currentId < Levels.all.size) currentId + 1 else 1
    startLevel(nextId)
  }

  fun setSoundEnabled(enabled: Boolean) {
    repository.setSoundEnabled(enabled)
  }

  fun setVibrationEnabled(enabled: Boolean) {
    repository.setVibrationEnabled(enabled)
  }

  fun setAmbientTheme(theme: AmbientTheme) {
    repository.setAmbientTheme(theme)
    soundHaptics.updateAmbientAudio(theme, isAppForeground)
  }

  fun resetAllProgress() {
    repository.resetProgress()
  }

  override fun onCleared() {
    super.onCleared()
    soundHaptics.release()
  }
}
