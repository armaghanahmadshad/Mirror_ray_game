package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.engine.BeamColor
import com.example.engine.CellSpec
import com.example.engine.Direction
import com.example.engine.GridPos
import com.example.engine.LevelDef
import com.example.engine.MirrorOrientation
import com.example.engine.TraceResult
import com.example.ui.theme.PrismTokens
import com.example.ui.theme.rememberReducedMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Spark(
  val angleRad: Float,
  val speed: Float,
  val radius: Float,
  val color: Color,
)

@Composable
fun GameBoard(
  level: LevelDef,
  orientations: Map<GridPos, MirrorOrientation>,
  visualAngles: Map<GridPos, Float>,
  traceResult: TraceResult,
  activeHintCell: GridPos?,
  onCellTap: (GridPos) -> Unit,
  modifier: Modifier = Modifier,
) {
  val reducedMotion = rememberReducedMotion()
  val infiniteTransition = rememberInfiniteTransition(label = "beam_pulse")

  val pulse by
    infiniteTransition.animateFloat(
      initialValue = if (reducedMotion) 1f else 0.88f,
      targetValue = if (reducedMotion) 1f else 1.14f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1500, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "pulse",
    )

  val emitterRingProgress by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = if (reducedMotion) 0f else 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1800, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "emitter_ring",
    )

  val portalSpin by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = if (reducedMotion) 0f else 360f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(3800, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "portal_spin",
    )

  val hintPulse by
    infiniteTransition.animateFloat(
      initialValue = if (reducedMotion) 0.85f else 0.40f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(720, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "hint_pulse",
    )

  val beamDrawProgress = remember(level.id) { Animatable(if (reducedMotion) 1f else 0f) }
  LaunchedEffect(traceResult.segments, level.id, reducedMotion) {
    if (reducedMotion) {
      beamDrawProgress.snapTo(1f)
    } else {
      beamDrawProgress.snapTo(0f)
      beamDrawProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = PrismTokens.Motion.BeamDrawMs,
            easing = FastOutSlowInEasing,
          ),
      )
    }
  }

  var tappedCell by remember(level.id) { mutableStateOf<GridPos?>(null) }
  val tapScaleAnim = remember(level.id) { Animatable(1f) }
  LaunchedEffect(tappedCell, visualAngles) {
    if (tappedCell != null && !reducedMotion) {
      tapScaleAnim.snapTo(0.89f)
      tapScaleAnim.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
      )
    }
  }

  var prevLit by remember(level.id) { mutableStateOf<Set<GridPos>>(emptySet()) }
  val targetScaleAnim = remember(level.id) { Animatable(1f) }
  var newlyLitTargets by remember(level.id) { mutableStateOf<Set<GridPos>>(emptySet()) }
  LaunchedEffect(traceResult.litTargets, level.id) {
    val added = traceResult.litTargets - prevLit
    prevLit = traceResult.litTargets
    if (added.isNotEmpty() && !reducedMotion) {
      newlyLitTargets = added
      targetScaleAnim.snapTo(1.26f)
      targetScaleAnim.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
      )
      newlyLitTargets = emptySet()
    }
  }

  val animatedAngles =
    orientations.keys.associateWith { pos ->
      val targetAngle =
        visualAngles[pos]
          ?: if (orientations[pos] == MirrorOrientation.SLASH) 0f else 90f
      val anim by
        animateFloatAsState(
          targetValue = targetAngle,
          animationSpec =
            tween(
              durationMillis = if (reducedMotion) 0 else PrismTokens.Motion.MirrorRotateMs,
              easing = FastOutSlowInEasing,
            ),
          label = "cell_rot_${pos.row}_${pos.col}",
        )
      anim
    }

  val burstProgress = remember(level.id) { Animatable(0f) }
  LaunchedEffect(traceResult.isSolved, level.id) {
    if (traceResult.isSolved && !reducedMotion) {
      burstProgress.snapTo(0f)
      burstProgress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing),
      )
    } else {
      burstProgress.snapTo(0f)
    }
  }

  val sparks = remember {
    val palette =
      listOf(
        PrismTokens.Colors.Amber,
        PrismTokens.Colors.AmberBright,
        Color.White,
      )
    List(16) { idx ->
      Spark(
        angleRad = (idx * (2.0 * PI / 16.0)).toFloat(),
        speed = 0.72f + (idx % 4) * 0.22f,
        radius = 2.4f + (idx % 3) * 1.2f,
        color = palette[idx % palette.size],
      )
    }
  }

  // Outer machined bezel + smoked glass surface
  Surface(
    modifier = modifier.aspectRatio(1f).testTag("game_board"),
    shape = PrismTokens.Radii.modalShape,
    color = Color(0xFF0B1221),
    border =
      BorderStroke(
        width = 1.5.dp,
        brush =
          Brush.linearGradient(
            colors =
              listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.06f),
                Color.Black.copy(alpha = 0.45f),
              ),
          ),
      ),
    shadowElevation = 14.dp,
  ) {
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.radialGradient(
              colors =
                listOf(
                  Color(0xFF1A253B),
                  Color(0xFF0D1526),
                ),
            )
          )
          .padding(10.dp),
      contentAlignment = Alignment.Center,
    ) {
      val boardSizeDp = minOf(maxWidth, maxHeight)
      Box(
        modifier =
          Modifier.size(boardSizeDp)
            .pointerInput(level.id, level.size) {
              detectTapGestures { offset ->
                val cellPx = size.width.toFloat() / level.size
                val col = (offset.x / cellPx).toInt().coerceIn(0, level.size - 1)
                val row = (offset.y / cellPx).toInt().coerceIn(0, level.size - 1)
                val pos = GridPos(row, col)
                tappedCell = pos
                onCellTap(pos)
              }
            }
            .semantics {
              contentDescription = "Prism Path ${level.size} by ${level.size} puzzle board"
            }
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val n = level.size
          val cellPx = size.width / n
          val gapPx = 4.dp.toPx()

          // 1. Recessed optical-bench cells with faint rim & inner shadow
          drawOpticalBenchGrid(n = n, cellPx = cellPx, gapPx = gapPx)

          // 2. Hint indicator ring
          if (activeHintCell != null) {
            val pad = gapPx / 2f
            drawRoundRect(
              color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.16f * hintPulse),
              topLeft = Offset(activeHintCell.col * cellPx + pad, activeHintCell.row * cellPx + pad),
              size = Size(cellPx - gapPx, cellPx - gapPx),
              cornerRadius = CornerRadius(PrismTokens.Radii.cell.toPx()),
            )
            drawRoundRect(
              color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.90f * hintPulse),
              topLeft = Offset(activeHintCell.col * cellPx + pad, activeHintCell.row * cellPx + pad),
              size = Size(cellPx - gapPx, cellPx - gapPx),
              cornerRadius = CornerRadius(PrismTokens.Radii.cell.toPx()),
              style = Stroke(width = 2.dp.toPx()),
            )
          }

          // 3. Ambient caustic light spill around lit crystals
          for (pos in traceResult.litTargets) {
            val spec = level.cellMap[pos] as? CellSpec.Target
            val glowColor = (spec?.requiredColor ?: BeamColor.AMBER).toComposeColor()
            val cx = (pos.col + 0.5f) * cellPx
            val cy = (pos.row + 0.5f) * cellPx
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors =
                    listOf(
                      glowColor.copy(alpha = 0.42f * pulse),
                      glowColor.copy(alpha = 0.14f),
                      Color.Transparent,
                    ),
                  center = Offset(cx, cy),
                  radius = cellPx * 0.85f,
                ),
              radius = cellPx * 0.85f,
              center = Offset(cx, cy),
            )
          }

          // 4. Progressive 3-layer glowing laser beam + optical impact flares
          val segs = traceResult.segments
          val activeBeamHits = mutableMapOf<Offset, Color>()
          if (segs.isNotEmpty()) {
            val drawTotal = beamDrawProgress.value * segs.size
            for (i in segs.indices) {
              val frac = (drawTotal - i).coerceIn(0f, 1f)
              if (frac <= 0f) continue
              val seg = segs[i]
              val color = seg.color.toComposeColor()
              val start = Offset(seg.fromX * cellPx, seg.fromY * cellPx)
              val fullEnd = Offset(seg.toX * cellPx, seg.toY * cellPx)
              val end =
                Offset(
                  x = start.x + (fullEnd.x - start.x) * frac,
                  y = start.y + (fullEnd.y - start.y) * frac,
                )

              if (frac >= 0.98f) {
                activeBeamHits[fullEnd] = color
              }

              // Layer 1: Wide atmospheric bloom
              drawLine(
                color = color.copy(alpha = (0.24f * pulse).coerceAtMost(0.36f)),
                start = start,
                end = end,
                strokeWidth = cellPx * 0.36f * pulse,
                cap = StrokeCap.Round,
              )
              // Layer 2: Focused laser halo
              drawLine(
                color = color.copy(alpha = 0.72f),
                start = start,
                end = end,
                strokeWidth = cellPx * 0.135f,
                cap = StrokeCap.Round,
              )
              // Layer 3: Intense white core filament
              drawLine(
                color = Color.White.copy(alpha = 0.98f),
                start = start,
                end = end,
                strokeWidth = cellPx * 0.042f,
                cap = StrokeCap.Round,
              )
            }

            // Pinpoint light scatter bloom at reflection/absorption vertices
            for ((hitPos, hitColor) in activeBeamHits) {
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors =
                      listOf(
                        Color.White.copy(alpha = 0.55f),
                        hitColor.copy(alpha = 0.25f * pulse),
                        Color.Transparent,
                      ),
                    center = hitPos,
                    radius = cellPx * 0.26f,
                  ),
                radius = cellPx * 0.26f,
                center = hitPos,
              )
            }
          }

          // 5. Machined laser emitter
          drawRealisticEmitter(level, cellPx, pulse, emitterRingProgress)

          // 6. Optical board pieces
          for (cell in level.cells) {
            val cx = (cell.col + 0.5f) * cellPx
            val cy = (cell.row + 0.5f) * cellPx
            val center = Offset(cx, cy)
            val tapScale = if (cell.pos == tappedCell) tapScaleAnim.value else 1f
            val isBeamHittingCell = activeBeamHits.containsKey(center)

            when (val spec = cell.spec) {
              is CellSpec.Wall -> drawPhysicalWall(cell.pos, cellPx, gapPx)

              is CellSpec.Mirror -> {
                val angle =
                  if (spec.rotatable) {
                    animatedAngles[cell.pos]
                      ?: if (spec.initial == MirrorOrientation.SLASH) 0f else 90f
                  } else {
                    if (spec.initial == MirrorOrientation.SLASH) 0f else 90f
                  }
                scale(scale = if (spec.rotatable) tapScale else 1f, pivot = center) {
                  drawOpticalMirror(
                    center = center,
                    cellPx = cellPx,
                    angleDeg = angle,
                    rotatable = spec.rotatable,
                    isHit = isBeamHittingCell,
                  )
                }
              }

              is CellSpec.Splitter -> {
                val angle =
                  animatedAngles[cell.pos]
                    ?: if (spec.initial == MirrorOrientation.SLASH) 0f else 90f
                scale(scale = tapScale, pivot = center) {
                  drawOpticalSplitter(
                    center = center,
                    cellPx = cellPx,
                    angleDeg = angle,
                    isHit = isBeamHittingCell,
                  )
                }
              }

              is CellSpec.Filter ->
                drawStainedGlassFilter(
                  center = center,
                  cellPx = cellPx,
                  color = spec.color.toComposeColor(),
                )

              is CellSpec.Target -> {
                val isLit = cell.pos in traceResult.litTargets
                val isWrong = cell.pos in traceResult.wrongColorTargets
                val tScale = if (cell.pos in newlyLitTargets) targetScaleAnim.value else 1f
                scale(scale = tScale, pivot = center) {
                  drawFacetedCrystal(
                    center = center,
                    cellPx = cellPx,
                    requiredColor = spec.requiredColor,
                    isLit = isLit,
                    isWrongColor = isWrong,
                    pulse = pulse,
                  )
                }
              }

              is CellSpec.Portal -> {
                drawPortalLens(
                  center = center,
                  cellPx = cellPx,
                  pairId = spec.pairId,
                  spinDeg = portalSpin,
                  isActive = cell.pos in traceResult.hitPortals,
                )
              }
            }
          }

          // 7. Completion amber sparks
          val bp = burstProgress.value
          if (bp > 0f && bp < 1f) {
            val alpha = (1f - bp).coerceIn(0f, 1f)
            for (pos in traceResult.litTargets) {
              val cx = (pos.col + 0.5f) * cellPx
              val cy = (pos.row + 0.5f) * cellPx
              for (spark in sparks) {
                val dist = bp * cellPx * 1.15f * spark.speed
                val sx = cx + cos(spark.angleRad) * dist
                val sy = cy + sin(spark.angleRad) * dist
                drawCircle(
                  color = spark.color.copy(alpha = alpha),
                  radius = spark.radius * (1f - bp * 0.35f),
                  center = Offset(sx, sy),
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun DrawScope.drawOpticalBenchGrid(n: Int, cellPx: Float, gapPx: Float) {
  val halfGap = gapPx / 2f
  val tileW = cellPx - gapPx
  val corner = CornerRadius(PrismTokens.Radii.cell.toPx())
  for (r in 0 until n) {
    for (c in 0 until n) {
      val left = c * cellPx + halfGap
      val top = r * cellPx + halfGap
      val center = Offset(left + tileW / 2f, top + tileW / 2f)

      // Recessed dark slate pocket
      drawRoundRect(
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                Color(0xFF070C17),
                Color(0xFF0E1627),
              ),
            startY = top,
            endY = top + tileW,
          ),
        topLeft = Offset(left, top),
        size = Size(tileW, tileW),
        cornerRadius = corner,
      )
      // Subtle top inner shadow + bottom rim catchlight
      drawRoundRect(
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                Color.Black.copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.06f),
              ),
            startY = top,
            endY = top + tileW,
          ),
        topLeft = Offset(left, top),
        size = Size(tileW, tileW),
        cornerRadius = corner,
        style = Stroke(width = 1.dp.toPx()),
      )
      // Subtle optical registration dot at pocket center
      drawCircle(
        color = Color.White.copy(alpha = 0.045f),
        radius = 1.1.dp.toPx(),
        center = center,
      )
    }
  }
}

