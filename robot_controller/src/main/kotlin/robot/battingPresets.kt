package robot

/**
 * @author plorio
 */

data class BattingPreset(val dropTime: Double, val height: Double, val overHand: Boolean) {
    fun toBat(): Batting {
        if (overHand) {
            return Batting.Overhand(dropTime)
        }
        return Batting.Underhand(dropTime)
    }
}

val presets2 = arrayOf(
        // zero state: 79, 160
        BattingPreset(-0.2, 130.0, true),
        BattingPreset(-0.175, 155.0, true),
        BattingPreset(-0.25, 120.0, true),
        BattingPreset(-0.3, 103.0, true),
        BattingPreset(-0.35, 90.0, true),
        BattingPreset(-0.4, 87.0, true),
        BattingPreset(-0.4, 89.0, false),
        BattingPreset(-0.5, 45.0, false),
        BattingPreset(-0.45, 60.0, false),
        BattingPreset(-0.425, 75.0, false)
        //        BattingPreset(-0.52, 92.0, false)
)

val presets = arrayOf(
        BattingPreset(-0.5, 75.0, false), // :)


        BattingPreset(-0.35, 130.0, false),
        BattingPreset(-0.375, 135.0, false),
        BattingPreset(-0.4, 119.0, false),
        BattingPreset(-0.45, 90.0, false),
        BattingPreset(-0.475, 85.0, false),

        BattingPreset(-0.25, 135.0, true),
        BattingPreset(-0.23, 105.0, true),
        BattingPreset(-0.27, 75.0, true),
        BattingPreset(-0.2, 120.0, true)
)

fun getBestBatting(height: Double): Batting {
    val preset = presets2.minBy {
        Math.abs(it.height - height)
    } ?: throw RuntimeException()
    println("use preset $preset")

    if (preset.overHand) {
        return Batting.Overhand(preset.dropTime)
    }
    return Batting.Underhand(preset.dropTime)
}