package robot

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * @author plorio
 */

class TimeSolver(val samples: List<TimeSolver.Sample>) {
    data class Sample(val deltaTime: Double, val targetHeight: Double)

    fun getBestTime(targetHeight: Double) : Double {
        return samples.minBy { Math.abs(it.targetHeight - targetHeight) }?.deltaTime ?: throw RuntimeException()
    }

    companion object {
        fun Load(file: File) : TimeSolver {
            val samples = ArrayList<Sample>()
            val reader = BufferedReader(FileReader(file))
            var line: String? = reader.readLine()
            while (line != null) {
                val parts = line.split(",")
                samples.add(Sample(parts[0].toDouble(), parts[1].toDouble()))
                line = reader.readLine()
            }
            return TimeSolver(samples)
        }
    }
}