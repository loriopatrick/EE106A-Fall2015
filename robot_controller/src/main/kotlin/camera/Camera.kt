package camera

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * @author plorio
 */
class Camera(val cameraId: Int) {
    init {
        Runtime.getRuntime().load("/Users/plorio/Dropbox (Personal)/ee106a/robot_java/lib/libopencv_java300.dylib")
    }

    private val camera = VideoCapture(cameraId)
    private val mat = Mat()

    init {
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 200.0)
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 150.0)
    }

    fun takePhoto(): BufferedImage {
        camera.grab()
        camera.read(mat)

        Imgproc.resize(mat, mat, Size(400.0, 300.0))
        val matOfByte = MatOfByte()
        Imgcodecs.imencode(".jpg", mat, matOfByte)
        val bytes = matOfByte.toArray();
        var image = ImageIO.read(ByteArrayInputStream(bytes));

        return image
    }
}