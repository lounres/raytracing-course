package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.euclideanGeometry.minus
import kotlin.math.*


public sealed interface Figure {
    public infix fun intersect(ray: Ray): Double?
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
}

public data class Ellipsoid(val rX: Double, val rY: Double, val rZ: Double, val position: Point, val rotation: Rotation) :
    Figure {
    override fun intersect(ray: Ray): Double? {
        val (pX, pY, pZ) = rotation.inverseApplyTo(ray.position - position)
        val (dX, dY, dZ) = rotation.inverseApplyTo(ray.direction)

        val normalizedPX = pX / rX
        val normalizedPY = pY / rY
        val normalizedPZ = pZ / rZ
        val normalizedDX = dX / rX
        val normalizedDY = dY / rY
        val normalizedDZ = dZ / rZ

        val a = normalizedDX * normalizedDX + normalizedDY * normalizedDY + normalizedDZ * normalizedDZ
        val b = (normalizedDX * normalizedPX + normalizedDY * normalizedPY + normalizedDZ * normalizedPZ)
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
}