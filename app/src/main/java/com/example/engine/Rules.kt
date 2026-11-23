package com.example.engine

/**
 * Pure optical and scoring rules for Prism Path.
 */
object Rules {

  /**
   * Reflects an incoming beam traveling in [dir] off a diagonal mirror of [orientation].
   *
   * Geometry on a row-major screen grid (row increases DOWN, col increases RIGHT):
   * - SLASH (`/`):
   *   - Moving RIGHT -> reflects UP
   *   - Moving LEFT  -> reflects DOWN
   *   - Moving UP    -> reflects RIGHT
   *   - Moving DOWN  -> reflects LEFT
   * - BACKSLASH (`\`):
   *   - Moving RIGHT -> reflects DOWN
   *   - Moving LEFT  -> reflects UP
   *   - Moving UP    -> reflects LEFT
   *   - Moving DOWN  -> reflects RIGHT
   */
  fun reflect(dir: Direction, orientation: MirrorOrientation): Direction =
    when (orientation) {
      MirrorOrientation.SLASH ->
        when (dir) {
          Direction.RIGHT -> Direction.UP
          Direction.UP -> Direction.RIGHT
          Direction.LEFT -> Direction.DOWN
          Direction.DOWN -> Direction.LEFT
        }

      MirrorOrientation.BACKSLASH ->
        when (dir) {
          Direction.RIGHT -> Direction.DOWN
          Direction.DOWN -> Direction.RIGHT
          Direction.LEFT -> Direction.UP
          Direction.UP -> Direction.LEFT
        }
    }

  /**
   * Splits an incoming beam at a half-silvered diagonal splitter:
   * one ray continues straight through in [dir], and a second ray
   * reflects 90 degrees according to [reflect].
   */
  fun split(dir: Direction, orientation: MirrorOrientation): List<Direction> =
    listOf(dir, reflect(dir, orientation))

  /**
   * Star rating rule:
   * - 3 stars if moves <= par
   * - 2 stars if moves <= par + 3
   * - 1 star otherwise
   */
  fun computeStars(moves: Int, par: Int): Int =
    when {
      moves <= par -> 3
      moves <= par + 3 -> 2
      else -> 1
    }

  /**
   * Formats elapsed seconds as M:SS (e.g. 0:00, 0:42, 1:05).
   */
  fun formatTime(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val minutes = clamped / 60
    val seconds = clamped % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
  }

  /**
   * Returns true if the cell at [pos] can be tapped and rotated by the player.
   */
  fun isRotatable(spec: CellSpec?): Boolean =
    when (spec) {
      is CellSpec.Mirror -> spec.rotatable
      is CellSpec.Splitter -> spec.rotatable
      else -> false
    }

  /**
   * Builds the initial mutable orientation map for all mirrors and splitters in a [level].
   */
  fun initialOrientations(level: LevelDef): Map<GridPos, MirrorOrientation> =
    buildMap {
      for (cell in level.cells) {
        when (val s = cell.spec) {
          is CellSpec.Mirror -> put(cell.pos, s.initial)
          is CellSpec.Splitter -> put(cell.pos, s.initial)
          else -> Unit
        }
      }
    }

  /**
   * Finds a helpful hint cell to rotate next.
   * 1. First checks if rotating any single rotatable cell immediately solves the level.
   * 2. Otherwise returns a rotatable cell whose current orientation differs from the level's verified solution.
   */
  fun findHintCell(
    level: LevelDef,
    currentOrientations: Map<GridPos, MirrorOrientation>,
  ): GridPos? {
    val rotatablePositions =
      level.cells.filter { isRotatable(it.spec) }.map { it.pos }

    // 1-move winning check
    for (pos in rotatablePositions) {
      val current = currentOrientations[pos] ?: continue
      val nextMap = currentOrientations.toMutableMap().apply {
        this[pos] = current.toggled()
      }
      if (TraceBeam.trace(level, nextMap).isSolved) {
        return pos
      }
    }

    // Compare against verified solution orientations
    return rotatablePositions.firstOrNull { pos ->
      val sol = level.solutionOrientations[pos]
      sol != null && currentOrientations[pos] != sol
    }
  }
}
