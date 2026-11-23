package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GridPos
import com.example.engine.Rules
import com.example.levels.Levels
import com.example.ui.components.GameBoard
import com.example.ui.components.GlassCard
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.MechanicTutorialDialog
import com.example.ui.components.PrismBackdropBox
import com.example.ui.components.PrismGlassButton
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
  state: GameUiState,
  bestTimeSeconds: Int?,
  onTapCell: (GridPos) -> Unit,
  onUndo: () -> Unit,
  onReset: () -> Unit,
  onHint: () -> Unit,
  onBack: () -> Unit,
  onSettings: () -> Unit,
  onNextLevel: () -> Unit,
  onHome: () -> Unit,
  onDismissTip: () -> Unit,
  onShowTip: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val level = state.activeLevel

  PrismBackdropBox(
    modifier = Modifier.fillMaxSize(),
    radialSpotlight = true,
  ) {
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .safeDrawingPadding()
          .padding(horizontal = PrismTokens.Spacing.md, vertical = PrismTokens.Spacing.xs),
      contentAlignment = Alignment.Center,
    ) {
      val isLandscape = maxWidth > maxHeight && maxWidth > 600.dp

      if (isLandscape) {
        Row(
          modifier = Modifier.fillMaxSize().widthIn(max = 960.dp),
          horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.lg),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            GameTopHud(
              levelId = level.id,
              title = level.title,
              bestTimeSeconds = bestTimeSeconds,
              hasTip = level.id in GameViewModel.TUTORIAL_TIP_LEVELS,
              onBack = onBack,
              onShowTip = onShowTip,
              onSettings = onSettings,
            )
            GameStatsRow(
              moves = state.moves,
              par = level.par,
              elapsedSeconds = state.elapsedSeconds,
              litCount = state.traceResult.litTargets.size,
              totalTargets = level.targetPositions.size,
            )
            GameActionBar(
              canUndo = state.canUndo,
              hintsRemaining = state.hintsRemaining,
              onUndo = onUndo,
              onReset = onReset,
              onHint = onHint,
            )
          }

          Box(
            modifier = Modifier.weight(1.25f).fillMaxHeight(),
            contentAlignment = Alignment.Center,
          ) {
            GameBoard(
              level = level,
              orientations = state.orientations,
              visualAngles = state.visualAngles,
              traceResult = state.traceResult,
              activeHintCell = state.activeHintCell,
              onCellTap = onTapCell,
            )
          }
        }
      } else {
        Column(
          modifier = Modifier.widthIn(max = 520.dp).fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          GameTopHud(
            levelId = level.id,
            title = level.title,
            bestTimeSeconds = bestTimeSeconds,
            hasTip = level.id in GameViewModel.TUTORIAL_TIP_LEVELS,
            onBack = onBack,
            onShowTip = onShowTip,
            onSettings = onSettings,
          )

          Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = PrismTokens.Spacing.sm),
            contentAlignment = Alignment.Center,
          ) {
            GameBoard(
              level = level,
              orientations = state.orientations,
              visualAngles = state.visualAngles,
              traceResult = state.traceResult,
              activeHintCell = state.activeHintCell,
              onCellTap = onTapCell,
            )
          }

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
          ) {
            GameStatsRow(
              moves = state.moves,
              par = level.par,
              elapsedSeconds = state.elapsedSeconds,
              litCount = state.traceResult.litTargets.size,
              totalTargets = level.targetPositions.size,
            )
            GameActionBar(
              canUndo = state.canUndo,
              hintsRemaining = state.hintsRemaining,
              onUndo = onUndo,
              onReset = onReset,
              onHint = onHint,
            )
          }
        }
      }
    }
  }

  if (state.mechanicTipLevelId != null) {
    MechanicTutorialDialog(
      levelId = state.mechanicTipLevelId,
      onDismiss = onDismissTip,
    )
  }

  if (state.showCompleteModal) {
    LevelCompleteDialog(
      levelId = level.id,
      hasNextLevel = level.id < Levels.all.size,
      movesUsed = state.moves,
      par = level.par,
      elapsedSeconds = state.elapsedSeconds,
      bestTimeSeconds = bestTimeSeconds,
      isNewBestTime = state.isNewBestTime,
      starsEarned = state.earnedStars,
      onNextLevel = onNextLevel,
      onReplay = onReset,
      onHome = onHome,
    )
  }
}

