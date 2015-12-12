package robot

/**
 * @author plorio
 */
class Batting(val initDropTime : Double,
              val initShoulderAngle : Double,
              val initElbowAngle: Double,
              val elbowSwing: Double,
              val shoulderSwing: Double,
              val elbowStop: Double,
              val shoulderStop: Double) {

    var mode: State = State.SET_START_ANGLE
    private set

    private var dropTime = initDropTime
    private val shoulderTarget = TargetShoulderAngle(initShoulderAngle)
    private val elbowTarget = TargetElbowAngle(initElbowAngle)

    fun update(program: RobotProgram, state: RobotState) {
        program.drop = false

        if (mode == State.SET_START_ANGLE) {
            program.shoulderTorque = shoulderTarget.getTorque(state)
            program.elbowTorque = elbowTarget.getTorque(state)

            if (shoulderTarget.error(state) < 5 && elbowTarget.error(state) < 5) {
                mode = State.WAITING_FOR_DROP

                println("Waiting for drop")
            }
        } else if (mode == State.WAITING_FOR_DROP) {
            program.shoulderTorque = shoulderTarget.getTorque(state)
            program.elbowTorque = elbowTarget.getTorque(state)
        } else if (mode == State.WAITING_FOR_POS) {
            program.shoulderTorque = shoulderTarget.getTorque(state)
            program.elbowTorque = elbowTarget.getTorque(state)

            if (shoulderTarget.error(state) < 5 && elbowTarget.error(state) < 5) {
                mode = State.BATTING
                println("Batting")
            }
        } else if (mode == State.BATTING) {
            program.drop = dropTime <= 0.0

            if (dropTime > 0) {
                dropTime -= state.deltaTime
                if (dropTime <= 0) {
                    dropTime = 0.0
                }
            } else if (dropTime < 0) {
                dropTime += state.deltaTime
                if (dropTime >= 0) {
                    dropTime = 0.0
                }
            } else {
                dropTime = 0.0
            }

            if (dropTime == 0.0) {
                if (elbowSwing == 0.0) {
                    program.elbowTorque = elbowTarget.getTorque(state)
                } else {
                    program.elbowTorque = elbowSwing
                }

                if (shoulderSwing == 0.0) {
                    program.shoulderTorque = shoulderTarget.getTorque(state)
                } else {
                    program.shoulderTorque = shoulderSwing
                }
            } else {
                program.shoulderTorque = shoulderTarget.getTorque(state)
                program.elbowTorque = elbowTarget.getTorque(state)
            }

            if (elbowSwing > 0 && state.elbowAngle > elbowStop) {
                mode = State.DONE
            }

            if (elbowSwing < 0 && state.elbowAngle < elbowStop) {
                mode = State.DONE
            }

            if (shoulderSwing > 0 && state.shoulderAngle > shoulderStop) {
                mode = State.DONE
            }

            if (shoulderSwing < 0 && state.shoulderAngle < shoulderStop) {
                mode = State.DONE
            }

            if (mode == State.DONE) {
                program.kill()
            }
        } else if (mode == State.DONE) {
            program.kill()
        }
    }

    fun drop() {
        if (mode == State.WAITING_FOR_DROP) {
            mode = State.WAITING_FOR_POS
            println("Waiting for pos")
        } else {
            println("current mode: $mode")
        }
    }

    override fun toString(): String {
        return "Batting(mode=$mode, initDropTime=$initDropTime, initShoulderAngle=$initShoulderAngle, initElbowAngle=$initElbowAngle)"
    }

    enum class State {
        SET_START_ANGLE,
        WAITING_FOR_DROP,
        WAITING_FOR_POS,
        BATTING,
        DONE
    }

    companion object {
        fun Overhand(dropTime: Double) : Batting {
            return Batting(
                    initDropTime = dropTime,
                    initShoulderAngle = 210.0,
                    initElbowAngle = 55.0,
                    shoulderStop = 270.0,
                    elbowStop = 180.0,
                    elbowSwing = 1.0,
                    shoulderSwing = 1.0
            )
        }

        fun Underhand(dropTime: Double) : Batting {
            return Batting(
                    initDropTime = dropTime,
                    initShoulderAngle = 100.0,
                    initElbowAngle = 180.0,
                    shoulderStop = 270.0,
                    elbowStop = 280.0,
                    elbowSwing = 1.0,
                    shoulderSwing = 1.0
            )
        }
    }
}