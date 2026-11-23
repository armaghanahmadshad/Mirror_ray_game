package com.example

import com.example.engine.CellSpec
import com.example.engine.GridPos
import com.example.engine.MirrorOrientation
import com.example.engine.Rules
import com.example.engine.TraceBeam
import com.example.levels.Levels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSolvabilityTest {

  @Test
  fun all50LevelsAreValidAndSolvableWithExactPar() {
    assertEquals(50, Levels.all.size)
    val seenIds = mutableSetOf<Int>()

    val errors = mutableListOf<String>()

    for (level in Levels.all) {
      if (!seenIds.add(level.id)) errors.add("Duplicate id ${level.id}")
      if (level.size !in 5..9) errors.add("Level ${level.id}: size ${level.size} out of range")
      if (level.source.row !in 0 until level.size || level.source.col !in 0 until level.size) {
        errors.add("Level ${level.id}: source out of bounds")
      }
      if (level.targetPositions.isEmpty()) errors.add("Level ${level.id}: no targets")

      val cellPositions = mutableSetOf<GridPos>()
      for (cell in level.cells) {
        if (cell.row !in 0 until level.size || cell.col !in 0 until level.size) {
          errors.add("Level ${level.id}: cell (${cell.row},${cell.col}) out of bounds")
        }
        if (!cellPositions.add(cell.pos)) {
          errors.add("Level ${level.id}: duplicate cell (${cell.row},${cell.col})")
        }
        if (cell.pos == level.source.pos) {
          errors.add("Level ${level.id}: cell overlaps source")
        }
      }

      val initialOrientations = Rules.initialOrientations(level)
      val initialTrace = TraceBeam.trace(level, initialOrientations)
      if (initialTrace.isSolved) {
        errors.add("Level ${level.id} (${level.title}): starts already solved!")
      }

      val solutionTrace = TraceBeam.trace(level, level.solutionOrientations)
      if (!solutionTrace.isSolved) {
        errors.add(
          "Level ${level.id} (${level.title}): solution failed! lit=${solutionTrace.litTargets} expected=${level.targetPositions}"
        )
      }

      val rotatable = level.cells.filter { Rules.isRotatable(it.spec) }.map { it.pos }
      val n = rotatable.size
      var minMovesToSolve = Int.MAX_VALUE
      val totalStates = 1 shl n
      for (mask in 0 until totalStates) {
        val moves = Integer.bitCount(mask)
        if (moves >= minMovesToSolve) continue
        val map = initialOrientations.toMutableMap()
        for (i in 0 until n) {
          if ((mask and (1 shl i)) != 0) {
            val p = rotatable[i]
            map[p] = map[p]!!.toggled()
          }
        }
        if (TraceBeam.trace(level, map).isSolved) {
          minMovesToSolve = moves
        }
      }

      if (minMovesToSolve == Int.MAX_VALUE) {
        errors.add("Level ${level.id} (${level.title}): NO solution in any orientation")
      } else if (minMovesToSolve != level.par) {
        errors.add(
          "Level ${level.id} (${level.title}): par=${level.par}, actualMin=$minMovesToSolve"
        )
      }
    }

    assertTrue("Level errors:\n" + errors.joinToString("\n"), errors.isEmpty())
  }
}