@Composable
private fun GameTopHud(
  levelId: Int,
  title: String,
  bestTimeSeconds: Int?,
  hasTip: Boolean,
  onBack: () -> Unit,
  onShowTip: () -> Unit,
  onSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = onBack,
      modifier = Modifier.minimumInteractiveComponentSize().testTag("game_back_button"),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = "Back",
        tint = PrismTokens.Colors.TextPrimary,
      )
    }

    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text =
          if (bestTimeSeconds != null) "LEVEL $levelId · BEST ${Rules.formatTime(bestTimeSeconds)}"
          else "LEVEL $levelId",
        style = MaterialTheme.typography.labelSmall,
        color = PrismTokens.Colors.Amber,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = PrismTokens.Colors.TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (hasTip) {
        IconButton(
          onClick = onShowTip,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("show_tip_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = "Mechanic Tip",
            tint = PrismTokens.Colors.BeamBlue,
          )
        }
      }
      IconButton(
        onClick = onSettings,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("game_settings_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "Settings",
          tint = PrismTokens.Colors.TextSecondary,
        )
      }
    }
  }
}

@Composable
private fun GameStatsRow(
  moves: Int,
  par: Int,
  elapsedSeconds: Int,
  litCount: Int,
  totalTargets: Int,
) {
  GlassCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = PrismTokens.Spacing.md, vertical = PrismTokens.Spacing.sm),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      StatCell(
        label = "MOVES",
        value = "$moves",
        valueTag = "moves_counter",
      )
      StatCell(
        label = "PAR",
        value = "$par",
      )
      StatCell(
        label = "TIME",
        value = Rules.formatTime(elapsedSeconds),
        valueColor = PrismTokens.Colors.TextPrimary,
        valueTag = "timer_display",
      )
      StatCell(
        label = "TARGETS",
        value = "$litCount/$totalTargets",
        valueColor =
          if (litCount == totalTargets) PrismTokens.Colors.Success
          else PrismTokens.Colors.TextPrimary,
      )
    }
  }
}

@Composable
private fun StatCell(
  label: String,
  value: String,
  valueColor: androidx.compose.ui.graphics.Color = PrismTokens.Colors.TextPrimary,
  valueTag: String? = null,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = PrismTokens.Colors.TextSecondary,
    )
    Spacer(Modifier.height(2.dp))
    Text(
      text = value,
      style = TabularNumberStyle.copy(fontSize = 18.sp),
      color = valueColor,
      modifier = if (valueTag != null) Modifier.testTag(valueTag) else Modifier,
    )
  }
}

@Composable
private fun GameActionBar(
  canUndo: Boolean,
  hintsRemaining: Int,
  onUndo: () -> Unit,
  onReset: () -> Unit,
  onHint: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(bottom = PrismTokens.Spacing.xxs),
    horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
  ) {
    PrismGlassButton(
      onClick = onUndo,
      enabled = canUndo,
      minHeight = 50.dp,
      contentPadding = PaddingValues(horizontal = PrismTokens.Spacing.sm, vertical = PrismTokens.Spacing.sm),
      modifier = Modifier.weight(1f).testTag("undo_button"),
    ) {
      Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(6.dp))
      Text("Undo", style = MaterialTheme.typography.labelLarge)
    }

    PrismGlassButton(
      onClick = onReset,
      minHeight = 50.dp,
      contentPadding = PaddingValues(horizontal = PrismTokens.Spacing.sm, vertical = PrismTokens.Spacing.sm),
      modifier = Modifier.weight(1f).testTag("reset_button"),
    ) {
      Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(6.dp))
      Text("Reset", style = MaterialTheme.typography.labelLarge)
    }

    PrismGlassButton(
      onClick = onHint,
      enabled = hintsRemaining > 0,
      minHeight = 50.dp,
      borderColor =
        if (hintsRemaining > 0) PrismTokens.Colors.Amber.copy(alpha = 0.50f)
        else PrismTokens.Colors.GlassBorder,
      contentColor =
        if (hintsRemaining > 0) PrismTokens.Colors.Amber else PrismTokens.Colors.TextSecondary,
      contentPadding = PaddingValues(horizontal = PrismTokens.Spacing.sm, vertical = PrismTokens.Spacing.sm),
      modifier = Modifier.weight(1.1f).testTag("hint_button"),
    ) {
      Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(6.dp))
      Text("Hint", style = MaterialTheme.typography.labelLarge)
      Spacer(Modifier.width(6.dp))
      Surface(
        shape = PrismTokens.Radii.pillShape,
        color =
          if (hintsRemaining > 0) PrismTokens.Colors.AmberSubtle
          else PrismTokens.Colors.GlassSurface,
        border =
          BorderStroke(
            1.dp,
            if (hintsRemaining > 0) PrismTokens.Colors.Amber.copy(alpha = 0.45f)
            else PrismTokens.Colors.GlassBorder,
          ),
      ) {
        Text(
          text = hintsRemaining.toString(),
          style = TabularNumberStyle.copy(fontSize = 11.sp),
          color =
            if (hintsRemaining > 0) PrismTokens.Colors.Amber
            else PrismTokens.Colors.TextSecondary,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
      }
    }
  }
}
