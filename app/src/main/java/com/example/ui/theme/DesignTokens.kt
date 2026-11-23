package com.example.ui.theme

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Central design tokens for Prism Path:
 * "Light and glass in a dark room" — calm, focused, minimal.
 */
object PrismTokens {
  object Colors {
    val Background = Color(0xFF0F172A)
    val BackgroundDeep = Color(0xFF090E1A)
    val BoardRadialCenter = Color(0xFF1E293B)

    val Amber = Color(0xFFF59E0B)
    val AmberGlow = Color(0x59F59E0B) // rgba(245, 158, 11, 0.35)
    val AmberSubtle = Color(0x22F59E0B)
    val AmberBright = Color(0xFFFDE68A)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)

    val GlassSurface = Color(0x0AFFFFFF) // rgba(255, 255, 255, 0.04)
    val GlassSurfaceElevated = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
    val GlassBorder = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
    val CellBorder = Color(0x0FFFFFFF) // rgba(255, 255, 255, 0.06)

    val Success = Color(0xFF34D399)
    val Error = Color(0xFFF87171)

    // Spectrum beam colors for advanced levels
    val BeamAmber = Color(0xFFF59E0B)
    val BeamRed = Color(0xFFF87171)
    val BeamGreen = Color(0xFF34D399)
    val BeamBlue = Color(0xFF60A5FA)

    val PortalPrimary = Color(0xFFA78BFA)
    val PortalSecondary = Color(0xFF38BDF8)

    val StarEmpty = Color(0xFF334155)
  }

  object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
  }

  object Radii {
    val cell = 8.dp
    val card = 12.dp
    val modal = 16.dp
    val pill = 999.dp

    val cardShape = RoundedCornerShape(card)
    val modalShape = RoundedCornerShape(modal)
    val pillShape = RoundedCornerShape(pill)
  }

  object Borders {
    val glass = BorderStroke(1.dp, Colors.GlassBorder)
    val amberGlow = BorderStroke(1.5.dp, Colors.Amber)
  }

  object Motion {
    const val ScreenTransitionMs = 220
    const val BeamDrawMs = 250
    const val MirrorRotateMs = 180
    const val PressScale = 0.96f
  }
}

@Composable
fun rememberReducedMotion(): Boolean {
  val context = LocalContext.current
  return remember(context) { isSystemReducedMotion(context) }
}

private fun isSystemReducedMotion(context: Context): Boolean =
  try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
      !android.animation.ValueAnimator.areAnimatorsEnabled()
    ) {
      true
    } else {
      Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
      ) == 0f
    }
  } catch (_: Throwable) {
    false
  }

fun Modifier.pressScale(
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
): Modifier = composed {
  val pressed by interactionSource.collectIsPressedAsState()
  val reducedMotion = rememberReducedMotion()
  val scale by
    animateFloatAsState(
      targetValue = if (pressed && enabled && !reducedMotion) PrismTokens.Motion.PressScale else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessHigh),
      label = "press_scale",
    )
  this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}
