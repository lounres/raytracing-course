package dev.lounres.raytracingCourse.raytracing.figure

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.Ray
import dev.lounres.raytracingCourse.raytracing.Rotation
import dev.lounres.raytracingCourse.raytracing.applyTo
import dev.lounres.raytracingCourse.raytracing.inverseApplyTo
import kotlin.math.*


public data class FigureIntersection(
    val moment: Double,
    val normal: Vector,
)

public interface Figure {
    public fun intersect(ray: Ray): Double?
    public fun intersectAgain(incomingRay: Ray): Double?
    public fun outerNormalFor(position: Point): Vector
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
    override fun intersectAgain(incomingRay: Ray): Double? = null
    override fun outerNormalFor(position: Point): Vector = normal
}

public data class Ellipsoid(val rX: Double, val rY: Double, val rZ: Double, val position: Point, val rotation: Rotation) :
    Figure {
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
    override fun intersectAgain(incomingRay: Ray): Double? {
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
    override fun outerNormalFor(position: Point): Vector {
        val relativePosition = position - this.position
        val correction = sqrt(relativePosition.x * relativePosition.x + relativePosition.y * relativePosition.y + relativePosition.z * relativePosition.z)
        val outerNormalVector = Vector(relativePosition.x / (rX * rX), relativePosition.y / (rY * rY), relativePosition.z / (rZ * rZ)) / correction
        return outerNormalVector
    }
}

public data class Box(val sizeX: Double, val sizeY: Double, val sizeZ: Double, val position: Point, val rotation: Rotation) :
    Figure {
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
    override fun intersectAgain(incomingRay: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(incomingRay.position - this.position)
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
    override fun outerNormalFor(position: Point): Vector {
        val (pX, pY, pZ) = rotation.inverseApplyTo(position - this.position)

        val tX = abs(sizeX - abs(pX))
        val tY = abs(sizeY - abs(pY))
        val tZ = abs(sizeZ - abs(pZ))

        val rotatedNormalVector = when {
            tX <= tY && tX <= tZ -> Vector(1.0, 0.0, 0.0) * sign(pX)
            tY <= tZ -> Vector(0.0, 1.0, 0.0) * sign(pY)
            else -> Vector(0.0, 0.0, 1.0) * sign(pZ)
        }

        return rotation.applyTo(rotatedNormalVector)
    }
}