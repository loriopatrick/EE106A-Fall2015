package robot

/**
 * @author plorio
 */

class VelocityCalculator(val size: Int) {
    private var idx = 0
    private val vs = Array(size, { 0.0 });

    var vel = 0.0
    var setup = false
    private set

    fun calc(vel: Double): Double {
        if (idx == size) {
            setup = true
            idx = 0
        }

        val thres = Math.abs((vel) / (vel + this.vel + 10))
//
//        println("${thres} : $vel")
//        if (setup && thres >= 0.9) {
//            return this.vel
//        }

        vs[idx++] = vel

        this.vel = vs.sum() / vs.size.toDouble()
//        return vel
        return this.vel

//        this.vel = avg / items
//        return this.vel
    }
}