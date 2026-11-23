package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.pressScale

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = PrismTokens.Radii.cardShape,
  border: BorderStroke =
    BorderStroke(
      width = 1.dp,
      brush =
        Brush.verticalGradient(
          colors =
            listOf(
              Color.White.copy(alpha = 0.16f),
              Color.White.copy(alpha = 0.05f),
            ),
        ),
    ),
  backgroundColor: Color = PrismTokens.Colors.GlassSurface,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = shape,
    color = backgroundColor,
    border = border,
    shadowElevation = 2.dp,
  ) {
    Box(
      modifier =
        Modifier.background(
          Brush.verticalGradient(
            colors =
              listOf(
                Color.White.copy(alpha = 0.045f),
                Color.Transparent,
              ),
          )
        ),
    ) {
      content()
    }
  }
}

@Composable
fun PrismPrimaryButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = PrismTokens.Radii.pillShape,
  minHeight: Dp = 54.dp,
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    shape = shape,
    contentPadding = contentPadding,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = PrismTokens.Colors.Background,
        disabledContainerColor = PrismTokens.Colors.GlassSurface,
        disabledContentColor = PrismTokens.Colors.TextSecondary,
      ),
    modifier =
      modifier
        .heightIn(min = minHeight)
        .pressScale(interactionSource, enabled)
        .drawBehind {
          if (enabled) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            // Soft outer amber bloom
            drawRoundRect(
              brush =
                Brush.radialGradient(
                  colors =
                    listOf(
                      PrismTokens.Colors.AmberGlow,
                      Color.Transparent,
                    ),
                ),
              cornerRadius = r,
            )
            // Tactile metallic amber fill
            drawRoundRect(
              brush =
                Brush.verticalGradient(
                  colors =
                    listOf(
                      Color(0xFFFBBF24),
                      PrismTokens.Colors.Amber,
                      Color(0xFFD97706),
                    ),
                ),
              cornerRadius = r,
            )
            // Subtle top specular highlight rim
            drawRoundRect(
              brush =
                Brush.verticalGradient(
                  colors =
                    listOf(
                      Color(0xFFFEF3C7).copy(alpha = 0.7f),
                      Color.Transparent,
                    ),
                ),
              cornerRadius = r,
              style = Stroke(width = 1.2.dp.toPx()),
            )
          }
        },
    content = content,
  )
}

@Composable
fun PrismGlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = PrismTokens.Radii.cardShape,
  minHeight: Dp = 48.dp,
  borderColor: Color = PrismTokens.Colors.GlassBorder,
  contentColor: Color = PrismTokens.Colors.TextPrimary,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    shape = shape,
    contentPadding = contentPadding,
    border =
      BorderStroke(
        width = 1.dp,
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                if (enabled) borderColor.copy(alpha = (borderColor.alpha * 1.9f).coerceAtMost(0.75f))
                else borderColor.copy(alpha = 0.25f),
                if (enabled) borderColor else borderColor.copy(alpha = 0.2f),
              ),
          ),
      ),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = PrismTokens.Colors.GlassSurface,
        contentColor = contentColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = PrismTokens.Colors.TextSecondary.copy(alpha = 0.45f),
      ),
    modifier = modifier.heightIn(min = minHeight).pressScale(interactionSource, enabled),
    content = content,
  )
}

@Composable
fun PrismBackdropBox(
  modifier: Modifier = Modifier,
  radialSpotlight: Boolean = false,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier =
      modifier
        .background(PrismTokens.Colors.BackgroundDeep)
        .drawBehind {
          drawRect(
            brush =
              Brush.verticalGradient(
                colors =
                  listOf(
                    PrismTokens.Colors.Background,
                    PrismTokens.Colors.BackgroundDeep,
                  ),
              ),
          )
          if (radialSpotlight) {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors =
                    listOf(
                      PrismTokens.Colors.BoardRadialCenter.copy(alpha = 0.92f),
                      PrismTokens.Colors.Background.copy(alpha = 0.45f),
                      Color.Transparent,
                    ),
                  center = Offset(size.width * 0.5f, size.height * 0.44f),
                  radius = size.maxDimension * 0.58f,
                ),
              center = Offset(size.width * 0.5f, size.height * 0.44f),
              radius = size.maxDimension * 0.58f,
            )
          }
        },
    content = content,
  )
}

@Composable
fun SectionDividerLabel(
  text: String,
  modifier: Modifier = Modifier,
) {
  androidx.compose.material3.Text(
    text = text.uppercase(),
    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
    color = PrismTokens.Colors.TextSecondary,
    modifier = modifier.fillMaxWidth().padding(vertical = PrismTokens.Spacing.xs),
  )
}

val ModalCornerShape = RoundedCornerShape(PrismTokens.Radii.modal)
