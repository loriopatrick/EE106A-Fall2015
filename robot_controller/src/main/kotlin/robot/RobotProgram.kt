package robot

/**
 * @author plorio
 */
class RobotProgram(private val controller: RobotInterface, val update: (RobotProgram, RobotState) -> Unit) {
    var shoulderTorque: Double = 0.0
        set(torque: Double) {
            if (torque < 0) {
                controller.shoulderDirection = Direction.CLOCKWISE
                controller.shoulderCurrent = (Math.min(-torque, 1.0) * 255).toByte()
            } else if (torque > 0) {
                controller.shoulderDirection = Direction.COUNTER_CLOCKWISE
                controller.shoulderCurrent = (Math.min(torque, 1.0) * 255).toByte()
            } else {
                controller.shoulderDirection = Direction.STATIC
                controller.shoulderCurrent = 0
            }
        }

    var elbowTorque: Double = 0.0
        set(torque: Double) {
            if (torque < 0) {
                controller.elbowDirection = Direction.COUNTER_CLOCKWISE
                controller.elbowCurrent = (Math.min(-torque, 1.0) * 255).toByte()
            } else if (torque > 0) {
                controller.elbowDirection = Direction.CLOCKWISE
                controller.elbowCurrent = (Math.min(torque, 1.0) * 255).toByte()
            } else {
                controller.elbowDirection = Direction.STATIC
                controller.elbowCurrent = 0
            }
        }

    var drop: Boolean = false
        set(drop: Boolean) {
            controller.drop = drop
        }

    fun kill() {
        shoulderTorque = 0.0
        elbowTorque = 0.0
    }

    private var running = false

    var state: RobotState = RobotState(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        private set

    fun start() {
        if (running) {
            return
        }

        var lastState = controller.read()
        controller.write()

        var shoulderVelocity = VelocityCalculator(5)
        var elbowVelocity = VelocityCalculator(5)

        val velMul = 2.0 / (2.0 + 1)

        running = true
        while (running) {
            val state1 = controller.read()
            controller.write()

            val state2 = controller.read()
            controller.write()

            val state3 = controller.read()
            controller.write()

            val state4 = controller.read()

            val state = Pair(
                    JointState.Average(state1.first, state2.first, state3.first, state4.first),
                    JointState.Average(state1.second, state2.second, state3.second, state4.second)
            )

            var angleDiff = state.second.angle - lastState.second.angle

            val deltaTime = (state.first.time - lastState.first.time).toDouble() / 1000

            this.state = RobotState(
                    time = state.first.time / 1000.0,
                    deltaTime = deltaTime,
                    shoulderVelocity = shoulderVelocity.calc((state.first.angle - lastState.first.angle) / deltaTime),
                    shoulderAngle = state.first.angle.toDouble(),
                    shoulderCurrent = state.first.motorCurrent / 255.0,

                    elbowVelocity = elbowVelocity.calc(angleDiff / deltaTime),
                    elbowAngle = state.second.angle.toDouble(),
                    elbowCurrent = state.second.motorCurrent / 255.0
            )

            lastState = state
            update(this, this.state)
            controller.write()
        }
    }

    fun stop() {
        running = false
    }
}