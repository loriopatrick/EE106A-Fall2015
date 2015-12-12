package robot

/**
 * @author plorio
 */
data class RobotState(
        val time: Double,
        val deltaTime: Double,
        val shoulderVelocity: Double,
        val shoulderAngle: Double,
        val shoulderCurrent: Double,
        val elbowVelocity: Double,
        val elbowAngle: Double,
        val elbowCurrent: Double
)