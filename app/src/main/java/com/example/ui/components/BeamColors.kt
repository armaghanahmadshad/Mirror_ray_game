package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.engine.BeamColor
import com.example.ui.theme.PrismTokens

fun BeamColor.toComposeColor(): Color =
  when (this) {
    BeamColor.AMBER -> PrismTokens.Colors.BeamAmber
    BeamColor.CYAN -> PrismTokens.Colors.BeamBlue
    BeamColor.MAGENTA -> PrismTokens.Colors.BeamRed
    BeamColor.EMERALD -> PrismTokens.Colors.BeamGreen
  }

fun BeamColor.displayName(): String =
  when (this) {
    BeamColor.AMBER -> "Amber"
    BeamColor.CYAN -> "Blue"
    BeamColor.MAGENTA -> "Red"
    BeamColor.EMERALD -> "Green"
  }
