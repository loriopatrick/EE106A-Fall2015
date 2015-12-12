package data

import jssc.SerialPort
import robot.Batting
import robot.RobotInterface
import robot.RobotProgram
import robot.get
import java.io.BufferedReader
import java.io.FileWriter
import java.io.InputStreamReader

/**
 * @author plorio
 */

fun main(args: Array<String>) {
    val port = SerialPort("/dev/cu.usbmodem14211")
    port.openPort()
    port.setParams(115200, 8, 1, 0)

    val controller = RobotInterface(port)

    val over = false

    var writer = FileWriter("robot-${if (over) "over" else "under"}-${System.currentTimeMillis()}.csv")
    writer.write("time,elbow_angle,shoulder_angle,elbow_current,shoulder_current\n")

    val batting = if (over) Batting.Overhand(0.0) else Batting.Underhand(0.0)
    var done = false

    val program = RobotProgram(controller) { program, state ->
        batting.update(program, state)

        if (batting.mode == Batting.State.WAITING_FOR_DROP) {
            batting.drop()
        }
        else if (batting.mode == Batting.State.BATTING) {
            writer.write("${state.time},${state.elbowAngle},${state.shoulderAngle}," +
                    "${state.elbowCurrent},${state.shoulderCurrent}\n")
        } else if (batting.mode == Batting.State.DONE) {
            program.kill()
            if (!done) {
                writer.close()
                done = true
            }
            println("DONE")
        }
    }

    program.start()
}