private fun DrawScope.drawRealisticEmitter(
  level: LevelDef,
  cellPx: Float,
  pulse: Float,
  ringProgress: Float,
) {
  val s = level.source
  val cx = (s.col + 0.5f) * cellPx
  val cy = (s.row + 0.5f) * cellPx
  val color = s.color.toComposeColor()

  if (ringProgress > 0f) {
    drawCircle(
      color = color.copy(alpha = (1f - ringProgress) * 0.42f),
      radius = cellPx * (0.20f + ringProgress * 0.27f),
      center = Offset(cx, cy),
      style = Stroke(width = 1.6.dp.toPx()),
    )
  }

  val angle =
    when (s.direction) {
      Direction.RIGHT -> 0f
      Direction.DOWN -> 90f
      Direction.LEFT -> 180f
      Direction.UP -> 270f
    }

  rotate(degrees = angle, pivot = Offset(cx, cy)) {
    // Contact shadow
    drawRoundRect(
      color = Color.Black.copy(alpha = 0.45f),
      topLeft = Offset(cx - cellPx * 0.27f, cy - cellPx * 0.21f),
      size = Size(cellPx * 0.50f, cellPx * 0.50f),
      cornerRadius = CornerRadius(cellPx * 0.14f),
    )
    // Brushed gunmetal chassis
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF3E4C63), Color(0xFF1A2233)),
          startY = cy - cellPx * 0.25f,
          endY = cy + cellPx * 0.25f,
        ),
      topLeft = Offset(cx - cellPx * 0.29f, cy - cellPx * 0.25f),
      size = Size(cellPx * 0.48f, cellPx * 0.50f),
      cornerRadius = CornerRadius(cellPx * 0.14f),
    )
    drawRoundRect(
      color = Color.White.copy(alpha = 0.22f),
      topLeft = Offset(cx - cellPx * 0.29f, cy - cellPx * 0.25f),
      size = Size(cellPx * 0.48f, cellPx * 0.50f),
      cornerRadius = CornerRadius(cellPx * 0.14f),
      style = Stroke(width = 1.dp.toPx()),
    )
    // Focal nozzle barrel
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF64748B), Color(0xFF273449)),
        ),
      topLeft = Offset(cx + cellPx * 0.13f, cy - cellPx * 0.11f),
      size = Size(cellPx * 0.21f, cellPx * 0.22f),
      cornerRadius = CornerRadius(cellPx * 0.05f),
    )
    // Glowing lens core
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(Color.White, color, color.copy(alpha = 0f)),
          center = Offset(cx - cellPx * 0.03f, cy),
          radius = cellPx * 0.21f,
        ),
      radius = cellPx * 0.21f * pulse.coerceIn(0.92f, 1.08f),
      center = Offset(cx - cellPx * 0.03f, cy),
    )
  }
}

