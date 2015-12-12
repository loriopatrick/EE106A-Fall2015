package robot

import jssc.SerialPort
import java.nio.ByteBuffer
import java.util.*

/**
 * @author plorio
 */
class RobotInterface(val serial: SerialPort) {
    var shoulderCurrent = 0.toByte()
    var shoulderDirection = Direction.STATIC
    var elbowCurrent = 0.toByte()
    var elbowDirection = Direction.STATIC
    var drop = false

    fun write() {
        val data = ByteArray(8)
        data[0] = 0

        data[1] = shoulderDirection.value
        if (shoulderCurrent == 0.toByte()) {
            data[2] = 1
            data[3] = 1
        } else {
            data[2] = 2
            data[3] = shoulderCurrent
        }

        data[4] = elbowDirection.value
        if (elbowCurrent == 0.toByte()) {
            data[5] = 1
            data[6] = 1
        } else {
            data[5] = 2
            data[6] = elbowCurrent
        }

        data[7] = if (drop) 1 else 2

        serial.writeBytes(data)
    }


    fun read(): Pair<JointState, JointState> {
        var zeros = 0

        while (zeros < 5) {
            if (serial.readBytes(1)[0] == 0.toByte()) {
                zeros += 1
            } else {
                zeros = 0
            }
        }

        val wrap = ByteBuffer.wrap(serial.readBytes(14))
        val time = getLong(wrap)
        wrap.get()
        val shoulderAngle = getInt(wrap) % 360
        val shoulderCurrent = Math.round((getInt(wrap) - 402.0) / 10.200) / 100.0
        wrap.get()
        val elbowAngle = getInt(wrap) % 360
        // -8 + mcur*1.25*16/1024
        // 0 -> -13
        // 4 -> 13
        val elbowCurrent = (getInt(wrap) - 204.8 - 819.2 / 2.0) * 26.0 / 819.2

        return Pair(
                JointState(time, shoulderAngle.toDouble(), shoulderCurrent),
                JointState(time, elbowAngle.toDouble(), elbowCurrent)
        )
    }

    private fun getLong(buf: ByteBuffer): Long {
        val b1 = java.lang.Byte.toUnsignedInt(buf.get()).toLong()
        val b2 = java.lang.Byte.toUnsignedInt(buf.get()).toLong()
        val b3 = java.lang.Byte.toUnsignedInt(buf.get()).toLong()
        val b4 = java.lang.Byte.toUnsignedInt(buf.get()).toLong()

        return b1 +
                b2 * 256 +
                b3 * 256 * 256 +
                b4 * 256 * 256 * 256 * 256
    }

    private fun getInt(buf: ByteBuffer): Int {
        val b1 = java.lang.Byte.toUnsignedInt(buf.get())
        val b2 = java.lang.Byte.toUnsignedInt(buf.get())

        return b1 + b2 * 256
    }
}