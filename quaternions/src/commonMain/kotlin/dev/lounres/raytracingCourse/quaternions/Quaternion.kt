package dev.lounres.raytracingCourse.quaternions

import kotlin.math.sqrt


public data class Quaternion(
    val w: Double,
    val x: Double,
    val y: Double,
    val z: Double,
) {
    override fun toString(): String = "$w + $x i + $y j + $z k"

    public val norm: Double get() = w * w + x * x + y * y + z * z
    public fun normalized(): Quaternion {
        val absoluteValue = sqrt(norm)
        return Quaternion(
            w = w / absoluteValue,
            x = x / absoluteValue,
            y = y / absoluteValue,
            z = z / absoluteValue,
        )
    }

    public val reciprocal: Quaternion
        get() {
            val norm = norm
            return Quaternion(
                w = w / norm,
                x = -x / norm,
                y = -y / norm,
                z = -z / norm,
            )
        }
    
    public operator fun times(other: Quaternion): Quaternion =
        Quaternion(
            w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z,
            x = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y,
            y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x,
            z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w,
        )

    public operator fun div(other: Quaternion): Quaternion {
        val otherNorm = other.w * other.w + other.x * other.x + other.y * other.y + other.z * other.z
        return Quaternion(
            w = ( this.w * other.w + this.x * other.x + this.y * other.y + this.z * other.z) / otherNorm,
            x = (-this.w * other.x + this.x * other.w - this.y * other.z + this.z * other.y) / otherNorm,
            y = (-this.w * other.y + this.x * other.z + this.y * other.w - this.z * other.x) / otherNorm,
            z = (-this.w * other.z - this.x * other.y + this.y * other.x + this.z * other.w) / otherNorm,
        )
    }
}