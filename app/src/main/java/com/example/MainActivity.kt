package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.AppScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PrismPathTheme
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.rememberReducedMotion
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
      PrismPathTheme {
        PrismPathApp()
      }
    }
  }
}

@Composable
fun PrismPathApp(vm: GameViewModel = viewModel()) {
  val uiState by vm.uiState.collectAsStateWithLifecycle()
  val progress by vm.progress.collectAsStateWithLifecycle()
  val settings by vm.settings.collectAsStateWithLifecycle()
  val reducedMotion = rememberReducedMotion()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> vm.setAppForeground(true)
        Lifecycle.Event.ON_PAUSE -> vm.setAppForeground(false)
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  if (uiState.showFirstLaunchTutorial) {
    HowToPlayScreen(
      isFirstLaunch = true,
      onDone = vm::dismissFirstLaunchTutorial,
    )
    return
  }

  val transitionMs = if (reducedMotion) 0 else PrismTokens.Motion.ScreenTransitionMs

  AnimatedContent(
    targetState = uiState.currentScreen,
    transitionSpec = {
      val enter =
        fadeIn(tween(transitionMs, easing = FastOutSlowInEasing)) +
          slideInVertically(tween(transitionMs, easing = FastOutSlowInEasing)) { it / 28 }
      val exit =
        fadeOut(tween(transitionMs, easing = FastOutSlowInEasing)) +
          slideOutVertically(tween(transitionMs, easing = FastOutSlowInEasing)) { -it / 36 }
      enter togetherWith exit
    },
    label = "screen_nav",
  ) { screen ->
    when (screen) {
      AppScreen.Home ->
        HomeScreen(
          progress = progress,
          onPlay = vm::playContinue,
          onLevels = vm::openLevelSelect,
          onHowToPlay = vm::openHowToPlay,
          onSettings = vm::openSettings,
        )

      AppScreen.LevelSelect ->
        LevelSelectScreen(
          progress = progress,
          onSelectLevel = vm::startLevel,
          onBack = vm::navigateHome,
        )

      is AppScreen.Game ->
        GameScreen(
          state = uiState,
          bestTimeSeconds = progress.bestTimeSecondsByLevel[uiState.activeLevel.id],
          onTapCell = vm::tapCell,
          onUndo = vm::undo,
          onReset = vm::resetLevel,
          onHint = vm::useHint,
          onBack = vm::openLevelSelect,
          onSettings = vm::openSettings,
          onNextLevel = vm::nextLevel,
          onHome = vm::navigateHome,
          onDismissTip = vm::dismissMechanicTip,
          onShowTip = vm::showMechanicTipForCurrentLevel,
        )

      AppScreen.Settings ->
        SettingsScreen(
          settings = settings,
          onSoundChanged = vm::setSoundEnabled,
          onVibrationChanged = vm::setVibrationEnabled,
          onAmbientThemeChanged = vm::setAmbientTheme,
          onResetProgress = vm::resetAllProgress,
          onBack = vm::navigateBackFromSettings,
        )

      AppScreen.HowToPlay ->
        HowToPlayScreen(
          isFirstLaunch = false,
          onDone = vm::navigateHome,
        )
    }
  }
}
