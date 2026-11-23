package com.example.levels

import com.example.engine.BeamColor
import com.example.engine.CellDef
import com.example.engine.CellSpec
import com.example.engine.Direction
import com.example.engine.GridPos
import com.example.engine.LaserSource
import com.example.engine.LevelDef
import com.example.engine.MirrorOrientation
import com.example.engine.MirrorOrientation.BACKSLASH
import com.example.engine.MirrorOrientation.SLASH

/**
 * 50 handcrafted Prism Path levels with gradual mechanic introduction:
 * - Levels 1–10:   5x5 grids — mirrors, walls, multiple targets
 * - Levels 11–15:  6x6 grids — fixed (non-rotatable) mirrors
 * - Levels 16–25:  6x6..7x7  — beam splitters
 * - Levels 26–40:  7x7..8x8  — color filters & color-matched crystal targets
 * - Levels 41–50:  8x8..9x9  — portal pairs
 */
object Levels {

  private class LevelBuilder(
    private val id: Int,
    private val size: Int,
    private val title: String,
    private val source: LaserSource,
  ) {
    private val cells = mutableListOf<CellDef>()
    private val solutions = mutableMapOf<GridPos, MirrorOrientation>()

    fun wall(r: Int, c: Int) {
      cells.add(CellDef(r, c, CellSpec.Wall))
    }

    fun mirror(r: Int, c: Int, init: MirrorOrientation, sol: MirrorOrientation) {
      cells.add(CellDef(r, c, CellSpec.Mirror(init, rotatable = true)))
      solutions[GridPos(r, c)] = sol
    }

    fun fixedMirror(r: Int, c: Int, orient: MirrorOrientation) {
      cells.add(CellDef(r, c, CellSpec.Mirror(orient, rotatable = false)))
    }

    fun splitter(r: Int, c: Int, init: MirrorOrientation, sol: MirrorOrientation) {
      cells.add(CellDef(r, c, CellSpec.Splitter(init, rotatable = true)))
      solutions[GridPos(r, c)] = sol
    }

    fun target(r: Int, c: Int, color: BeamColor? = null) {
      cells.add(CellDef(r, c, CellSpec.Target(color)))
    }

    fun filter(r: Int, c: Int, color: BeamColor) {
      cells.add(CellDef(r, c, CellSpec.Filter(color)))
    }

    fun portal(r: Int, c: Int, pairId: Int) {
      cells.add(CellDef(r, c, CellSpec.Portal(pairId)))
    }

    fun build(par: Int): LevelDef =
      LevelDef(
        id = id,
        size = size,
        par = par,
        source = source,
        cells = cells.toList(),
        solutionOrientations = solutions.toMap(),
        title = title,
      )
  }

  private fun level(
    id: Int,
    size: Int,
    par: Int,
    title: String,
    source: LaserSource,
    block: LevelBuilder.() -> Unit,
  ): LevelDef = LevelBuilder(id, size, title, source).apply(block).build(par)

