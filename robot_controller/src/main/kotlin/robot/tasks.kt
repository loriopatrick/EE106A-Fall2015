package robot

/**
 * @author plorio
 */

abstract class Target {
    protected val m1: Double = 0.8305
    protected val m2: Double = 1.214
    protected val lg1: Double = 0.1592
    protected val lg2: Double = 0.07309
    protected val i1: Double = 0.01311
    protected val i2: Double = 0.01361
    protected val l1: Double = 0.2898
    protected val l2: Double = 0.47
    protected val g: Double = 9.81

    protected val initShoulder = 77.25
    protected val initElbow = 164.0

    protected var tow: Double = 0.0

    abstract fun getTorque(state: RobotState): Double

    fun reset() {
        tow = 0.0
    }

    companion object {
        fun AbsMin(min: Double, v: Double): Double {
            val va = Math.abs(v)
            return va / v * Math.min(Math.abs(min), va)
        }
    }

    abstract fun error(state: RobotState): Double
}

class TargetShoulderAngle(val target: Double,
                          val maxCurrent: Double = 1.0,
                          val alpha: Double = 0.04,
                          val beta: Double = 0.01,
                          val gama: Double = 0.000) : Target() {
    override fun error(state: RobotState): Double {
        return Math.abs(target - state.shoulderAngle)
    }

    private val TI = 17.1 * 4.5

    override fun getTorque(state: RobotState): Double {
        if (Math.abs(tow) > 200 && Math.abs(state.shoulderVelocity) > 40 && Math.abs(state.shoulderAngle - target) < 5) {
            println("reset tow shoulder")
            tow = 0.0
        }

        val diff = target - state.shoulderAngle
        val towUpdate = tow + diff * state.deltaTime
        val gravity = (m1 * g * lg1 + m2 * g * l1) * Math.sin((state.shoulderAngle - initShoulder) * Math.PI / 180.0) +
                m2 * g * lg2 * Math.sin((state.elbowAngle + state.shoulderAngle - initShoulder - initElbow) * Math.PI / 180.0)
        val control = alpha * diff + beta * towUpdate + state.shoulderVelocity * gama
        var torque = control / 4.5 - gravity / TI
        val current = torque
        val applyCurrent = AbsMin(maxCurrent, current)

        if (applyCurrent == current) {
            tow = towUpdate
        }

        return applyCurrent
    }
}

class TargetElbowAngle(val target: Double,
                       val maxCurrent: Double = 1.0,
                       val alpha: Double = 0.02,
                       val beta: Double = 0.01,
                       val gama: Double = 0.000) : Target() {

    override fun error(state: RobotState): Double {
        return Math.abs(target - state.elbowAngle)
    }

    private val TI = 2.6 * 5.0

    override fun getTorque(state: RobotState): Double {
        if (tow > 100 && Math.abs(state.elbowAngle - target) < 5) {
            tow = 0.0
            println("reset tow elbow")
        }

        val diff = target - state.elbowAngle
        val towUpdate = tow + diff * state.deltaTime
        val gravity = m2 * g * lg2 * Math.sin((state.elbowAngle + state.shoulderAngle - initShoulder - initElbow) * Math.PI / 180.0)
        val control = alpha * diff + beta * towUpdate + state.elbowVelocity * gama
        var torque = control / 5 - gravity / TI
        val current = torque
        val applyCurrent = AbsMin(maxCurrent, current)
        if (applyCurrent == current) {
            tow = towUpdate
        }
        return applyCurrent
    }
}