private fun DrawScope.drawPhysicalWall(pos: GridPos, cellPx: Float, gapPx: Float) {
  val pad = gapPx / 2f + cellPx * 0.03f
  val sizeVal = cellPx - pad * 2f
  val left = pos.col * cellPx + pad
  val top = pos.row * cellPx + pad
  val radius = CornerRadius(PrismTokens.Radii.cell.toPx())

  // Drop shadow on optical bench
  drawRoundRect(
    color = Color.Black.copy(alpha = 0.5f),
    topLeft = Offset(left + 1.5.dp.toPx(), top + 2.5.dp.toPx()),
    size = Size(sizeVal, sizeVal),
    cornerRadius = radius,
  )
  // Raised chamfered slate block
  drawRoundRect(
    brush =
      Brush.linearGradient(
        colors = listOf(Color(0xFF2B384E), Color(0xFF131B2B), Color(0xFF0B101C)),
        start = Offset(left, top),
        end = Offset(left + sizeVal, top + sizeVal),
      ),
    topLeft = Offset(left, top),
    size = Size(sizeVal, sizeVal),
    cornerRadius = radius,
  )
  // Inner machined recess
  val innerPad = sizeVal * 0.18f
  drawRoundRect(
    color = Color(0xFF0E1523),
    topLeft = Offset(left + innerPad, top + innerPad),
    size = Size(sizeVal - innerPad * 2f, sizeVal - innerPad * 2f),
    cornerRadius = CornerRadius(cellPx * 0.06f),
  )
  // Top-left rim catchlight
  drawRoundRect(
    brush =
      Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
        start = Offset(left, top),
        end = Offset(left + sizeVal, top + sizeVal),
      ),
    topLeft = Offset(left, top),
    size = Size(sizeVal, sizeVal),
    cornerRadius = radius,
    style = Stroke(width = 1.dp.toPx()),
  )
}

