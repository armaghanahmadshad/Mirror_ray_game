package com.example.engine

enum class Direction(val dr: Int, val dc: Int) {
  UP(-1, 0),
  RIGHT(0, 1),
  DOWN(1, 0),
  LEFT(0, -1),
}

enum class BeamColor {
  AMBER,
  CYAN,
  MAGENTA,
  EMERALD,
}

enum class MirrorOrientation {
  /** Diagonal from bottom-left to top-right: `/` */
  SLASH,

  /** Diagonal from top-left to bottom-right: `\` */
  BACKSLASH;

  fun toggled(): MirrorOrientation = if (this == SLASH) BACKSLASH else SLASH
}

data class GridPos(val row: Int, val col: Int) {
  fun step(dir: Direction): GridPos = GridPos(row + dir.dr, col + dir.dc)
}

sealed interface CellSpec {
  data object Wall : CellSpec

  data class Mirror(
    val initial: MirrorOrientation,
    val rotatable: Boolean = true,
  ) : CellSpec

  data class Splitter(
    val initial: MirrorOrientation,
    val rotatable: Boolean = true,
  ) : CellSpec

  data class Target(
    val requiredColor: BeamColor? = null,
  ) : CellSpec

  data class Filter(
    val color: BeamColor,
  ) : CellSpec

  data class Portal(
    val pairId: Int,
  ) : CellSpec
}

data class CellDef(
  val row: Int,
  val col: Int,
  val spec: CellSpec,
) {
  val pos: GridPos get() = GridPos(row, col)
}

data class LaserSource(
  val row: Int,
  val col: Int,
  val direction: Direction,
  val color: BeamColor = BeamColor.AMBER,
) {
  val pos: GridPos get() = GridPos(row, col)
}

data class LevelDef(
  val id: Int,
  val size: Int,
  val par: Int,
  val source: LaserSource,
  val cells: List<CellDef>,
  val solutionOrientations: Map<GridPos, MirrorOrientation>,
  val title: String = "",
) {
  val cellMap: Map<GridPos, CellSpec> by lazy { cells.associate { it.pos to it.spec } }
  val targetPositions: Set<GridPos> by lazy {
    cells.filter { it.spec is CellSpec.Target }.map { it.pos }.toSet()
  }
}

data class BeamSegment(
  val fromX: Float,
  val fromY: Float,
  val toX: Float,
  val toY: Float,
  val color: BeamColor,
)

data class TraceResult(
  val segments: List<BeamSegment>,
  val litTargets: Set<GridPos>,
  val wrongColorTargets: Set<GridPos>,
  val hitPortals: Set<GridPos>,
  val isSolved: Boolean,
)

data class PlayerProgress(
  val unlockedLevel: Int = 1,
  val starsByLevel: Map<Int, Int> = emptyMap(),
  val bestTimeSecondsByLevel: Map<Int, Int> = emptyMap(),
  val seenTutorial: Boolean = false,
  val seenMechanicTips: Set<Int> = emptySet(),
) {
  val totalStars: Int get() = starsByLevel.values.sum()
}

enum class AmbientTheme(
  val label: String,
  val description: String,
) {
  OFF("Off", "Silent focus"),
  GLASS_PRISM("Glass Prism", "Warm crystalline pads"),
  NIGHT_OBSERVATORY("Night Observatory", "Deep atmospheric drone"),
  SOLAR_DRIFT("Solar Drift", "Luminous harmonic swells"),
}

data class AppSettings(
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val ambientTheme: AmbientTheme = AmbientTheme.GLASS_PRISM,
)

sealed interface AppScreen {
  data object Home : AppScreen
  data object LevelSelect : AppScreen
  data class Game(val levelId: Int) : AppScreen
  data object Settings : AppScreen
  data object HowToPlay : AppScreen
}
