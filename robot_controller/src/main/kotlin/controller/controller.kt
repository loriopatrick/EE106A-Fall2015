//package controller
//
//import jssc.SerialPort
//import java.io.BufferedReader
//import java.io.FileWriter
//import java.io.InputStreamReader
//
///**
// * @author plorio
// */
//
//
//fun main(args: Array<String>) {
//    val port = SerialPort("/dev/cu.usbmodem14111")
//    port.openPort()
//    port.setParams(115200, 8, 1, 0)
//
//    val controller = RobotInterface(port)
//    var batting = Batting.Underhand(-0.35)
//
//    var kill = true
//
//    var writer = FileWriter("robot-${System.currentTimeMillis()}.csv")
//    writer.write("time,elbow_angle,shoulder_angle,elbow_current,shoulder_current\n")
//    //        writer.write("${state.time},${state.deltaTime},${state.elbowAngle}," +
//    //                "${state.elbowVelocity},$torque,${state.elbowCurrent}\n")
//
//    val program = RobotProgram(controller) { program, state ->
//        batting.update(program, state)
//
//        if (kill) {
//            program.kill()
//        } else {
//            writer.write("${state.time},${state.elbowAngle},${state.shoulderAngle}," +
//                    "${state.elbowCurrent},${state.shoulderCurrent}\n")
//        }
//    }
//
//
//
//
//    val reader = BufferedReader(InputStreamReader(System.`in`))
//    Thread() {
//        while (true) {
//            try {
//                val split = reader.readLine().split(" ")
//                if (split[0].equals("r")) {
//                    batting = Batting.Underhand(-0.35)
//                } else if (split[0].equals("d")) {
//                    batting.drop()
//                } else if (split[0].equals("o")) {
//                    batting = Batting.Underhand(-0.35)
//                    kill = false
//                } else if (split[0].equals("k")) {
//                    kill = true
//                } else if (split[0].equals("i")) {
//                    println(program.state)
//                }
//            } catch (e: Exception) {
//            }
//        }
//    }.start()
//
//    program.start()
//}