private fun DrawScope.drawOpticalMirror(
  center: Offset,
  cellPx: Float,
  angleDeg: Float,
  rotatable: Boolean,
  isHit: Boolean,
) {
  val half = cellPx * 0.31f

  if (rotatable) {
    // Subtle circular glass turntable disc beneath rotatable mirror
    drawCircle(
      brush =
        Brush.radialGradient(
          colors =
            listOf(
              Color.White.copy(alpha = 0.055f),
              Color.White.copy(alpha = 0.015f),
            ),
          center = center,
          radius = cellPx * 0.36f,
        ),
      radius = cellPx * 0.36f,
      center = center,
    )
    drawCircle(
      color = Color.White.copy(alpha = 0.08f),
      radius = cellPx * 0.36f,
      center = center,
      style = Stroke(width = 0.8.dp.toPx()),
    )
  } else {
    // Fixed mirror bolted gunmetal clamp
    val boxHalf = cellPx * 0.36f
    drawRoundRect(
      color = Color.Black.copy(alpha = 0.4f),
      topLeft = Offset(center.x - boxHalf + 1.dp.toPx(), center.y - boxHalf + 2.dp.toPx()),
      size = Size(boxHalf * 2f, boxHalf * 2f),
      cornerRadius = CornerRadius(cellPx * 0.12f),
    )
    drawRoundRect(
      brush =
        Brush.linearGradient(
          colors = listOf(Color(0xFF232E42), Color(0xFF111827)),
        ),
      topLeft = Offset(center.x - boxHalf, center.y - boxHalf),
      size = Size(boxHalf * 2f, boxHalf * 2f),
      cornerRadius = CornerRadius(cellPx * 0.12f),
    )
    drawRoundRect(
      color = Color(0xFF64748B).copy(alpha = 0.55f),
      topLeft = Offset(center.x - boxHalf, center.y - boxHalf),
      size = Size(boxHalf * 2f, boxHalf * 2f),
      cornerRadius = CornerRadius(cellPx * 0.12f),
      style = Stroke(width = 1.1.dp.toPx()),
    )
  }

  rotate(degrees = angleDeg, pivot = center) {
    val p1 = Offset(center.x - half, center.y + half)
    val p2 = Offset(center.x + half, center.y - half)

    // Cast shadow beneath mirror bar
    drawLine(
      color = Color.Black.copy(alpha = 0.50f),
      start = Offset(p1.x + 1.5.dp.toPx(), p1.y + 2.dp.toPx()),
      end = Offset(p2.x + 1.5.dp.toPx(), p2.y + 2.dp.toPx()),
      strokeWidth = cellPx * 0.14f,
      cap = StrokeCap.Round,
    )

    // Glass-coated mirror body
    drawLine(
      brush =
        Brush.linearGradient(
          colors =
            if (rotatable) {
              listOf(
                Color(0xFF94A3B8).copy(alpha = 0.55f),
                Color(0xFFF1F5F9).copy(alpha = 0.88f),
                Color(0xFF94A3B8).copy(alpha = 0.55f),
              )
            } else {
              listOf(
                Color(0xFF475569),
                Color(0xFF94A3B8),
                Color(0xFF475569),
              )
            },
          start = p1,
          end = p2,
        ),
      start = p1,
      end = p2,
      strokeWidth = cellPx * 0.125f,
      cap = StrokeCap.Round,
    )

    // Razor-sharp specular reflection edge
    drawLine(
      color =
        if (isHit) Color.White
        else if (rotatable) Color.White.copy(alpha = 0.94f)
        else Color(0xFFCBD5E1),
      start = p1,
      end = p2,
      strokeWidth = cellPx * 0.042f,
      cap = StrokeCap.Round,
    )
  }

  if (!rotatable) {
    // Machined lock pin dot in top-right corner
    val dotOffset = cellPx * 0.25f
    drawCircle(
      color = Color(0xFF0B1221),
      radius = cellPx * 0.07f,
      center = Offset(center.x + dotOffset, center.y - dotOffset),
    )
    drawCircle(
      color = PrismTokens.Colors.TextSecondary,
      radius = cellPx * 0.045f,
      center = Offset(center.x + dotOffset, center.y - dotOffset),
    )
  }
}

