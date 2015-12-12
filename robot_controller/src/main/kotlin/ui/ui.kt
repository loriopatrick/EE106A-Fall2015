package ui

import camera.Camera
import camera.CameraTarget
import jssc.SerialPort
import robot.Batting
import robot.RobotInterface
import robot.RobotProgram
import robot.getBestBatting
import java.io.FileWriter
import javax.swing.JFrame

/**
 * @author plorio
 */

fun main(args: Array<String>) {
    val port = SerialPort("/dev/cu.usbmodem14211")
    port.openPort()
    port.setParams(115200, 8, 1, 0)

    val controller = RobotInterface(port)

    var state = "Target"

    var batting: Batting? = null
    var batHeight: Double = 0.0

    val robotUI = RobotUI({
        if (state.equals("Target")) {
            state = "Preparing"
            batting = getBestBatting(batHeight)
        } else if (state.equals("Fire")) {
            batting?.drop()
        }
    })

    robotUI.setSize(900, 500)
    robotUI.isResizable = false

//    val cameraView = Camera(2)

    val cameraTarget = CameraTarget(0, {
        robotUI.setVideo1(it.image)
//        robotUI.setVideo2(cameraView.takePhoto())
        robotUI.setAngles("height: ${Math.round(it.targetHeight ?: 0.0)} cm")
        if (it.targetHeight != null) {
            batHeight = it.targetHeight ?: 0.0
        }
        if (state.equals("Target")) {
            robotUI.enableDrop(it.targetHeight != null)
        } else if (state.equals("Preparing")) {
            robotUI.enableDrop(false)
            if (batting?.mode == Batting.State.WAITING_FOR_DROP) {
                state = "Fire"
                robotUI.enableDrop(true)
            }
        }

        if (state.equals("Fire") && batting?.mode == Batting.State.DONE) {
            state = "Target"
        }
        robotUI.setState(state)
        robotUI.draw();
    })

    cameraTarget.start()

    val program = RobotProgram(controller) { program, state ->
        batting?.update(program, state)
    }

    program.start()
}