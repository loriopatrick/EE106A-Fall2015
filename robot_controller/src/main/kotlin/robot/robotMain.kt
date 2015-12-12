package robot

import jssc.SerialPort
import java.io.*

/**
 * @author plorio
 */

fun get() : Batting {
    return presets2[7].toBat()
}

fun main(args: Array<String>) {
    val port = SerialPort("/dev/cu.usbmodem14211")
    port.openPort()
    port.setParams(115200, 8, 1, 0)

    val controller = RobotInterface(port)
    var batting = get()

    var kill = true

    var writer = FileWriter("robot-${System.currentTimeMillis()}.csv")
    writer.write("time,elbow_angle,shoulder_angle,elbow_current,shoulder_current\n")
//            writer.write("${state.time},${state.deltaTime},${state.elbowAngle}," +
//                    "${state.elbowVelocity},$torque,${state.elbowCurrent}\n")

    val program = RobotProgram(controller) { program, state ->
        batting.update(program, state)

        if (kill) {
            program.kill()
        } else {
            writer.write("${state.time},${state.elbowAngle},${state.shoulderAngle}," +
                    "${state.elbowCurrent},${state.shoulderCurrent}\n")
        }
    }

    val reader = BufferedReader(InputStreamReader(System.`in`))
    Thread() {
        while (true) {
            try {
                val split = reader.readLine().split(" ")
                if (split[0].equals("r")) {
                    batting = get()
                } else if (split[0].equals("d")) {
                    writer.write("start\n")
                    batting.drop()
                } else if (split[0].equals("o")) {
                    batting = get()
                    kill = false
                    println("ON")
                } else if (split[0].equals("k")) {
                    kill = true
                } else if (split[0].equals("i")) {
                    println(program.state)
                }
            } catch (e: Exception) {
            }
        }
    }.start()

    program.start()
}