private fun DrawScope.drawOpticalSplitter(
  center: Offset,
  cellPx: Float,
  angleDeg: Float,
  isHit: Boolean,
) {
  // Subtle circular turntable pad
  drawCircle(
    color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.06f),
    radius = cellPx * 0.36f,
    center = center,
  )
  drawCircle(
    color = PrismTokens.Colors.BeamBlue.copy(alpha = 0.22f),
    radius = cellPx * 0.36f,
    center = center,
    style = Stroke(width = 0.8.dp.toPx()),
  )

  rotate(degrees = angleDeg, pivot = center) {
    val half = cellPx * 0.31f
    val p1 = Offset(center.x - half, center.y + half)
    val p2 = Offset(center.x + half, center.y - half)

    // Shadow
    drawLine(
      color = Color.Black.copy(alpha = 0.45f),
      start = Offset(p1.x + 1.dp.toPx(), p1.y + 1.5.dp.toPx()),
      end = Offset(p2.x + 1.dp.toPx(), p2.y + 1.5.dp.toPx()),
      strokeWidth = cellPx * 0.13f,
      cap = StrokeCap.Round,
    )
    // Translucent dichroic glass
    drawLine(
      brush =
        Brush.linearGradient(
          colors =
            listOf(
              PrismTokens.Colors.BeamBlue.copy(alpha = 0.35f),
              Color(0xFFE0F2FE).copy(alpha = if (isHit) 0.90f else 0.68f),
              PrismTokens.Colors.BeamBlue.copy(alpha = 0.35f),
            ),
          start = p1,
          end = p2,
        ),
      start = p1,
      end = p2,
      strokeWidth = cellPx * 0.12f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0xFFBAE6FD),
      start = p1,
      end = p2,
      strokeWidth = cellPx * 0.038f,
      cap = StrokeCap.Round,
    )
    val d = cellPx * 0.085f
    val diamond =
      Path().apply {
        moveTo(center.x, center.y - d)
        lineTo(center.x + d, center.y)
        lineTo(center.x, center.y + d)
        lineTo(center.x - d, center.y)
        close()
      }
    drawPath(diamond, color = PrismTokens.Colors.BeamBlue)
  }
}

