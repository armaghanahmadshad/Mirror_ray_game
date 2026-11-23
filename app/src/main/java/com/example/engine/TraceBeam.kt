package com.example.engine

/**
 * Pure ray-tracing engine for Prism Path.
 *
 * Traces one or more light rays across a square [LevelDef.size] x [LevelDef.size] grid
 * using a breadth-first queue of active rays.
 *
 * Coordinate convention for rendering:
 * Cell `(row, col)` has its geometric center at `(x = col + 0.5f, y = row + 0.5f)`.
 * Segments are emitted from cell-center to cell-center (or cell-center to cell-edge
 * when terminating against a wall or the board perimeter).
 *
 * Loop protection:
 * - Tracks visited `(pos, direction, color)` states so identical rays cannot loop infinitely.
 * - Caps total propagation steps at [MAX_STEPS] (200) per trace.
 */
object TraceBeam {

  const val MAX_STEPS = 200

  private data class RayState(
    val pos: GridPos,
    val dir: Direction,
    val color: BeamColor,
  )

  fun trace(
    level: LevelDef,
    orientations: Map<GridPos, MirrorOrientation>,
  ): TraceResult {
    val segments = mutableListOf<BeamSegment>()
    val litTargets = mutableSetOf<GridPos>()
    val wrongColorTargets = mutableSetOf<GridPos>()
    val hitPortals = mutableSetOf<GridPos>()
    val visited = mutableSetOf<RayState>()

    // Pre-index portal pairs so entering one portal jumps to its twin.
    val portalPairs = mutableMapOf<GridPos, GridPos>()
    val portalsById = level.cells
      .mapNotNull { cell -> (cell.spec as? CellSpec.Portal)?.let { it.pairId to cell.pos } }
      .groupBy({ it.first }, { it.second })
    for ((_, positions) in portalsById) {
      if (positions.size == 2) {
        portalPairs[positions[0]] = positions[1]
        portalPairs[positions[1]] = positions[0]
      }
    }

    val queue = ArrayDeque<RayState>()
    val startRay = RayState(level.source.pos, level.source.direction, level.source.color)
    queue.addLast(startRay)
    visited.add(startRay)

    var steps = 0
    while (queue.isNotEmpty() && steps < MAX_STEPS) {
      val ray = queue.removeFirst()
      steps++

      val curr = ray.pos
      val next = curr.step(ray.dir)
      val fromX = curr.col + 0.5f
      val fromY = curr.row + 0.5f

      // 1. Check board bounds: terminate cleanly at outer grid edge.
      if (next.row !in 0 until level.size || next.col !in 0 until level.size) {
        segments.add(
          BeamSegment(
            fromX = fromX,
            fromY = fromY,
            toX = fromX + ray.dir.dc * 0.5f,
            toY = fromY + ray.dir.dr * 0.5f,
            color = ray.color,
          )
        )
        continue
      }

      // 2. Check if beam hits the source emitter back from outside: stop at source boundary.
      if (next == level.source.pos) {
        segments.add(
          BeamSegment(
            fromX = fromX,
            fromY = fromY,
            toX = fromX + ray.dir.dc * 0.5f,
            toY = fromY + ray.dir.dr * 0.5f,
            color = ray.color,
          )
        )
        continue
      }

      val nextSpec = level.cellMap[next]

      // 3. Walls absorb the beam at the wall's face.
      if (nextSpec is CellSpec.Wall) {
        segments.add(
          BeamSegment(
            fromX = fromX,
            fromY = fromY,
            toX = fromX + ray.dir.dc * 0.5f,
            toY = fromY + ray.dir.dr * 0.5f,
            color = ray.color,
          )
        )
        continue
      }

      // Beam enters `next` cell center.
      val toX = next.col + 0.5f
      val toY = next.row + 0.5f
      segments.add(
        BeamSegment(
          fromX = fromX,
          fromY = fromY,
          toX = toX,
          toY = toY,
          color = ray.color,
        )
      )

      // 4. Determine outgoing ray(s) based on the cell occupant.
      when (nextSpec) {
        null -> {
          enqueueIfNew(RayState(next, ray.dir, ray.color), visited, queue)
        }

        is CellSpec.Mirror -> {
          val orient = orientations[next] ?: nextSpec.initial
          val reflectedDir = Rules.reflect(ray.dir, orient)
          enqueueIfNew(RayState(next, reflectedDir, ray.color), visited, queue)
        }

        is CellSpec.Splitter -> {
          val orient = orientations[next] ?: nextSpec.initial
          val outDirs = Rules.split(ray.dir, orient)
          for (outDir in outDirs) {
            enqueueIfNew(RayState(next, outDir, ray.color), visited, queue)
          }
        }

        is CellSpec.Filter -> {
          enqueueIfNew(RayState(next, ray.dir, nextSpec.color), visited, queue)
        }

        is CellSpec.Target -> {
          if (nextSpec.requiredColor == null || nextSpec.requiredColor == ray.color) {
            litTargets.add(next)
          } else {
            wrongColorTargets.add(next)
          }
          // Light passes straight through crystal targets.
          enqueueIfNew(RayState(next, ray.dir, ray.color), visited, queue)
        }

        is CellSpec.Portal -> {
          hitPortals.add(next)
          val partner = portalPairs[next]
          if (partner != null) {
            hitPortals.add(partner)
            enqueueIfNew(RayState(partner, ray.dir, ray.color), visited, queue)
          }
        }

        is CellSpec.Wall -> Unit // Already handled above
      }
    }

    val targets = level.targetPositions
    val isSolved = targets.isNotEmpty() && litTargets.containsAll(targets)
    return TraceResult(
      segments = segments,
      litTargets = litTargets,
      wrongColorTargets = wrongColorTargets - litTargets,
      hitPortals = hitPortals,
      isSolved = isSolved,
    )
  }

  private fun enqueueIfNew(
    state: RayState,
    visited: MutableSet<RayState>,
    queue: ArrayDeque<RayState>,
  ) {
    if (visited.add(state)) {
      queue.addLast(state)
    }
  }
}
