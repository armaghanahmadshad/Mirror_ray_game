package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LevelDef
import com.example.engine.PlayerProgress
import com.example.levels.Levels
import com.example.ui.components.PrismBackdropBox
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.pressScale

private data class ChapterGroup(
  val title: String,
  val levels: List<LevelDef>,
)

@Composable
fun LevelSelectScreen(
  progress: PlayerProgress,
  onSelectLevel: (Int) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val chapters =
    listOf(
      ChapterGroup("I · Mirrors & Walls", Levels.all.subList(0, 10)),
      ChapterGroup("II · Fixed Mirrors", Levels.all.subList(10, 15)),
      ChapterGroup("III · Beam Splitters", Levels.all.subList(15, 25)),
      ChapterGroup("IV · Color Filters", Levels.all.subList(25, 40)),
      ChapterGroup("V · Portals", Levels.all.subList(40, 50)),
    )

  PrismBackdropBox(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .safeDrawingPadding()
          .widthIn(max = 600.dp)
          .align(Alignment.TopCenter),
    ) {
      // Sticky Header
      Surface(
        color = PrismTokens.Colors.Background.copy(alpha = 0.92f),
        border = BorderStroke(0.dp, Color.Transparent),
      ) {
        Column {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .padding(
                  horizontal = PrismTokens.Spacing.sm,
                  vertical = PrismTokens.Spacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("levels_back_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = PrismTokens.Colors.TextPrimary,
              )
            }
            Spacer(Modifier.width(PrismTokens.Spacing.xs))
            Text(
              text = "Levels",
              style = MaterialTheme.typography.headlineMedium,
              color = PrismTokens.Colors.TextPrimary,
              modifier = Modifier.weight(1f),
            )
            Surface(
              shape = PrismTokens.Radii.pillShape,
              color = PrismTokens.Colors.GlassSurface,
              border = PrismTokens.Borders.glass,
            ) {
              Row(
                modifier =
                  Modifier.padding(
                    horizontal = PrismTokens.Spacing.sm,
                    vertical = PrismTokens.Spacing.xxs,
                  ),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.Star,
                  contentDescription = null,
                  tint = PrismTokens.Colors.Amber,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(PrismTokens.Spacing.xxs))
                Text(
                  text = "${progress.totalStars} / 150",
                  style = TabularNumberStyle.copy(fontSize = 13.sp),
                  color = PrismTokens.Colors.TextPrimary,
                )
              }
            }
            Spacer(Modifier.width(PrismTokens.Spacing.xs))
          }
          HorizontalDivider(color = PrismTokens.Colors.GlassBorder, thickness = 1.dp)
        }
      }

      // 4-Column Level Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding =
          PaddingValues(
            horizontal = PrismTokens.Spacing.md,
            vertical = PrismTokens.Spacing.sm,
          ),
        horizontalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(PrismTokens.Spacing.sm),
        modifier = Modifier.fillMaxSize().testTag("level_grid"),
      ) {
        for (chapter in chapters) {
          item(span = { GridItemSpan(4) }) {
            Text(
              text = chapter.title.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = PrismTokens.Colors.TextSecondary,
              modifier =
                Modifier.padding(
                  top = PrismTokens.Spacing.sm,
                  bottom = PrismTokens.Spacing.xxs,
                ),
            )
          }

          items(chapter.levels, key = { it.id }) { level ->
            val unlocked = level.id <= progress.unlockedLevel
            val stars = progress.starsByLevel[level.id] ?: 0
            val isCurrent = level.id == progress.unlockedLevel

            LevelTile(
              levelId = level.id,
              unlocked = unlocked,
              isCurrent = isCurrent,
              stars = stars,
              onClick = { if (unlocked) onSelectLevel(level.id) },
            )
          }
        }
        item(span = { GridItemSpan(4) }) { Spacer(Modifier.height(PrismTokens.Spacing.md)) }
      }
    }
  }
}

@Composable
private fun LevelTile(
  levelId: Int,
  unlocked: Boolean,
  isCurrent: Boolean,
  stars: Int,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val bg =
    when {
      isCurrent -> PrismTokens.Colors.AmberSubtle
      unlocked -> PrismTokens.Colors.GlassSurface
      else -> Color.White.copy(alpha = 0.02f)
    }
  val border =
    when {
      isCurrent ->
        BorderStroke(
          1.25.dp,
          Brush.verticalGradient(
            listOf(
              PrismTokens.Colors.AmberBright.copy(alpha = 0.85f),
              PrismTokens.Colors.Amber.copy(alpha = 0.35f),
            ),
          ),
        )
      unlocked ->
        BorderStroke(
          1.dp,
          Brush.verticalGradient(
            listOf(
              Color.White.copy(alpha = 0.16f),
              Color.White.copy(alpha = 0.05f),
            ),
          ),
        )
      else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    }

  Surface(
    shape = PrismTokens.Radii.cardShape,
    color = bg,
    border = border,
    shadowElevation = if (unlocked) 2.dp else 0.dp,
    modifier =
      Modifier.aspectRatio(1f)
        .minimumInteractiveComponentSize()
        .pressScale(interactionSource, enabled = unlocked)
        .drawBehind {
          if (isCurrent) {
            drawRoundRect(
              brush =
                Brush.radialGradient(
                  listOf(
                    PrismTokens.Colors.AmberGlow.copy(alpha = 0.32f),
                    Color.Transparent,
                  ),
                ),
              cornerRadius = CornerRadius(PrismTokens.Radii.card.toPx()),
            )
          }
        }
        .testTag("level_tile_$levelId")
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          enabled = unlocked,
          onClick = onClick,
        ),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              if (unlocked) {
                listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
              } else {
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f))
              },
            ),
          ),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(PrismTokens.Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
      if (unlocked) {
        Text(
          text = levelId.toString(),
          style = TabularNumberStyle.copy(fontSize = 18.sp),
          color = if (isCurrent) PrismTokens.Colors.Amber else PrismTokens.Colors.TextPrimary,
        )
        Spacer(Modifier.height(PrismTokens.Spacing.xxs))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = null,
              tint = if (i <= stars) PrismTokens.Colors.Amber else PrismTokens.Colors.StarEmpty,
              modifier = Modifier.size(12.dp),
            )
          }
        }
      } else {
        Icon(
          imageVector = Icons.Rounded.Lock,
          contentDescription = "Level $levelId locked",
          tint = PrismTokens.Colors.TextSecondary.copy(alpha = 0.35f),
          modifier = Modifier.size(18.dp),
        )
      }
      }
    }
  }
}
