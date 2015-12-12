package camera

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * @author plorio
 */
class CameraTarget(val cameraId: Int = 1, val updateImage: (CameraTarget) -> Unit = {}) {
    init {
        Runtime.getRuntime().load("/Users/plorio/Dropbox (Personal)/ee106a/robot_java/lib/libopencv_java300.dylib")
    }

    private var frame: Mat? = null
    private var running = false

    var targetHeight: Double? = null

    val size = Size(800.0, 600.0)

    var image: BufferedImage = BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB)
        private set

    fun start() {
        running = true

        val blurSize = 21.0
        val sensorArea = Rect(Point(300.0, 0.0), Point(550.0, 600.0))

        Thread({
            val camera = VideoCapture(cameraId)
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 100.0)
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 0.0)

            val mat = Mat()
            val reference = Mat()
            frame = Mat()

            camera.grab()
            camera.read(reference)
            Imgproc.resize(reference, reference, size)
            Imgproc.GaussianBlur(reference, reference, Size(blurSize, blurSize), 0.0);


            while (running) {
                camera.grab()
                camera.read(frame)
                Imgproc.resize(frame, frame, size)

                Imgproc.GaussianBlur(frame, mat, Size(blurSize, blurSize), 0.0);
                Core.absdiff(reference, mat, mat)
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
                Imgproc.threshold(mat, mat, 70.0, 255.0, Imgproc.THRESH_BINARY)

                Imgproc.rectangle(frame, sensorArea.tl(), sensorArea.br(), Scalar(0.0, 255.0, 0.0), 5)

                val item = detect(mat).filter {
                    sensorArea.contains(it.tl()) || sensorArea.contains(it.br())
                }.maxBy {
                    it.area()
                }

                if (item != null) {
                    Imgproc.rectangle(frame, item.tl(), item.br(), Scalar(255.0, 0.0, 0.0), 5)
                    targetHeight = 179 + 6.8 + 3.8 - ((item.y + item.height / 2.0) * 0.2680401681 - 15.18347252)
                } else {
                    targetHeight = null
                }

                Imgproc.resize(frame, frame, Size(400.0, 300.0))
                val matOfByte = MatOfByte()
                Imgcodecs.imencode(".jpg", frame, matOfByte)
                val bytes = matOfByte.toArray();
                image = ImageIO.read(ByteArrayInputStream(bytes));
                updateImage(this)
            }

            camera.release()
            running = false
        }).start()
    }

    private fun detect(mat: Mat): List<Rect> {
        val high = Mat()
        val contours = ArrayList<MatOfPoint>();
        Imgproc.findContours(mat.clone(), contours, high, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        high.release()
        return contours.map { Imgproc.boundingRect(it) }
    }

    fun stop() {
        running = false
    }
}