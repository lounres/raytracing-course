package dev.lounres.raytracingCourse.raytracing.figure

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.minus
import dev.lounres.raytracingCourse.raytracing.geometry.Ray
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


public data class AABB(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
) {
    init {
        require(minX <= maxX && minY <= maxY && minZ <= maxZ)
    }
    public val center: Point = Point((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2)
    public val surfaceArea: Double = (maxX - minX) * (maxY - minY) + (maxZ - minZ) * (maxX - minX) + (maxY - minY) * (maxZ - minZ)
}

public infix fun AABB.and(other: AABB): AABB =
    AABB(
        minX = min(this.minX, other.minX),
        minY = min(this.minY, other.minY),
        minZ = min(this.minZ, other.minZ),
        maxX = max(this.maxX, other.maxX),
        maxY = max(this.maxY, other.maxY),
        maxZ = max(this.maxZ, other.maxZ),
    )

public infix fun Ray.intersects(aabb: AABB): Boolean {
    val (pX, pY, pZ) = this.position - aabb.center
    val (dX, dY, dZ) = this.direction
    
    val t1X = -pX / dX - abs((aabb.maxX - aabb.minX) / dX)
    val t2X = -pX / dX + abs((aabb.maxX - aabb.minX) / dX)
    val t1Y = -pY / dY - abs((aabb.maxY - aabb.minY) / dY)
    val t2Y = -pY / dY + abs((aabb.maxY - aabb.minY) / dY)
    val t1Z = -pZ / dZ - abs((aabb.maxZ - aabb.minZ) / dZ)
    val t2Z = -pZ / dZ + abs((aabb.maxZ - aabb.minZ) / dZ)
    
    val t1 = max(max(t1X, t1Y), t1Z)
    val t2 = min(min(t2X, t2Y), t2Z)
    
    return when {
        t1 >= t2 -> false
        t1 > 0.0 -> true
        t2 > 0.0 -> true
        else -> false
    }
}