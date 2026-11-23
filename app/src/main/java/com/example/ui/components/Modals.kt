package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LensBlur
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.Rules
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LevelCompleteDialog(
  levelId: Int,
  hasNextLevel: Boolean,
  movesUsed: Int,
  par: Int,
  elapsedSeconds: Int,
  bestTimeSeconds: Int?,
  isNewBestTime: Boolean,
  starsEarned: Int,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onHome: () -> Unit,
) {
  val reducedMotion = rememberReducedMotion()
  var revealedStars by remember(levelId, starsEarned) { mutableIntStateOf(if (reducedMotion) starsEarned else 0) }
  val starBurst = remember(levelId) { Animatable(0f) }

  LaunchedEffect(levelId, starsEarned, reducedMotion) {
    if (reducedMotion) {
      revealedStars = starsEarned
    } else {
      revealedStars = 0
      for (i in 1..starsEarned) {
        delay(170L)
        revealedStars = i
      }
      starBurst.snapTo(0f)
      starBurst.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
      )
    }
  }

  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.62f))
          .padding(PrismTokens.Spacing.lg),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        shape = PrismTokens.Radii.modalShape,
        color = PrismTokens.Colors.BoardRadialCenter.copy(alpha = 0.94f),
        border =
          BorderStroke(
            1.dp,
            Brush.verticalGradient(
              listOf(
                Color.White.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.06f),
              ),
            ),
          ),
        shadowElevation = 18.dp,
        modifier =
          Modifier.widthIn(max = 400.dp)
            .fillMaxWidth()
            .testTag("level_complete_modal"),
      ) {
        Box(
          modifier =
            Modifier.background(
              Brush.verticalGradient(
                listOf(
                  Color.White.copy(alpha = 0.06f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.14f),
                ),
              ),
            ),
        ) {
          Column(
            modifier = Modifier.padding(PrismTokens.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
          Text(
            text = "LEVEL $levelId",
            style = MaterialTheme.typography.labelSmall,
            color = PrismTokens.Colors.Amber,
          )
          Spacer(Modifier.height(PrismTokens.Spacing.xxs))
          Text(
            text = "Crystal Lit",
            style = MaterialTheme.typography.headlineMedium,
            color = PrismTokens.Colors.TextPrimary,
            textAlign = TextAlign.Center,
          )

          Spacer(Modifier.height(PrismTokens.Spacing.lg))

          // Animated stars with amber particle burst behind
          Box(contentAlignment = Alignment.Center, modifier = Modifier.height(64.dp).fillMaxWidth()) {
            val burst = starBurst.value
            if (burst > 0f && burst < 1f) {
              Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val alpha = (1f - burst).coerceIn(0f, 1f)
                for (i in 0 until 14) {
                  val ang = (i * 2.0 * PI / 14.0).toFloat()
                  val dist = burst * 90.dp.toPx() * (0.65f + (i % 3) * 0.2f)
                  drawCircle(
                    color =
                      (if (i % 2 == 0) PrismTokens.Colors.Amber else PrismTokens.Colors.AmberBright)
                        .copy(alpha = alpha),
                    radius = (3.5f - burst * 1.8f).dp.toPx(),
                    center = Offset(cx + cos(ang) * dist, cy + sin(ang) * dist),
                  )
                }
              }
            }

            Row(
              horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.md),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              for (idx in 1..3) {
                val active = idx <= revealedStars
                val scale by
                  animateFloatAsState(
                    targetValue = if (active) 1.15f else 0.90f,
                    animationSpec =
                      spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                      ),
                    label = "star_pop_$idx",
                  )
                Box(contentAlignment = Alignment.Center) {
                  if (active) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                      drawCircle(
                        brush =
                          Brush.radialGradient(
                            listOf(
                              PrismTokens.Colors.Amber.copy(alpha = 0.34f),
                              Color.Transparent,
                            ),
                          ),
                      )
                    }
                  }
                  Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = if (active) "Earned star $idx" else "Empty star $idx",
                    tint = if (active) PrismTokens.Colors.Amber else PrismTokens.Colors.StarEmpty,
                    modifier = Modifier.size(44.dp).scale(scale),
                  )
                }
              }
            }
          }

          Spacer(Modifier.height(PrismTokens.Spacing.md))

          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = PrismTokens.Spacing.sm, horizontal = PrismTokens.Spacing.md),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MOVES", style = MaterialTheme.typography.labelSmall, color = PrismTokens.Colors.TextSecondary)
                Text(
                  text = "$movesUsed / $par",
                  style = TabularNumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                  color = PrismTokens.Colors.TextPrimary,
                )
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TIME", style = MaterialTheme.typography.labelSmall, color = PrismTokens.Colors.TextSecondary)
                Text(
                  text = Rules.formatTime(elapsedSeconds),
                  style = TabularNumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                  color = PrismTokens.Colors.TextPrimary,
                )
              }
            }
          }

          if (isNewBestTime) {
            Spacer(Modifier.height(PrismTokens.Spacing.sm))
            Surface(
              shape = PrismTokens.Radii.pillShape,
              color = PrismTokens.Colors.AmberSubtle,
              border = BorderStroke(1.dp, PrismTokens.Colors.Amber.copy(alpha = 0.55f)),
            ) {
              Text(
                text = "New Best Time",
                style = MaterialTheme.typography.labelLarge,
                color = PrismTokens.Colors.Amber,
                modifier = Modifier.padding(horizontal = PrismTokens.Spacing.sm, vertical = PrismTokens.Spacing.xxs),
              )
            }
          } else if (bestTimeSeconds != null) {
            Spacer(Modifier.height(PrismTokens.Spacing.xs))
            Text(
              text = "Best: ${Rules.formatTime(bestTimeSeconds)}",
              style = MaterialTheme.typography.bodyMedium,
              color = PrismTokens.Colors.TextSecondary,
            )
          }

          Spacer(Modifier.height(PrismTokens.Spacing.lg))

          if (hasNextLevel) {
            PrismPrimaryButton(
              onClick = onNextLevel,
              shape = PrismTokens.Radii.cardShape,
              modifier = Modifier.fillMaxWidth().testTag("next_level_button"),
            ) {
              Text(
                text = "Next Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Spacer(Modifier.width(PrismTokens.Spacing.xs))
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
              )
            }
            Spacer(Modifier.height(PrismTokens.Spacing.sm))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
          ) {
            PrismGlassButton(
              onClick = onReplay,
              modifier = Modifier.weight(1f).testTag("replay_button"),
            ) {
              Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Replay")
            }
            PrismGlassButton(
              onClick = onHome,
              modifier = Modifier.weight(1f).testTag("modal_home_button"),
            ) {
              Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Home")
            }
          }
          }
        }
      }
    }
  }
}

