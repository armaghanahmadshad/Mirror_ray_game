package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LensBlur
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.CellDef
import com.example.engine.CellSpec
import com.example.engine.Direction
import com.example.engine.GridPos
import com.example.engine.LaserSource
import com.example.engine.LevelDef
import com.example.engine.MirrorOrientation
import com.example.engine.TraceBeam
import com.example.ui.components.GameBoard
import com.example.ui.components.GlassCard
import com.example.ui.components.PrismBackdropBox
import com.example.ui.components.PrismPrimaryButton
import com.example.ui.theme.PrismTokens

@Composable
fun HowToPlayScreen(
  isFirstLaunch: Boolean,
  onDone: () -> Unit,
) {
  BackHandler(onBack = onDone)

  val sandboxLevel = remember {
    LevelDef(
      id = 0,
      size = 3,
      par = 1,
      source = LaserSource(1, 0, Direction.RIGHT),
      cells =
        listOf(
          CellDef(1, 1, CellSpec.Mirror(MirrorOrientation.SLASH, rotatable = true)),
          CellDef(2, 1, CellSpec.Target()),
        ),
      solutionOrientations = mapOf(GridPos(1, 1) to MirrorOrientation.BACKSLASH),
      title = "Try It",
    )
  }

  var mirrorOrient by remember { mutableStateOf(MirrorOrientation.SLASH) }
  var visualAngle by remember { mutableFloatStateOf(0f) }
  val orientMap = mapOf(GridPos(1, 1) to mirrorOrient)
  val trace = remember(mirrorOrient) { TraceBeam.trace(sandboxLevel, orientMap) }

  PrismBackdropBox(
    modifier = Modifier.fillMaxSize(),
    radialSpotlight = true,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .safeDrawingPadding()
          .widthIn(max = 540.dp)
          .align(Alignment.TopCenter)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = PrismTokens.Spacing.md, vertical = PrismTokens.Spacing.xs),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (!isFirstLaunch) {
          IconButton(
            onClick = onDone,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("tutorial_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = "Back",
              tint = PrismTokens.Colors.TextPrimary,
            )
          }
          Spacer(Modifier.width(PrismTokens.Spacing.xs))
        }
        Text(
          text = if (isFirstLaunch) "Welcome to Prism Path" else "How to Play",
          style = MaterialTheme.typography.headlineMedium,
          color = PrismTokens.Colors.TextPrimary,
        )
      }

      Spacer(Modifier.height(PrismTokens.Spacing.md))

      GlassCard(
        border =
          if (trace.isSolved)
            androidx.compose.foundation.BorderStroke(1.5.dp, PrismTokens.Colors.Success)
          else PrismTokens.Borders.glass,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(PrismTokens.Spacing.md),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (trace.isSolved) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = PrismTokens.Colors.Success,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(6.dp))
            }
            Text(
              text = if (trace.isSolved) "Crystal lit!" else "Tap the mirror to aim the beam",
              style = MaterialTheme.typography.titleMedium,
              color =
                if (trace.isSolved) PrismTokens.Colors.Success else PrismTokens.Colors.Amber,
            )
          }
          Spacer(Modifier.height(PrismTokens.Spacing.sm))
          GameBoard(
            level = sandboxLevel,
            orientations = orientMap,
            visualAngles = mapOf(GridPos(1, 1) to visualAngle),
            traceResult = trace,
            activeHintCell = if (!trace.isSolved) GridPos(1, 1) else null,
            onCellTap = { pos ->
              if (pos == GridPos(1, 1)) {
                mirrorOrient = mirrorOrient.toggled()
                visualAngle += 90f
              }
            },
            modifier = Modifier.size(188.dp),
          )
        }
      }

      Spacer(Modifier.height(PrismTokens.Spacing.md))

      GuideRow(
        icon = Icons.Rounded.AutoAwesome,
        accent = PrismTokens.Colors.Amber,
        title = "Mirrors & Crystals",
        desc = "Tap glass mirrors to rotate 90°. Light every diamond target.",
      )
      GuideRow(
        icon = Icons.Rounded.Lock,
        accent = PrismTokens.Colors.Success,
        title = "Fixed Mirrors (Lv 11+)",
        desc = "Metallic mirrors with a lock dot stay fixed in place.",
      )
      GuideRow(
        icon = Icons.AutoMirrored.Rounded.CallSplit,
        accent = PrismTokens.Colors.BeamBlue,
        title = "Splitters (Lv 16+)",
        desc = "Splits light straight ahead and 90° to the side.",
      )
      GuideRow(
        icon = Icons.Rounded.Palette,
        accent = PrismTokens.Colors.BeamRed,
        title = "Color Filters (Lv 26+)",
        desc = "Colored crystals only light when hit by a matching beam.",
      )
      GuideRow(
        icon = Icons.Rounded.LensBlur,
        accent = PrismTokens.Colors.PortalPrimary,
        title = "Portals (Lv 41+)",
        desc = "Beams warp between matching portals in the same direction.",
      )

      Spacer(Modifier.height(PrismTokens.Spacing.lg))

      PrismPrimaryButton(
        onClick = onDone,
        minHeight = 54.dp,
        modifier = Modifier.fillMaxWidth().testTag("tutorial_done_button"),
      ) {
        Text(
          text = if (isFirstLaunch) "Start Playing" else "Got It",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(Modifier.height(PrismTokens.Spacing.md))
    }
  }
}

@Composable
private fun GuideRow(
  icon: ImageVector,
  accent: Color,
  title: String,
  desc: String,
) {
  GlassCard(
    modifier = Modifier.fillMaxWidth().padding(vertical = PrismTokens.Spacing.xxs),
  ) {
    Row(
      modifier = Modifier.padding(PrismTokens.Spacing.sm),
      horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accent,
        modifier = Modifier.size(22.dp),
      )
      Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = PrismTokens.Colors.TextPrimary)
        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = PrismTokens.Colors.TextSecondary)
      }
    }
  }
}
