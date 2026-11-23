package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.AmbientTheme
import com.example.engine.AppSettings
import com.example.ui.components.GlassCard
import com.example.ui.components.PrismBackdropBox
import com.example.ui.components.PrismGlassButton
import com.example.ui.components.ResetProgressConfirmDialog
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.pressScale

@Composable
fun SettingsScreen(
  settings: AppSettings,
  onSoundChanged: (Boolean) -> Unit,
  onVibrationChanged: (Boolean) -> Unit,
  onAmbientThemeChanged: (AmbientTheme) -> Unit,
  onResetProgress: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)
  var showConfirmReset by remember { mutableStateOf(false) }

  PrismBackdropBox(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .safeDrawingPadding()
          .widthIn(max = 520.dp)
          .align(Alignment.TopCenter)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = PrismTokens.Spacing.md),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = PrismTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = PrismTokens.Colors.TextPrimary,
          )
        }
        Spacer(Modifier.width(PrismTokens.Spacing.xs))
        Text(
          text = "Settings",
          style = MaterialTheme.typography.headlineMedium,
          color = PrismTokens.Colors.TextPrimary,
        )
      }

      Spacer(Modifier.height(PrismTokens.Spacing.md))

      SettingToggleCard(
        icon = Icons.AutoMirrored.Rounded.VolumeUp,
        title = "Sound Effects",
        subtitle = "Mirror clicks and crystal chimes",
        checked = settings.soundEnabled,
        onCheckedChange = onSoundChanged,
        testTag = "sound_toggle",
      )

      Spacer(Modifier.height(PrismTokens.Spacing.sm))

      SettingToggleCard(
        icon = Icons.Rounded.Vibration,
        title = "Haptic Feedback",
        subtitle = "Tactile tap and solve vibration",
        checked = settings.vibrationEnabled,
        onCheckedChange = onVibrationChanged,
        testTag = "vibration_toggle",
      )

      Spacer(Modifier.height(PrismTokens.Spacing.md))

      // Ambient Background Audio Theme Selector
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PrismTokens.Spacing.md)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
          ) {
            Icon(
              imageVector = Icons.Rounded.GraphicEq,
              contentDescription = null,
              tint = PrismTokens.Colors.Amber,
              modifier = Modifier.size(22.dp),
            )
            Column {
              Text(
                text = "Ambient Audio",
                style = MaterialTheme.typography.titleMedium,
                color = PrismTokens.Colors.TextPrimary,
              )
              Text(
                text = "Subtle generative soundscapes",
                style = MaterialTheme.typography.bodyMedium,
                color = PrismTokens.Colors.TextSecondary,
              )
            }
          }

          Spacer(Modifier.height(PrismTokens.Spacing.sm))

          Column(verticalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.xs)) {
            for (theme in AmbientTheme.entries) {
              AmbientThemeOptionRow(
                theme = theme,
                selected = settings.ambientTheme == theme,
                onClick = { onAmbientThemeChanged(theme) },
              )
            }
          }
        }
      }

      Spacer(Modifier.height(PrismTokens.Spacing.md))

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(PrismTokens.Spacing.md)) {
          Text(
            text = "Progress Data",
            style = MaterialTheme.typography.titleMedium,
            color = PrismTokens.Colors.TextPrimary,
          )
          Spacer(Modifier.height(PrismTokens.Spacing.xxs))
          Text(
            text = "Saved offline on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = PrismTokens.Colors.TextSecondary,
          )
          Spacer(Modifier.height(PrismTokens.Spacing.md))
          PrismGlassButton(
            onClick = { showConfirmReset = true },
            minHeight = 48.dp,
            borderColor = PrismTokens.Colors.Error.copy(alpha = 0.55f),
            contentColor = PrismTokens.Colors.Error,
            modifier = Modifier.fillMaxWidth().testTag("reset_progress_button"),
          ) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(PrismTokens.Spacing.xs))
            Text("Reset Progress", style = MaterialTheme.typography.labelLarge)
          }
        }
      }

      Spacer(Modifier.height(PrismTokens.Spacing.lg))
    }
  }

  if (showConfirmReset) {
    ResetProgressConfirmDialog(
      onConfirm = {
        onResetProgress()
        showConfirmReset = false
      },
      onDismiss = { showConfirmReset = false },
    )
  }
}

@Composable
private fun AmbientThemeOptionRow(
  theme: AmbientTheme,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val icon =
    when (theme) {
      AmbientTheme.OFF -> Icons.AutoMirrored.Rounded.VolumeOff
      AmbientTheme.GLASS_PRISM -> Icons.Rounded.AutoAwesome
      AmbientTheme.NIGHT_OBSERVATORY -> Icons.Rounded.DarkMode
      AmbientTheme.SOLAR_DRIFT -> Icons.Rounded.WbTwilight
    }

  Surface(
    shape = PrismTokens.Radii.cardShape,
    color = if (selected) PrismTokens.Colors.AmberSubtle else PrismTokens.Colors.GlassSurface,
    border =
      if (selected) PrismTokens.Borders.amberGlow
      else BorderStroke(1.dp, PrismTokens.Colors.GlassBorder),
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 52.dp)
        .pressScale(interactionSource)
        .testTag("ambient_theme_${theme.name.lowercase()}")
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
        ),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = PrismTokens.Spacing.sm, vertical = PrismTokens.Spacing.xs),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) PrismTokens.Colors.Amber else PrismTokens.Colors.TextSecondary,
        modifier = Modifier.size(20.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = theme.label,
          style = MaterialTheme.typography.labelLarge,
          color = if (selected) PrismTokens.Colors.Amber else PrismTokens.Colors.TextPrimary,
        )
        Text(
          text = theme.description,
          style = MaterialTheme.typography.bodyMedium,
          color = PrismTokens.Colors.TextSecondary,
        )
      }
      if (selected) {
        Icon(
          imageVector = Icons.Rounded.CheckCircle,
          contentDescription = "Selected",
          tint = PrismTokens.Colors.Amber,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun SettingToggleCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  GlassCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = PrismTokens.Spacing.md, vertical = PrismTokens.Spacing.sm),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = PrismTokens.Colors.Amber,
        modifier = Modifier.size(24.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = PrismTokens.Colors.TextPrimary)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = PrismTokens.Colors.TextSecondary)
      }
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.minimumInteractiveComponentSize().testTag(testTag),
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = PrismTokens.Colors.Background,
            checkedTrackColor = PrismTokens.Colors.Amber,
            uncheckedThumbColor = PrismTokens.Colors.TextSecondary,
            uncheckedTrackColor = PrismTokens.Colors.GlassSurfaceElevated,
          ),
      )
    }
  }
}
