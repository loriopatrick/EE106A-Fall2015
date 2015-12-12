package robot

/**
 * @author plorio
 */
data class JointState(
        val time: Long,
        val angle: Double,
        val motorCurrent: Double
) {
    companion object {
        fun Average(vararg states: JointState) : JointState {
            return JointState(
                    Math.round(states.foldRight(0.0, { j, i -> i + j.time }) / states.size),
                    states.foldRight(0.0, { j, i -> i + j.angle }) / states.size,
                    states.foldRight(0.0, { j, i -> i + j.motorCurrent }) / states.size
            )
        }
    }
}