private fun DrawScope.drawStainedGlassFilter(
  center: Offset,
  cellPx: Float,
  color: Color,
) {
  // Translucent stained-glass lens dome
  drawCircle(
    brush =
      Brush.radialGradient(
        colors =
          listOf(
            color.copy(alpha = 0.34f),
            color.copy(alpha = 0.12f),
          ),
        center = center,
        radius = cellPx * 0.28f,
      ),
    radius = cellPx * 0.28f,
    center = center,
  )
  // Anodized bezel ring
  drawCircle(
    color = color,
    radius = cellPx * 0.27f,
    center = center,
    style = Stroke(width = cellPx * 0.06f),
  )
  // Top-left glass specular crescent
  drawArc(
    color = Color.White.copy(alpha = 0.65f),
    startAngle = 200f,
    sweepAngle = 75f,
    useCenter = false,
    topLeft = Offset(center.x - cellPx * 0.18f, center.y - cellPx * 0.18f),
    size = Size(cellPx * 0.36f, cellPx * 0.36f),
    style = Stroke(width = cellPx * 0.032f, cap = StrokeCap.Round),
  )
}

private fun DrawScope.drawFacetedCrystal(
  center: Offset,
  cellPx: Float,
  requiredColor: BeamColor?,
  isLit: Boolean,
  isWrongColor: Boolean,
  pulse: Float,
) {
  val baseColor = (requiredColor ?: BeamColor.AMBER).toComposeColor()
  val r = cellPx * 0.31f
  val rx = r * 0.86f

  val top = Offset(center.x, center.y - r)
  val right = Offset(center.x + rx, center.y)
  val bottom = Offset(center.x, center.y + r)
  val left = Offset(center.x - rx, center.y)

  val outer =
    Path().apply {
      moveTo(top.x, top.y)
      lineTo(right.x, right.y)
      lineTo(bottom.x, bottom.y)
      lineTo(left.x, left.y)
      close()
    }

  val ir = r * 0.45f
  val irx = rx * 0.45f
  val iTop = Offset(center.x, center.y - ir)
  val iRight = Offset(center.x + irx, center.y)
  val iBottom = Offset(center.x, center.y + ir)
  val iLeft = Offset(center.x - irx, center.y)

  val crown =
    Path().apply {
      moveTo(iTop.x, iTop.y)
      lineTo(iRight.x, iRight.y)
      lineTo(iBottom.x, iBottom.y)
      lineTo(iLeft.x, iLeft.y)
      close()
    }

  if (isLit) {
    // Rich luminous crystal fill with faceted brightness
    drawPath(
      outer,
      brush =
        Brush.linearGradient(
          colors = listOf(Color.White.copy(alpha = 0.9f), baseColor, baseColor.copy(alpha = 0.8f)),
          start = top,
          end = bottom,
        ),
    )
    drawPath(crown, color = Color.White.copy(alpha = 0.88f))
    // Facet ridges
    val ridgeColor = baseColor.copy(alpha = 0.55f)
    drawLine(ridgeColor, top, iTop, strokeWidth = 1.dp.toPx())
    drawLine(ridgeColor, right, iRight, strokeWidth = 1.dp.toPx())
    drawLine(ridgeColor, bottom, iBottom, strokeWidth = 1.dp.toPx())
    drawLine(ridgeColor, left, iLeft, strokeWidth = 1.dp.toPx())
    drawPath(outer, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 1.2.dp.toPx()))
  } else {
    val outline = if (isWrongColor) PrismTokens.Colors.Error else baseColor.copy(alpha = 0.90f)
    // Hollow smoked-crystal glass fill
    drawPath(outer, color = baseColor.copy(alpha = 0.08f))
    drawPath(outer, color = outline, style = Stroke(width = cellPx * 0.058f))
    drawPath(crown, color = outline.copy(alpha = 0.35f), style = Stroke(width = 1.dp.toPx()))
    // Subtle facet lines
    drawLine(outline.copy(alpha = 0.28f), top, iTop, strokeWidth = 1.dp.toPx())
    drawLine(outline.copy(alpha = 0.28f), right, iRight, strokeWidth = 1.dp.toPx())
    drawLine(outline.copy(alpha = 0.28f), bottom, iBottom, strokeWidth = 1.dp.toPx())
    drawLine(outline.copy(alpha = 0.28f), left, iLeft, strokeWidth = 1.dp.toPx())

    if (requiredColor != null || isWrongColor) {
      drawCircle(
        color = outline.copy(alpha = 0.78f),
        radius = cellPx * 0.055f * if (isWrongColor) pulse else 1f,
        center = center,
      )
    }
  }
}

