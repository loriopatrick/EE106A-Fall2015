package camera

import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

/**
 * @author plorio
 */

fun main(args: Array<String>) {
    val jFrame = JFrame("setup")
    jFrame.setSize(800, 600)
    jFrame.isVisible = true
    val jLabel = JLabel()
    jFrame.contentPane = jLabel
    val cameraTarget = CameraTarget(cameraId = 1, updateImage = {
        jLabel.icon = ImageIcon(it.image)
        jLabel.repaint()
    })
    cameraTarget.start()
}