  val all: List<LevelDef> =
    listOf(
      // 1-10: Core mirrors & walls (5x5)
      level(1, 5, 1, "First Spark", LaserSource(2, 0, Direction.RIGHT)) {
        mirror(2, 3, SLASH, BACKSLASH)
        target(4, 3)
      },
      level(2, 5, 2, "Corner Bounce", LaserSource(4, 0, Direction.RIGHT)) {
        mirror(4, 4, BACKSLASH, SLASH)
        mirror(0, 4, SLASH, BACKSLASH)
        target(0, 1)
        wall(2, 2)
      },
      level(3, 5, 2, "Dual Crystals", LaserSource(0, 2, Direction.DOWN)) {
        mirror(2, 2, BACKSLASH, SLASH)
        target(2, 1)
        mirror(2, 0, BACKSLASH, SLASH)
        target(4, 0)
        wall(1, 1)
        wall(3, 3)
      },
      level(4, 5, 3, "Zigzag", LaserSource(0, 0, Direction.RIGHT)) {
        mirror(0, 3, SLASH, BACKSLASH)
        mirror(2, 3, BACKSLASH, SLASH)
        target(2, 2)
        mirror(2, 1, BACKSLASH, SLASH)
        target(4, 1)
        wall(1, 1)
        wall(3, 3)
      },
      level(5, 5, 3, "Switchback", LaserSource(4, 1, Direction.UP)) {
        mirror(1, 1, BACKSLASH, SLASH)
        target(1, 3)
        mirror(1, 4, SLASH, BACKSLASH)
        mirror(3, 4, BACKSLASH, SLASH)
        target(3, 0)
        wall(2, 2)
        wall(0, 3)
        wall(4, 3)
      },
      level(6, 5, 3, "Stone Corridor", LaserSource(0, 0, Direction.DOWN)) {
        wall(2, 0)
        mirror(1, 0, SLASH, BACKSLASH)
        wall(1, 3)
        mirror(1, 2, SLASH, BACKSLASH)
        mirror(4, 2, SLASH, BACKSLASH)
        target(4, 4)
        wall(3, 1)
        wall(3, 3)
      },
      level(7, 5, 4, "Decoy Turn", LaserSource(2, 0, Direction.RIGHT)) {
        mirror(2, 2, BACKSLASH, SLASH)
        mirror(0, 2, BACKSLASH, SLASH)
        target(0, 3)
        mirror(0, 4, SLASH, BACKSLASH)
        mirror(4, 4, BACKSLASH, SLASH)
        mirror(4, 0, SLASH, SLASH) // decoy
        target(4, 1)
        wall(2, 3)
        wall(1, 1)
        wall(3, 2)
      },
      level(8, 5, 4, "Perimeter Run", LaserSource(0, 4, Direction.LEFT)) {
        mirror(0, 1, BACKSLASH, SLASH)
        mirror(4, 1, SLASH, BACKSLASH)
        mirror(4, 3, BACKSLASH, SLASH)
        mirror(2, 3, SLASH, BACKSLASH)
        target(2, 2)
        wall(1, 2)
        wall(3, 2)
        wall(0, 0)
        wall(2, 0)
      },
      level(9, 5, 4, "Crossfire", LaserSource(4, 2, Direction.UP)) {
        mirror(1, 2, BACKSLASH, SLASH)
        mirror(1, 4, SLASH, BACKSLASH)
        target(2, 4)
        mirror(3, 4, BACKSLASH, SLASH)
        target(3, 1)
        mirror(3, 0, SLASH, BACKSLASH)
        target(0, 0)
        wall(2, 1)
        wall(2, 3)
        wall(0, 2)
      },
      level(10, 5, 5, "Labyrinth I", LaserSource(0, 0, Direction.RIGHT)) {
        mirror(0, 4, SLASH, BACKSLASH)
        mirror(2, 4, BACKSLASH, SLASH)
        target(2, 2)
        mirror(2, 1, BACKSLASH, SLASH)
        mirror(4, 1, SLASH, BACKSLASH)
        mirror(4, 3, BACKSLASH, SLASH)
        target(3, 3)
        target(0, 2)
        wall(1, 1)
        wall(1, 3)
        wall(3, 2)
      },

      // 11-15: Fixed mirrors (6x6)
      level(11, 6, 2, "Anchored Glass", LaserSource(3, 0, Direction.RIGHT)) {
        mirror(3, 2, BACKSLASH, SLASH)
        fixedMirror(0, 2, SLASH)
        target(0, 4)
        mirror(0, 5, SLASH, BACKSLASH)
        fixedMirror(4, 5, SLASH)
        target(4, 1)
        wall(2, 3)
        wall(1, 4)
        wall(3, 4)
        wall(5, 2)
      },
      level(12, 6, 3, "Silver Relay", LaserSource(0, 1, Direction.DOWN)) {
        fixedMirror(2, 1, BACKSLASH)
        mirror(2, 4, SLASH, BACKSLASH)
        target(4, 4)
        fixedMirror(5, 4, SLASH)
        mirror(5, 0, SLASH, BACKSLASH)
        mirror(1, 0, BACKSLASH, SLASH)
        target(1, 5)
        wall(3, 2)
        wall(4, 2)
        wall(0, 3)
      },
      level(13, 6, 3, "Pinball Chamber", LaserSource(5, 5, Direction.LEFT)) {
        mirror(5, 2, SLASH, BACKSLASH)
        fixedMirror(1, 2, SLASH)
        target(1, 3)
        mirror(1, 5, SLASH, BACKSLASH)
        mirror(3, 5, BACKSLASH, SLASH)
        target(3, 3)
        fixedMirror(3, 0, SLASH)
        target(4, 0)
        wall(2, 3)
        wall(4, 3)
        wall(0, 4)
      },
      level(14, 6, 4, "Double Lock", LaserSource(2, 0, Direction.RIGHT)) {
        mirror(2, 3, SLASH, BACKSLASH)
        fixedMirror(5, 3, BACKSLASH)
        target(5, 4)
        mirror(5, 5, BACKSLASH, SLASH)
        fixedMirror(0, 5, BACKSLASH)
        target(0, 3)
        mirror(0, 1, BACKSLASH, SLASH)
        mirror(4, 1, SLASH, BACKSLASH)
        target(4, 4)
        wall(1, 2)
        wall(3, 2)
        wall(3, 4)
      },
      level(15, 6, 4, "Ricochet Vault", LaserSource(0, 5, Direction.DOWN)) {
        mirror(4, 5, BACKSLASH, SLASH)
        target(4, 3)
        mirror(4, 1, SLASH, BACKSLASH)
        fixedMirror(1, 1, SLASH)
        target(1, 2)
        mirror(1, 4, SLASH, BACKSLASH)
        fixedMirror(3, 4, SLASH)
        mirror(3, 2, BACKSLASH, SLASH)
        target(5, 2)
        wall(2, 2)
        wall(2, 3)
        wall(0, 3)
      },

      // 16-25: Splitters (6x6 .. 7x7)
      level(16, 6, 1, "Twin Beam", LaserSource(3, 0, Direction.RIGHT)) {
        splitter(3, 3, BACKSLASH, SLASH)
        target(3, 5)
        target(0, 3)
        wall(5, 3)
      },
      level(17, 6, 2, "Fork & Bounce", LaserSource(5, 2, Direction.UP)) {
        splitter(2, 2, BACKSLASH, SLASH)
        target(0, 2)
        mirror(2, 5, SLASH, BACKSLASH)
        target(5, 5)
        wall(2, 0)
        wall(3, 4)
        wall(1, 4)
      },
      level(18, 6, 3, "Threefold", LaserSource(2, 0, Direction.RIGHT)) {
        splitter(2, 2, SLASH, BACKSLASH)
        target(5, 2)
        splitter(2, 4, BACKSLASH, SLASH)
        target(2, 5)
        mirror(0, 4, SLASH, BACKSLASH)
        target(0, 0)
        wall(1, 1)
        wall(4, 4)
      },
      level(19, 6, 4, "Starburst", LaserSource(0, 3, Direction.DOWN)) {
        splitter(2, 3, SLASH, BACKSLASH)
        mirror(2, 5, BACKSLASH, SLASH)
        target(0, 5)
        splitter(4, 3, BACKSLASH, SLASH)
        target(5, 3)
        mirror(4, 1, SLASH, BACKSLASH)
        target(1, 1)
        wall(1, 2)
        wall(3, 2)
        wall(3, 4)
      },
      level(20, 7, 4, "Cascade", LaserSource(3, 0, Direction.RIGHT)) {
        splitter(3, 2, BACKSLASH, SLASH)
        mirror(0, 2, BACKSLASH, SLASH)
        target(0, 6)
        splitter(3, 4, SLASH, BACKSLASH)
        target(3, 6)
        fixedMirror(6, 4, SLASH)
        mirror(6, 1, SLASH, BACKSLASH)
        target(4, 1)
        wall(1, 3)
        wall(2, 3)
        wall(5, 3)
      },
      level(21, 7, 4, "Crystal Grid", LaserSource(6, 0, Direction.UP)) {
        splitter(3, 0, BACKSLASH, SLASH)
        target(0, 0)
        splitter(3, 3, SLASH, BACKSLASH)
        target(3, 6)
        splitter(5, 3, BACKSLASH, SLASH)
        target(6, 3)
        mirror(5, 1, SLASH, BACKSLASH)
        target(1, 1)
        wall(2, 2)
        wall(4, 4)
        wall(1, 4)
      },
      level(22, 7, 4, "Prism Matrix", LaserSource(0, 6, Direction.LEFT)) {
        splitter(0, 3, BACKSLASH, SLASH)
        mirror(0, 1, BACKSLASH, SLASH)
        target(4, 1)
        fixedMirror(4, 3, BACKSLASH)
        splitter(4, 5, BACKSLASH, SLASH)
        target(4, 6)
        mirror(1, 5, SLASH, BACKSLASH)
        target(1, 2)
        wall(2, 2)
        wall(2, 4)
        wall(5, 2)
      },
      level(23, 7, 3, "Center Pivot", LaserSource(3, 3, Direction.UP)) {
        splitter(1, 3, BACKSLASH, SLASH)
        target(0, 3)
        fixedMirror(1, 6, BACKSLASH)
        splitter(4, 6, BACKSLASH, SLASH)
        target(6, 6)
        mirror(4, 1, BACKSLASH, SLASH)
        target(6, 1)
        mirror(5, 5, SLASH, SLASH) // decoy
        wall(2, 2)
        wall(2, 4)
        wall(5, 2)
        wall(5, 3)
      },
      level(24, 7, 4, "Quadrant", LaserSource(0, 0, Direction.RIGHT)) {
        splitter(0, 3, SLASH, BACKSLASH)
        mirror(0, 6, SLASH, BACKSLASH)
        target(5, 6)
        splitter(3, 3, BACKSLASH, SLASH)
        mirror(3, 0, BACKSLASH, SLASH)
        target(6, 0)
        fixedMirror(6, 3, BACKSLASH)
        target(6, 5)
        wall(2, 1)
        wall(2, 5)
        wall(4, 4)
      },
      level(25, 7, 5, "Octet", LaserSource(6, 3, Direction.UP)) {
        splitter(4, 3, BACKSLASH, SLASH)
        splitter(2, 3, SLASH, BACKSLASH)
        target(0, 3)
        mirror(2, 0, BACKSLASH, SLASH)
        target(5, 0)
        mirror(4, 6, BACKSLASH, SLASH)
        splitter(1, 6, SLASH, BACKSLASH)
        target(0, 6)
        target(1, 1)
        wall(3, 1)
        wall(3, 5)
        wall(5, 4)
      },

      // 26-40: Color filters & colored crystal targets (7x7 .. 8x8)
      level(26, 7, 1, "Chromatic Gate", LaserSource(3, 0, Direction.RIGHT)) {
        filter(3, 2, BeamColor.CYAN)
        target(3, 3, BeamColor.CYAN)
        mirror(3, 4, BACKSLASH, SLASH)
        target(1, 4, BeamColor.CYAN)
        wall(3, 6)
        wall(5, 4)
      },
      level(27, 7, 2, "Spectrum Split", LaserSource(6, 3, Direction.UP)) {
        splitter(3, 3, BACKSLASH, SLASH)
        filter(2, 3, BeamColor.CYAN)
        target(0, 3, BeamColor.CYAN)
        filter(3, 4, BeamColor.MAGENTA)
        mirror(3, 6, SLASH, BACKSLASH)
        target(5, 6, BeamColor.MAGENTA)
        wall(1, 1)
        wall(4, 5)
      },
      level(28, 7, 2, "Color Shift", LaserSource(0, 0, Direction.RIGHT)) {
        target(0, 2, BeamColor.AMBER)
        filter(0, 3, BeamColor.EMERALD)
        mirror(0, 5, SLASH, BACKSLASH)
        target(3, 5, BeamColor.EMERALD)
        filter(4, 5, BeamColor.CYAN)
        mirror(6, 5, BACKSLASH, SLASH)
        target(6, 1, BeamColor.CYAN)
        wall(2, 2)
        wall(4, 3)
      },
      level(29, 7, 3, "RGB Trio", LaserSource(3, 0, Direction.RIGHT)) {
        splitter(3, 2, SLASH, BACKSLASH)
        filter(4, 2, BeamColor.EMERALD)
        target(6, 2, BeamColor.EMERALD)
        splitter(3, 4, BACKSLASH, SLASH)
        filter(3, 5, BeamColor.MAGENTA)
        target(3, 6, BeamColor.MAGENTA)
        filter(2, 4, BeamColor.CYAN)
        mirror(0, 4, SLASH, BACKSLASH)
        target(0, 1, BeamColor.CYAN)
        wall(1, 2)
        wall(5, 4)
      },
      level(30, 7, 3, "Filter Loopback", LaserSource(0, 6, Direction.DOWN)) {
        mirror(5, 6, BACKSLASH, SLASH)
        filter(5, 4, BeamColor.CYAN)
        splitter(5, 2, SLASH, BACKSLASH)
        target(5, 0, BeamColor.CYAN)
        filter(3, 2, BeamColor.MAGENTA)
        mirror(1, 2, BACKSLASH, SLASH)
        target(1, 5, BeamColor.MAGENTA)
        wall(2, 4)
        wall(4, 4)
        wall(0, 3)
      },
      level(31, 7, 4, "Chromatic Cross", LaserSource(0, 3, Direction.DOWN)) {
        target(1, 3, BeamColor.AMBER)
        splitter(2, 3, SLASH, BACKSLASH)
        filter(2, 4, BeamColor.CYAN)
        mirror(2, 6, SLASH, BACKSLASH)
        target(6, 6, BeamColor.CYAN)
        filter(4, 3, BeamColor.MAGENTA)
        splitter(5, 3, BACKSLASH, SLASH)
        target(6, 3, BeamColor.MAGENTA)
        filter(5, 2, BeamColor.EMERALD)
        mirror(5, 0, SLASH, BACKSLASH)
        target(1, 0, BeamColor.EMERALD)
        wall(3, 1)
        wall(3, 5)
      },
      level(32, 8, 3, "Prism Chamber", LaserSource(4, 0, Direction.RIGHT)) {
        splitter(4, 2, BACKSLASH, SLASH)
        filter(2, 2, BeamColor.CYAN)
        fixedMirror(0, 2, SLASH)
        target(0, 6, BeamColor.CYAN)
        filter(4, 4, BeamColor.MAGENTA)
        splitter(4, 6, SLASH, BACKSLASH)
        target(4, 7, BeamColor.MAGENTA)
        filter(5, 6, BeamColor.EMERALD)
        mirror(7, 6, BACKSLASH, SLASH)
        target(7, 1, BeamColor.EMERALD)
        wall(1, 4)
        wall(3, 4)
        wall(6, 3)
      },
      level(33, 8, 4, "Refraction Array", LaserSource(7, 1, Direction.UP)) {
        splitter(4, 1, BACKSLASH, SLASH)
        filter(2, 1, BeamColor.CYAN)
        mirror(0, 1, BACKSLASH, SLASH)
        target(0, 5, BeamColor.CYAN)
        filter(4, 3, BeamColor.EMERALD)
        splitter(4, 5, BACKSLASH, SLASH)
        target(4, 7, BeamColor.EMERALD)
        filter(3, 5, BeamColor.MAGENTA)
        mirror(1, 5, SLASH, BACKSLASH)
        target(1, 2, BeamColor.MAGENTA)
        wall(3, 3)
        wall(5, 4)
        wall(6, 6)
      },
      level(34, 8, 3, "Split Palette", LaserSource(0, 4, Direction.DOWN)) {
        target(1, 4, BeamColor.AMBER)
        splitter(2, 4, SLASH, BACKSLASH)
        filter(2, 6, BeamColor.CYAN)
        mirror(2, 7, SLASH, BACKSLASH)
        target(6, 7, BeamColor.CYAN)
        splitter(5, 4, BACKSLASH, SLASH)
        target(7, 4, BeamColor.AMBER)
        filter(5, 2, BeamColor.MAGENTA)
        target(5, 1, BeamColor.MAGENTA)
        fixedMirror(5, 0, BACKSLASH)
        filter(3, 0, BeamColor.EMERALD)
        target(0, 0, BeamColor.EMERALD)
        wall(1, 2)
        wall(6, 3)
      },
      level(35, 8, 4, "Emerald Conduit", LaserSource(3, 7, Direction.LEFT)) {
        filter(3, 5, BeamColor.EMERALD)
        splitter(3, 3, BACKSLASH, SLASH)
        mirror(3, 0, SLASH, BACKSLASH)
        target(0, 0, BeamColor.EMERALD)
        filter(5, 3, BeamColor.CYAN)
        splitter(6, 3, BACKSLASH, SLASH)
        target(7, 3, BeamColor.CYAN)
        filter(6, 2, BeamColor.MAGENTA)
        mirror(6, 0, SLASH, BACKSLASH)
        target(4, 0, BeamColor.MAGENTA)
        wall(1, 2)
        wall(2, 5)
        wall(5, 5)
      },
      level(36, 8, 4, "Laser Cathedral", LaserSource(7, 4, Direction.UP)) {
        splitter(5, 4, BACKSLASH, SLASH)
        filter(5, 6, BeamColor.CYAN)
        fixedMirror(5, 7, SLASH)
        target(1, 7, BeamColor.CYAN)
        filter(3, 4, BeamColor.MAGENTA)
        splitter(2, 4, SLASH, BACKSLASH)
        target(0, 4, BeamColor.MAGENTA)
        mirror(2, 1, BACKSLASH, SLASH)
        filter(4, 1, BeamColor.EMERALD)
        mirror(7, 1, SLASH, BACKSLASH)
        target(7, 3, BeamColor.EMERALD)
        wall(1, 2)
        wall(4, 3)
        wall(6, 6)
      },
      level(37, 8, 4, "Overlapping Rays", LaserSource(0, 0, Direction.RIGHT)) {
        splitter(0, 3, SLASH, BACKSLASH)
        filter(0, 5, BeamColor.CYAN)
        mirror(0, 7, SLASH, BACKSLASH)
        target(4, 7, BeamColor.CYAN)
        filter(2, 3, BeamColor.MAGENTA)
        splitter(4, 3, BACKSLASH, SLASH)
        target(7, 3, BeamColor.MAGENTA)
        mirror(4, 0, BACKSLASH, SLASH)
        filter(5, 0, BeamColor.AMBER)
        target(7, 0, BeamColor.AMBER)
        wall(2, 5)
        wall(5, 5)
        wall(6, 2)
      },
      level(38, 8, 4, "Chromatic Lock", LaserSource(4, 7, Direction.LEFT)) {
        target(4, 6, BeamColor.AMBER)
        splitter(4, 4, SLASH, BACKSLASH)
        filter(2, 4, BeamColor.CYAN)
        mirror(0, 4, BACKSLASH, SLASH)
        target(0, 7, BeamColor.CYAN)
        filter(4, 2, BeamColor.EMERALD)
        splitter(4, 1, BACKSLASH, SLASH)
        target(4, 0, BeamColor.EMERALD)
        filter(6, 1, BeamColor.MAGENTA)
        fixedMirror(7, 1, BACKSLASH)
        mirror(7, 5, BACKSLASH, SLASH)
        target(5, 5, BeamColor.MAGENTA)
        wall(1, 2)
        wall(3, 2)
        wall(6, 4)
      },
      level(39, 8, 4, "Four Colors", LaserSource(0, 3, Direction.DOWN)) {
        target(1, 3, BeamColor.AMBER)
        splitter(2, 3, BACKSLASH, SLASH)
        filter(2, 1, BeamColor.CYAN)
        mirror(2, 0, SLASH, BACKSLASH)
        target(0, 0, BeamColor.CYAN)
        splitter(4, 3, SLASH, BACKSLASH)
        filter(4, 5, BeamColor.MAGENTA)
        fixedMirror(4, 7, BACKSLASH)
        target(7, 7, BeamColor.MAGENTA)
        filter(6, 3, BeamColor.EMERALD)
        mirror(7, 3, BACKSLASH, SLASH)
        target(7, 0, BeamColor.EMERALD)
        wall(1, 5)
        wall(3, 5)
        wall(5, 1)
      },
      level(40, 8, 4, "Spectrum Core", LaserSource(7, 0, Direction.RIGHT)) {
        target(7, 1, BeamColor.AMBER)
        splitter(7, 2, BACKSLASH, SLASH)
        filter(7, 4, BeamColor.CYAN)
        mirror(7, 7, BACKSLASH, SLASH)
        target(2, 7, BeamColor.CYAN)
        splitter(4, 2, SLASH, BACKSLASH)
        filter(4, 1, BeamColor.MAGENTA)
        mirror(4, 0, SLASH, BACKSLASH)
        target(0, 0, BeamColor.MAGENTA)
        filter(2, 2, BeamColor.EMERALD)
        fixedMirror(0, 2, SLASH)
        target(0, 6, BeamColor.EMERALD)
        wall(1, 4)
        wall(3, 4)
        wall(5, 4)
        wall(5, 6)
      },

      // 41-50: Portal pairs (8x8 .. 9x9)
      level(41, 8, 2, "Warp Gate", LaserSource(4, 0, Direction.RIGHT)) {
        for (r in 0 until 8) wall(r, 4)
        mirror(4, 2, BACKSLASH, SLASH)
        portal(1, 2, 1)
        portal(6, 5, 1)
        mirror(2, 5, BACKSLASH, SLASH)
        target(2, 7)
      },
      level(42, 8, 3, "Twin Warp", LaserSource(0, 0, Direction.DOWN)) {
        for (c in 0 until 8) wall(3, c)
        mirror(2, 0, SLASH, BACKSLASH)
        portal(2, 3, 1)
        portal(5, 1, 1)
        splitter(5, 4, BACKSLASH, SLASH)
        target(4, 4)
        splitter(5, 6, SLASH, BACKSLASH)
        target(5, 7)
        target(7, 6)
      },
      level(43, 8, 2, "Phase Tunnel", LaserSource(7, 7, Direction.LEFT)) {
        for (r in 0 until 8) wall(r, 3)
        mirror(7, 4, SLASH, BACKSLASH)
        portal(5, 4, 1)
        portal(5, 1, 1)
        filter(3, 1, BeamColor.CYAN)
        splitter(2, 1, BACKSLASH, SLASH)
        target(2, 2, BeamColor.CYAN)
        portal(1, 1, 2)
        portal(1, 5, 2)
        target(0, 5, BeamColor.CYAN)
      },
      level(44, 8, 2, "Chromatic Warp", LaserSource(0, 7, Direction.LEFT)) {
        for (c in 0 until 8) if (c != 5) wall(1, c)
        splitter(0, 5, BACKSLASH, SLASH)
        filter(0, 3, BeamColor.CYAN)
        portal(0, 1, 1)
        portal(6, 2, 1)
        target(6, 0, BeamColor.CYAN)
        filter(2, 5, BeamColor.MAGENTA)
        mirror(4, 5, BACKSLASH, SLASH)
        portal(4, 3, 2)
        portal(2, 1, 2)
        target(2, 0, BeamColor.MAGENTA)
        wall(5, 1)
        wall(5, 3)
        wall(5, 5)
      },
      level(45, 9, 3, "Mirror Paradox", LaserSource(4, 0, Direction.RIGHT)) {
        splitter(4, 2, BACKSLASH, SLASH)
        portal(1, 2, 1)
        portal(7, 6, 1)
        filter(5, 6, BeamColor.EMERALD)
        fixedMirror(3, 6, SLASH)
        target(3, 8, BeamColor.EMERALD)
        mirror(4, 5, SLASH, BACKSLASH)
        portal(7, 5, 2)
        portal(1, 4, 2)
        filter(2, 4, BeamColor.CYAN)
        mirror(3, 4, BACKSLASH, SLASH)
        target(3, 0, BeamColor.CYAN)
        wall(2, 1)
        wall(6, 2)
        wall(4, 7)
        wall(0, 6)
      },
      level(46, 9, 4, "Quantum Relay", LaserSource(8, 4, Direction.UP)) {
        splitter(6, 4, BACKSLASH, SLASH)
        mirror(6, 7, BACKSLASH, SLASH)
        filter(4, 7, BeamColor.CYAN)
        portal(2, 7, 1)
        portal(2, 1, 1)
        target(0, 1, BeamColor.CYAN)
        filter(4, 4, BeamColor.MAGENTA)
        splitter(3, 4, SLASH, BACKSLASH)
        target(0, 4, BeamColor.MAGENTA)
        portal(3, 2, 2)
        portal(7, 3, 2)
        mirror(7, 0, SLASH, BACKSLASH)
        target(4, 0, BeamColor.MAGENTA)
        wall(5, 2)
        wall(5, 6)
        wall(1, 6)
        wall(2, 5)
      },
      level(47, 9, 4, "Hyperbolic", LaserSource(0, 4, Direction.DOWN)) {
        target(1, 4, BeamColor.AMBER)
        splitter(2, 4, SLASH, BACKSLASH)
        mirror(2, 7, SLASH, BACKSLASH)
        portal(6, 7, 1)
        portal(6, 1, 1)
        filter(7, 1, BeamColor.EMERALD)
        target(8, 1, BeamColor.EMERALD)
        splitter(5, 4, BACKSLASH, SLASH)
        filter(7, 4, BeamColor.CYAN)
        target(8, 4, BeamColor.CYAN)
        filter(5, 2, BeamColor.MAGENTA)
        portal(5, 0, 2)
        portal(0, 8, 2)
        mirror(0, 6, BACKSLASH, SLASH)
        target(3, 6, BeamColor.MAGENTA)
        wall(3, 2)
        wall(3, 5)
        wall(6, 6)
      },
      level(48, 9, 4, "Prism Nexus", LaserSource(0, 0, Direction.RIGHT)) {
        splitter(0, 3, SLASH, BACKSLASH)
        filter(0, 5, BeamColor.CYAN)
        portal(0, 8, 1)
        portal(8, 2, 1)
        mirror(8, 7, BACKSLASH, SLASH)
        target(5, 7, BeamColor.CYAN)
        splitter(3, 3, BACKSLASH, SLASH)
        filter(3, 1, BeamColor.MAGENTA)
        fixedMirror(3, 0, BACKSLASH)
        target(1, 0, BeamColor.MAGENTA)
        mirror(6, 3, SLASH, BACKSLASH)
        filter(6, 5, BeamColor.EMERALD)
        portal(6, 6, 2)
        portal(2, 5, 2)
        target(2, 8, BeamColor.EMERALD)
        wall(1, 2)
        wall(4, 4)
        wall(5, 2)
        wall(7, 5)
      },
      level(49, 9, 4, "Singularity", LaserSource(8, 0, Direction.RIGHT)) {
        splitter(8, 2, BACKSLASH, SLASH)
        filter(8, 4, BeamColor.CYAN)
        splitter(8, 6, BACKSLASH, SLASH)
        target(8, 8, BeamColor.CYAN)
        portal(5, 6, 1)
        portal(5, 1, 1)
        target(2, 1, BeamColor.CYAN)
        filter(6, 2, BeamColor.MAGENTA)
        mirror(3, 2, BACKSLASH, SLASH)
        target(3, 4, BeamColor.MAGENTA)
        portal(3, 5, 2)
        portal(1, 3, 2)
        filter(1, 5, BeamColor.EMERALD)
        mirror(1, 8, SLASH, BACKSLASH)
        target(6, 8, BeamColor.EMERALD)
        wall(4, 4)
        wall(2, 4)
        wall(7, 4)
        wall(4, 7)
      },
      level(50, 9, 4, "Grand Prism", LaserSource(4, 0, Direction.RIGHT)) {
        target(4, 1, BeamColor.AMBER)
        splitter(4, 2, BACKSLASH, SLASH)
        filter(2, 2, BeamColor.CYAN)
        mirror(0, 2, BACKSLASH, SLASH)
        portal(0, 4, 1)
        portal(6, 3, 1)
        target(6, 5, BeamColor.CYAN)
        wall(6, 7)
        splitter(4, 5, SLASH, BACKSLASH)
        filter(4, 6, BeamColor.MAGENTA)
        fixedMirror(4, 8, SLASH)
        target(1, 8, BeamColor.MAGENTA)
        filter(5, 5, BeamColor.EMERALD)
        mirror(7, 5, BACKSLASH, SLASH)
        portal(7, 3, 2)
        portal(2, 6, 2)
        target(2, 4, BeamColor.EMERALD)
        wall(1, 3)
        wall(3, 3)
        wall(5, 3)
        wall(7, 7)
        wall(3, 6)
      },
    )

  fun byId(id: Int): LevelDef = all.firstOrNull { it.id == id } ?: all.first()
}