private fun DrawScope.drawPortalLens(
  center: Offset,
  cellPx: Float,
  pairId: Int,
  spinDeg: Float,
  isActive: Boolean,
) {
  val color =
    if (pairId == 1) PrismTokens.Colors.PortalPrimary else PrismTokens.Colors.PortalSecondary
  drawCircle(
    brush =
      Brush.radialGradient(
        colors =
          listOf(
            color.copy(alpha = if (isActive) 0.42f else 0.18f),
            Color.Transparent,
          ),
        center = center,
        radius = cellPx * 0.38f,
      ),
    radius = cellPx * 0.38f,
    center = center,
  )
  rotate(degrees = if (pairId == 1) spinDeg else -spinDeg, pivot = center) {
    drawArc(
      color = color,
      startAngle = 0f,
      sweepAngle = 235f,
      useCenter = false,
      topLeft = Offset(center.x - cellPx * 0.28f, center.y - cellPx * 0.28f),
      size = Size(cellPx * 0.56f, cellPx * 0.56f),
      style = Stroke(width = cellPx * 0.058f, cap = StrokeCap.Round),
    )
    drawArc(
      color = Color.White.copy(alpha = 0.85f),
      startAngle = 140f,
      sweepAngle = 115f,
      useCenter = false,
      topLeft = Offset(center.x - cellPx * 0.17f, center.y - cellPx * 0.17f),
      size = Size(cellPx * 0.34f, cellPx * 0.34f),
      style = Stroke(width = cellPx * 0.04f, cap = StrokeCap.Round),
    )
  }
  drawCircle(
    color = Color.White.copy(alpha = 0.9f),
    radius = cellPx * 0.045f,
    center = center,
  )
}
