package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PlayerProgress
import com.example.ui.components.PrismBackdropBox
import com.example.ui.components.PrismGlassButton
import com.example.ui.components.PrismPrimaryButton
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.rememberReducedMotion
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun HomeScreen(
  progress: PlayerProgress,
  onPlay: () -> Unit,
  onLevels: () -> Unit,
  onHowToPlay: () -> Unit,
  onSettings: () -> Unit,
) {
  val reducedMotion = rememberReducedMotion()
  val transition = rememberInfiniteTransition(label = "home_ambient")
  val drift by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = if (reducedMotion) 0f else (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(9000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "ambient_drift",
    )

  val scrollState = rememberScrollState()

  PrismBackdropBox(
    modifier = Modifier.fillMaxSize(),
    radialSpotlight = true,
  ) {
    // Slow ambient background beam animation
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val dy = sin(drift) * h * 0.04f

      // Subtle diagonal amber ambient ray
      drawLine(
        color = PrismTokens.Colors.Amber.copy(alpha = 0.055f),
        start = Offset(-w * 0.1f, h * 0.24f + dy),
        end = Offset(w * 1.1f, h * 0.14f - dy),
        strokeWidth = 28.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = PrismTokens.Colors.Amber.copy(alpha = 0.11f),
        start = Offset(-w * 0.1f, h * 0.24f + dy),
        end = Offset(w * 1.1f, h * 0.14f - dy),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )

      // Subtle lower blue ambient ray
      drawLine(
        color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.04f),
        start = Offset(-w * 0.1f, h * 0.78f - dy),
        end = Offset(w * 1.1f, h * 0.88f + dy),
        strokeWidth = 22.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.08f),
        start = Offset(-w * 0.1f, h * 0.78f - dy),
        end = Offset(w * 1.1f, h * 0.88f + dy),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    Box(
      modifier =
        Modifier.fillMaxSize()
          .safeDrawingPadding()
          .padding(horizontal = PrismTokens.Spacing.lg, vertical = PrismTokens.Spacing.md),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 460.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        AnimatedPrismLogo(modifier = Modifier.size(164.dp))
        Spacer(Modifier.height(PrismTokens.Spacing.lg))

        Text(
          text = "PRISM PATH",
          style = MaterialTheme.typography.displaySmall,
          color = PrismTokens.Colors.TextPrimary,
          letterSpacing = 3.sp,
          textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PrismTokens.Spacing.xs))
        Text(
          text = "Refract light. Illuminate every crystal.",
          style = MaterialTheme.typography.bodyLarge,
          color = PrismTokens.Colors.TextSecondary,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(PrismTokens.Spacing.md))

        Surface(
          shape = PrismTokens.Radii.pillShape,
          color = PrismTokens.Colors.GlassSurface,
          border = PrismTokens.Borders.glass,
        ) {
          Row(
            modifier =
              Modifier.padding(
                horizontal = PrismTokens.Spacing.md,
                vertical = PrismTokens.Spacing.xs,
              ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.xs),
          ) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = null,
              tint = PrismTokens.Colors.Amber,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "${progress.totalStars} / 150  ·  Level ${progress.unlockedLevel} / 50",
              style = TabularNumberStyle.copy(fontSize = 13.sp),
              color = PrismTokens.Colors.TextPrimary,
            )
          }
        }

        Spacer(Modifier.height(PrismTokens.Spacing.xl))

        val playLabel =
          if (progress.unlockedLevel > 1) "Continue · Level ${progress.unlockedLevel}"
          else "Play"

        PrismPrimaryButton(
          onClick = onPlay,
          minHeight = 58.dp,
          modifier = Modifier.fillMaxWidth().testTag("play_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
          )
          Spacer(Modifier.width(PrismTokens.Spacing.xs))
          Text(
            text = playLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }

        Spacer(Modifier.height(PrismTokens.Spacing.sm))

        PrismGlassButton(
          onClick = onLevels,
          minHeight = 50.dp,
          modifier = Modifier.fillMaxWidth().testTag("levels_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.GridView,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(Modifier.width(PrismTokens.Spacing.xs))
          Text("Levels", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(PrismTokens.Spacing.sm))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
        ) {
          PrismGlassButton(
            onClick = onHowToPlay,
            minHeight = 50.dp,
            modifier = Modifier.weight(1f).testTag("how_to_play_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
              contentDescription = null,
              tint = PrismTokens.Colors.BeamBlue,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(PrismTokens.Spacing.xs))
            Text("How to Play", style = MaterialTheme.typography.labelLarge)
          }

          PrismGlassButton(
            onClick = onSettings,
            minHeight = 50.dp,
            modifier = Modifier.weight(1f).testTag("settings_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = null,
              tint = PrismTokens.Colors.TextSecondary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(PrismTokens.Spacing.xs))
            Text("Settings", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}

@Composable
private fun AnimatedPrismLogo(modifier: Modifier = Modifier) {
  val reducedMotion = rememberReducedMotion()
  val transition = rememberInfiniteTransition(label = "logo_anim")
  val glow by
    transition.animateFloat(
      initialValue = if (reducedMotion) 1f else 0.82f,
      targetValue = if (reducedMotion) 1f else 1.14f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1700, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "logo_glow",
    )

  Canvas(
    modifier =
      modifier.semantics {
        contentDescription = "Prism Path animated prism logo"
      }
  ) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h * 0.52f

    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(PrismTokens.Colors.Amber.copy(alpha = 0.22f * glow), Color.Transparent),
          center = Offset(cx, cy),
          radius = w * 0.48f,
        ),
      radius = w * 0.48f,
      center = Offset(cx, cy),
    )

    // Incoming amber beam
    val entry = Offset(cx - w * 0.14f, cy + h * 0.04f)
    val start = Offset(w * 0.04f, cy + h * 0.14f)
    drawLine(
      color = PrismTokens.Colors.Amber.copy(alpha = 0.30f * glow),
      start = start,
      end = entry,
      strokeWidth = 14f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = PrismTokens.Colors.Amber,
      start = start,
      end = entry,
      strokeWidth = 6f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color.White,
      start = start,
      end = entry,
      strokeWidth = 2f,
      cap = StrokeCap.Round,
    )

    // Refracted rays: Blue, Green, Amber, Red
    val exit = Offset(cx + w * 0.14f, cy + h * 0.04f)
    val rays =
      listOf(
        Triple(PrismTokens.Colors.BeamBlue, Offset(w * 0.96f, cy - h * 0.11f), 4.5f),
        Triple(PrismTokens.Colors.BeamGreen, Offset(w * 0.95f, cy - h * 0.01f), 4.0f),
        Triple(PrismTokens.Colors.Amber, Offset(w * 0.96f, cy + h * 0.09f), 4.5f),
        Triple(PrismTokens.Colors.BeamRed, Offset(w * 0.96f, cy + h * 0.21f), 4.5f),
      )
    for ((rayColor, target, strokeW) in rays) {
      drawLine(
        color = rayColor.copy(alpha = 0.28f * glow),
        start = exit,
        end = target,
        strokeWidth = strokeW * 2.6f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = rayColor,
        start = exit,
        end = target,
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
      )
    }

    // Realistic faceted triangular glass prism
    val top = Offset(cx, cy - h * 0.30f)
    val left = Offset(cx - w * 0.28f, cy + h * 0.22f)
    val right = Offset(cx + w * 0.28f, cy + h * 0.22f)
    val innerApex = Offset(cx, cy + h * 0.03f)

    val prism =
      Path().apply {
        moveTo(top.x, top.y)
        lineTo(right.x, right.y)
        lineTo(left.x, left.y)
        close()
      }
    val leftFacet =
      Path().apply {
        moveTo(top.x, top.y)
        lineTo(innerApex.x, innerApex.y)
        lineTo(left.x, left.y)
        close()
      }
    val rightFacet =
      Path().apply {
        moveTo(top.x, top.y)
        lineTo(right.x, right.y)
        lineTo(innerApex.x, innerApex.y)
        close()
      }

    // Soft glass body fill
    drawPath(
      prism,
      brush =
        Brush.linearGradient(
          colors =
            listOf(
              Color(0xFF28354E).copy(alpha = 0.85f),
              PrismTokens.Colors.Background.copy(alpha = 0.88f),
            ),
          start = top,
          end = right,
        ),
    )
    // Internal glass facet sheen
    drawPath(leftFacet, color = Color.White.copy(alpha = 0.09f))
    drawPath(rightFacet, color = Color.Black.copy(alpha = 0.20f))

    // Internal dispersed light bridge
    drawLine(
      brush =
        Brush.horizontalGradient(
          colors = listOf(PrismTokens.Colors.AmberBright, Color.White, Color(0xFFBAE6FD)),
          startX = entry.x,
          endX = exit.x,
        ),
      start = entry,
      end = exit,
      strokeWidth = 5f,
      cap = StrokeCap.Round,
    )

    // Crisp beveled prism rim
    drawPath(
      prism,
      brush =
        Brush.linearGradient(
          colors =
            listOf(
              Color.White.copy(alpha = 0.92f),
              Color.White.copy(alpha = 0.45f),
            ),
          start = top,
          end = right,
        ),
      style = Stroke(width = 4.2f),
    )
    // Specular edge glint on upper-left face
    drawLine(
      color = Color.White.copy(alpha = 0.95f),
      start = top,
      end = Offset((top.x + left.x) / 2f, (top.y + left.y) / 2f),
      strokeWidth = 5.5f,
      cap = StrokeCap.Round,
    )
  }
}