private data class MechanicTip(
  val title: String,
  val body: String,
  val icon: ImageVector,
  val accent: Color,
)

private fun tipForLevel(levelId: Int): MechanicTip =
  when (levelId) {
    1 ->
      MechanicTip(
        title = "Rotate Mirrors",
        body = "Tap any glass mirror to turn it 90°. Guide the amber beam to light the crystal.",
        icon = Icons.Rounded.AutoAwesome,
        accent = PrismTokens.Colors.Amber,
      )
    6 ->
      MechanicTip(
        title = "Stone Walls",
        body = "Solid slate blocks stop the beam. Route around them.",
        icon = Icons.Rounded.Shield,
        accent = PrismTokens.Colors.TextSecondary,
      )
    11 ->
      MechanicTip(
        title = "Fixed Mirrors",
        body = "Metallic mirrors with a lock dot stay fixed in place.",
        icon = Icons.Rounded.Lock,
        accent = PrismTokens.Colors.Success,
      )
    16 ->
      MechanicTip(
        title = "Beam Splitters",
        body = "Half passes straight through, half reflects 90°. Tap to flip.",
        icon = Icons.AutoMirrored.Rounded.CallSplit,
        accent = PrismTokens.Colors.BeamBlue,
      )
    26 ->
      MechanicTip(
        title = "Color Filters",
        body = "Filters tint the beam. Colored crystals require a matching beam.",
        icon = Icons.Rounded.Palette,
        accent = PrismTokens.Colors.BeamRed,
      )
    else ->
      MechanicTip(
        title = "Portal Pairs",
        body = "Beams warp between matching portals in the same direction.",
        icon = Icons.Rounded.LensBlur,
        accent = PrismTokens.Colors.PortalPrimary,
      )
  }

