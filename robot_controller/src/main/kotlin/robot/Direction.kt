package robot

/**
 * @author plorio
 */

enum class Direction(val value: Byte) {
    CLOCKWISE(1),
    COUNTER_CLOCKWISE(2),
    STATIC(3),
    HOLD(4)
}