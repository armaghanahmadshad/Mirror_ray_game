package com.example

import com.example.engine.AmbientTheme
import com.example.engine.BeamColor
import com.example.engine.CellDef
import com.example.engine.CellSpec
import com.example.engine.Direction
import com.example.engine.GridPos
import com.example.engine.LaserSource
import com.example.engine.LevelDef
import com.example.engine.MirrorOrientation
import com.example.engine.Rules
import com.example.engine.TraceBeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineUnitTest {

  @Test
  fun starCalculationMatchesSpec() {
    assertEquals(3, Rules.computeStars(moves = 1, par = 2))
    assertEquals(3, Rules.computeStars(moves = 2, par = 2))
    assertEquals(2, Rules.computeStars(moves = 3, par = 2))
    assertEquals(2, Rules.computeStars(moves = 5, par = 2))
    assertEquals(1, Rules.computeStars(moves = 6, par = 2))
  }

  @Test
  fun formatTimeFormatsMinutesAndSeconds() {
    assertEquals("0:00", Rules.formatTime(0))
    assertEquals("0:09", Rules.formatTime(9))
    assertEquals("0:59", Rules.formatTime(59))
    assertEquals("1:05", Rules.formatTime(65))
    assertEquals("12:30", Rules.formatTime(750))
    assertEquals("0:00", Rules.formatTime(-5))
  }

  @Test
  fun ambientThemesHaveLabelsAndDescriptions() {
    assertEquals(4, AmbientTheme.entries.size)
    for (theme in AmbientTheme.entries) {
      assertTrue(theme.label.isNotBlank())
      assertTrue(theme.description.isNotBlank())
    }
  }

  @Test
  fun loopProtectionTerminatesClosedMirrorCycleSafely() {
    val loopLevel =
      LevelDef(
        id = 999,
        size = 5,
        par = 1,
        source = LaserSource(1, 0, Direction.RIGHT, BeamColor.AMBER),
        cells =
          listOf(
            CellDef(1, 3, CellSpec.Mirror(MirrorOrientation.BACKSLASH, rotatable = false)),
            CellDef(3, 3, CellSpec.Mirror(MirrorOrientation.SLASH, rotatable = false)),
            CellDef(3, 1, CellSpec.Mirror(MirrorOrientation.BACKSLASH, rotatable = false)),
            CellDef(1, 1, CellSpec.Mirror(MirrorOrientation.SLASH, rotatable = false)),
            CellDef(4, 4, CellSpec.Target()),
          ),
        solutionOrientations = emptyMap(),
      )
    val result = TraceBeam.trace(loopLevel, Rules.initialOrientations(loopLevel))
    assertFalse(result.isSolved)
    assertTrue(result.segments.size <= TraceBeam.MAX_STEPS)
  }

  @Test
  fun colorFilterBlocksWrongColorTargetAndLightsMatchingColor() {
    val level =
      LevelDef(
        id = 998,
        size = 5,
        par = 1,
        source = LaserSource(2, 0, Direction.RIGHT, BeamColor.AMBER),
        cells =
          listOf(
            CellDef(2, 2, CellSpec.Filter(BeamColor.CYAN)),
            CellDef(2, 4, CellSpec.Target(BeamColor.CYAN)),
            CellDef(1, 4, CellSpec.Target(BeamColor.MAGENTA)),
          ),
        solutionOrientations = emptyMap(),
      )
    val trace = TraceBeam.trace(level, emptyMap())
    assertTrue(GridPos(2, 4) in trace.litTargets)
    assertFalse(GridPos(1, 4) in trace.litTargets)
    assertFalse(trace.isSolved)
  }
}
