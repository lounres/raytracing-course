package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*
import kotlin.math.*


public data class FigureIntersection(
    val moment: Double,
    val normal: Vector,
)

public interface Figure {
    public infix fun intersect(ray: Ray): Double?
    public infix fun intersectFromSurface(incomingRay: Ray): Double?
    public fun normalAt(incomingRay: Ray): Vector
}

public data class Plane(val normal: Vector, val valueAtZero: Double) : Figure {
    override fun intersect(ray: Ray): Double? {
        val (pX, pY, pZ) = ray.position
        val (dX, dY, dZ) = ray.direction
        val (nX, nY, nZ) = this.normal
        val v = this.valueAtZero

        val result = (v - (pX * nX + pY * nY + pZ * nZ)) / (dX * nX + dY * nY + dZ * nZ)

        return if (result <= 0.0) null else result
    }
    override fun intersectFromSurface(incomingRay: Ray): Double? = null
    override fun normalAt(incomingRay: Ray): Vector = if (normal dot incomingRay.direction <= .0) normal else -normal
}

public data class Ellipsoid(val rX: Double, val rY: Double, val rZ: Double, val position: Point, val rotation: Rotation) : Figure {
    override fun intersect(ray: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(ray.position - this.position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(ray.direction)

        val normalizedPX = pX / rX
        val normalizedPY = pY / rY
        val normalizedPZ = pZ / rZ
        val normalizedDX = dX / rX
        val normalizedDY = dY / rY
        val normalizedDZ = dZ / rZ

        val a = normalizedDX * normalizedDX + normalizedDY * normalizedDY + normalizedDZ * normalizedDZ
        val b = normalizedDX * normalizedPX + normalizedDY * normalizedPY + normalizedDZ * normalizedPZ
        val c = normalizedPX * normalizedPX + normalizedPY * normalizedPY + normalizedPZ * normalizedPZ - 1.0

        val discriminant = sqrt(b * b - a * c)
        val root1 = (-b - discriminant) / a
        val root2 = (-b + discriminant) / a

        return when {
            root1 > 0.0 -> root1
            root2 > 0.0 -> root2
            else -> null
        }
    }
    override fun intersectFromSurface(incomingRay: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(incomingRay.position - this.position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(incomingRay.direction)

        val normalizedPX = pX / rX
        val normalizedPY = pY / rY
        val normalizedPZ = pZ / rZ
        val normalizedDX = dX / rX
        val normalizedDY = dY / rY
        val normalizedDZ = dZ / rZ

        val a = normalizedDX * normalizedDX + normalizedDY * normalizedDY + normalizedDZ * normalizedDZ
        val b = normalizedDX * normalizedPX + normalizedDY * normalizedPY + normalizedDZ * normalizedPZ

        return (-2 * b / a).let { if (it <= 0.0) null else it }
    }
    override fun normalAt(incomingRay: Ray): Vector =
        (incomingRay.position - this.position)
            .let { Vector(it.x / (rX * rX), it.y / (rY * rY), it.z / (rZ * rZ)) }
            .let { if (it dot incomingRay.direction >= 0.0) -it else it }
}

public data class Box(val sizeX: Double, val sizeY: Double, val sizeZ: Double, val position: Point, val rotation: Rotation) : Figure {
    override fun intersect(ray: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(ray.position - position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(ray.direction)

        val t1X = -pX / dX - abs(sizeX / dX)
        val t2X = -pX / dX + abs(sizeX / dX)
        val t1Y = -pY / dY - abs(sizeY / dY)
        val t2Y = -pY / dY + abs(sizeY / dY)
        val t1Z = -pZ / dZ - abs(sizeZ / dZ)
        val t2Z = -pZ / dZ + abs(sizeZ / dZ)

        val t1 = max(max(t1X, t1Y), t1Z)
        val t2 = min(min(t2X, t2Y), t2Z)

        return when {
            t1 >= t2 -> null
            t1 > 0.0 -> t1
            t2 > 0.0 -> t2
            else -> null
        }
    }
    override fun intersectFromSurface(incomingRay: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(incomingRay.position - position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(incomingRay.direction)

        val t1X = -pX / dX - abs(sizeX / dX)
        val t2X = -pX / dX + abs(sizeX / dX)
        val t1Y = -pY / dY - abs(sizeY / dY)
        val t2Y = -pY / dY + abs(sizeY / dY)
        val t1Z = -pZ / dZ - abs(sizeZ / dZ)
        val t2Z = -pZ / dZ + abs(sizeZ / dZ)

        val t1 = max(max(t1X, t1Y), t1Z)
        val t2 = min(min(t2X, t2Y), t2Z)

        return (if (t1 + t2 > 0.0) t2 else t1).let { if (it <= 0.0) null else it }
    }
    override fun normalAt(incomingRay: Ray): Vector {
        val (pX, pY, pZ) = rotation.inverseApplyTo(incomingRay.position - position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(incomingRay.direction)

        val t1X = -pX / dX - abs(sizeX / dX)
        val t2X = -pX / dX + abs(sizeX / dX)
        val t1Y = -pY / dY - abs(sizeY / dY)
        val t2Y = -pY / dY + abs(sizeY / dY)
        val t1Z = -pZ / dZ - abs(sizeZ / dZ)
        val t2Z = -pZ / dZ + abs(sizeZ / dZ)

        val t1 = max(max(t1X, t1Y), t1Z)
        val t1Normal = when (t1) {
            t1X -> Vector(1.0, 0.0, 0.0)
            t1Y -> Vector(0.0, 1.0, 0.0)
            t1Z -> Vector(0.0, 0.0, 1.0)
            else -> error("For some reason could not find normal at incoming ray $incomingRay with plane $this")
        }
        val t2 = min(min(t2X, t2Y), t2Z)
        val t2Normal = when (t2) {
            t2X -> Vector(1.0, 0.0, 0.0)
            t2Y -> Vector(0.0, 1.0, 0.0)
            t2Z -> Vector(0.0, 0.0, 1.0)
            else -> error("For some reason could not find normal at incoming ray $incomingRay with plane $this")
        }

        return (if (t1 + t2 > 0.0) t1 else t2).let {
            when {
                it == t1 -> t1Normal
                it == t2 -> t2Normal
                else -> error("For some reason could not find normal at incoming ray $incomingRay with plane $this")
            }
        }.let { if (it dot incomingRay.direction > 0.0) -it else it }
    }
}