@Composable
fun MechanicTutorialDialog(
  levelId: Int,
  onDismiss: () -> Unit,
) {
  val tip = tipForLevel(levelId)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.56f))
          .padding(PrismTokens.Spacing.lg),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        shape = PrismTokens.Radii.modalShape,
        color = PrismTokens.Colors.BoardRadialCenter.copy(alpha = 0.95f),
        border =
          BorderStroke(
            1.dp,
            Brush.verticalGradient(
              listOf(
                tip.accent.copy(alpha = 0.65f),
                tip.accent.copy(alpha = 0.18f),
              ),
            ),
          ),
        shadowElevation = 16.dp,
        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth().testTag("mechanic_tip_dialog"),
      ) {
        Box(
          modifier =
            Modifier.background(
              Brush.verticalGradient(
                listOf(
                  tip.accent.copy(alpha = 0.08f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.12f),
                ),
              ),
            ),
        ) {
          Column(
            modifier = Modifier.padding(PrismTokens.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
          Surface(
            shape = PrismTokens.Radii.cardShape,
            color = tip.accent.copy(alpha = 0.16f),
            modifier = Modifier.size(52.dp),
          ) {
            Icon(
              imageVector = tip.icon,
              contentDescription = null,
              tint = tip.accent,
              modifier = Modifier.padding(13.dp),
            )
          }
          Spacer(Modifier.height(PrismTokens.Spacing.md))
          Text(
            text = tip.title,
            style = MaterialTheme.typography.headlineMedium,
            color = PrismTokens.Colors.TextPrimary,
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.height(PrismTokens.Spacing.xs))
          Text(
            text = tip.body,
            style = MaterialTheme.typography.bodyLarge,
            color = PrismTokens.Colors.TextSecondary,
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.height(PrismTokens.Spacing.lg))
          PrismPrimaryButton(
            onClick = onDismiss,
            shape = PrismTokens.Radii.cardShape,
            minHeight = 48.dp,
            modifier = Modifier.fillMaxWidth().testTag("dismiss_tip_button"),
          ) {
            Text("Got It", fontWeight = FontWeight.Bold)
          }
        }
        }
      }
    }
  }
}

@Composable
fun ResetProgressConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.60f))
          .padding(PrismTokens.Spacing.lg),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        shape = PrismTokens.Radii.modalShape,
        color = PrismTokens.Colors.BoardRadialCenter.copy(alpha = 0.96f),
        border =
          BorderStroke(
            1.dp,
            Brush.verticalGradient(
              listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.06f),
              ),
            ),
          ),
        shadowElevation = 16.dp,
        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth().testTag("reset_confirm_dialog"),
      ) {
        Box(
          modifier =
            Modifier.background(
              Brush.verticalGradient(
                listOf(
                  Color.White.copy(alpha = 0.05f),
                  Color.Transparent,
                ),
              ),
            ),
        ) {
          Column(modifier = Modifier.padding(PrismTokens.Spacing.lg)) {
            Text(
              text = "Reset All Progress?",
              style = MaterialTheme.typography.headlineMedium,
              color = PrismTokens.Colors.TextPrimary,
            )
            Spacer(Modifier.height(PrismTokens.Spacing.xs))
            Text(
              text = "Levels 2–50 will lock and all stars and best times will be cleared.",
              style = MaterialTheme.typography.bodyMedium,
              color = PrismTokens.Colors.TextSecondary,
            )
            Spacer(Modifier.height(PrismTokens.Spacing.lg))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
            ) {
              TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp).testTag("cancel_reset_button"),
              ) {
                Text("Cancel", color = PrismTokens.Colors.TextSecondary)
              }
              Spacer(Modifier.width(PrismTokens.Spacing.xs))
              Button(
                onClick = onConfirm,
                shape = PrismTokens.Radii.cardShape,
                modifier = Modifier.height(48.dp).testTag("confirm_reset_button"),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = PrismTokens.Colors.Error,
                    contentColor = PrismTokens.Colors.TextPrimary,
                  ),
              ) {
                Text("Reset", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
