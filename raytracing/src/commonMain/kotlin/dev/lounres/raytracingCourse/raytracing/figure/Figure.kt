package dev.lounres.raytracingCourse.raytracing.figure

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.geometry.Ray
import dev.lounres.raytracingCourse.raytracing.geometry.Rotation
import dev.lounres.raytracingCourse.raytracing.geometry.applyTo
import dev.lounres.raytracingCourse.raytracing.geometry.inverseApplyTo
import dev.lounres.raytracingCourse.raytracing.random.nextVectorOnUnitSphere
import kotlin.math.*
import kotlin.random.Random


private fun AABB.rotate(rotation: Rotation): AABB {
    val vertices = listOf(
        Vector(this.minX, this.minY, this.minZ),
        Vector(this.minX, this.minY, this.maxZ),
        Vector(this.minX, this.maxY, this.minZ),
        Vector(this.minX, this.maxY, this.maxZ),
        Vector(this.maxX, this.minY, this.minZ),
        Vector(this.maxX, this.minY, this.maxZ),
        Vector(this.maxX, this.maxY, this.minZ),
        Vector(this.maxX, this.maxY, this.maxZ),
    )
    val rotatedVertices = vertices.map { rotation.applyTo(it) }
    return AABB(
        minX = rotatedVertices.minOf { it.x },
        minY = rotatedVertices.minOf { it.y },
        minZ = rotatedVertices.minOf { it.z },
        maxX = rotatedVertices.maxOf { it.x },
        maxY = rotatedVertices.maxOf { it.y },
        maxZ = rotatedVertices.maxOf { it.z },
    )
}

private fun AABB.move(position: Point): AABB =
    AABB(
        minX = this.minX + position.x,
        minY = this.minY + position.y,
        minZ = this.minZ + position.z,
        maxX = this.maxX + position.x,
        maxY = this.maxY + position.y,
        maxZ = this.maxZ + position.z,
    )

public interface Figure {
    public fun intersect(ray: Ray): Double?
    public fun intersectAgain(incomingRay: Ray): Double?
    public fun outerNormalFor(position: Point): Vector
}

public interface FiniteFigure : Figure {
    public val AABB: AABB
    public fun probabilityDensityFor(ray: Ray): Double
    context(Random)
    public fun lightSourceSample(): Point
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
    override fun intersectAgain(incomingRay: Ray): Nothing? = null
    override fun outerNormalFor(position: Point): Vector = normal
}

public data class Ellipsoid(
    val rX: Double,
    val rY: Double,
    val rZ: Double,
    val position: Point,
    val rotation: Rotation
) : FiniteFigure {
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

        val discriminant = b * b - a * c
        if (discriminant <= 0.0) return null
        val discriminantSqrt = sqrt(discriminant)
        val root1 = (-b - discriminantSqrt) / a
        val root2 = (-b + discriminantSqrt) / a

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
        val relativePosition = rotation.inverseApplyTo(position - this.position)
        val correction = sqrt(relativePosition.x * relativePosition.x + relativePosition.y * relativePosition.y + relativePosition.z * relativePosition.z)
        val outerNormalVector = Vector(relativePosition.x / (rX * rX), relativePosition.y / (rY * rY), relativePosition.z / (rZ * rZ)) / correction
        return rotation.applyTo(outerNormalVector)
    }
    override fun probabilityDensityFor(ray: Ray): Double {
        val relativePosition = rotation.inverseApplyTo(ray.position - this.position)
        val relativeDirection = rotation.inverseApplyTo(ray.direction)
        
        val normalizedRelativePositionX = relativePosition.x / rX
        val normalizedRelativePositionY = relativePosition.y / rY
        val normalizedRelativePositionZ = relativePosition.z / rZ
        val normalizedRelativeDirectionX = relativeDirection.x / rX
        val normalizedRelativeDirectionY = relativeDirection.y / rY
        val normalizedRelativeDirectionZ = relativeDirection.z / rZ
        
        val a = normalizedRelativeDirectionX * normalizedRelativeDirectionX + normalizedRelativeDirectionY * normalizedRelativeDirectionY + normalizedRelativeDirectionZ * normalizedRelativeDirectionZ
        val b = normalizedRelativeDirectionX * normalizedRelativePositionX + normalizedRelativeDirectionY * normalizedRelativePositionY + normalizedRelativeDirectionZ * normalizedRelativePositionZ
        val c = normalizedRelativePositionX * normalizedRelativePositionX + normalizedRelativePositionY * normalizedRelativePositionY + normalizedRelativePositionZ * normalizedRelativePositionZ - 1.0
        
        val discriminant = b * b - a * c
        if (discriminant <= 0.0) return 0.0
        val discriminantSqrt = sqrt(discriminant)
        val root1 = (-b - discriminantSqrt) / a
        val root2 = (-b + discriminantSqrt) / a
        
        val directionVector1 = Vector(normalizedRelativePositionX + normalizedRelativeDirectionX * root1, normalizedRelativePositionY + normalizedRelativeDirectionY * root1, normalizedRelativePositionZ + normalizedRelativeDirectionZ * root1)
        val directionVector2 = Vector(normalizedRelativePositionX + normalizedRelativeDirectionX * root2, normalizedRelativePositionY + normalizedRelativeDirectionY * root2, normalizedRelativePositionZ + normalizedRelativeDirectionZ * root2)
        
        val radiusVector1 = Vector(directionVector1.x * rX, directionVector1.y * rY, directionVector1.z * rZ)
        val radiusVector2 = Vector(directionVector2.x * rX, directionVector2.y * rY, directionVector2.z * rZ)
        
        val localNormal1 = Vector(radiusVector1.x / (rX * rX), radiusVector1.y / (rY * rY), radiusVector1.z / (rZ * rZ))
        val localNormal2 = Vector(radiusVector2.x / (rX * rX), radiusVector2.y / (rY * rY), radiusVector2.z / (rZ * rZ))
        
        val probabilityDensity1 = 1 / (4 * PI) / sqrt(
            (directionVector1.x * rY * rZ).let { it * it } + (directionVector1.y * rZ * rX).let { it * it } + (directionVector1.z * rX * rY).let { it * it }
        ) * (relativePosition - radiusVector1).length.pow(3) / (abs((relativePosition - radiusVector1) dot localNormal1) / localNormal1.length)
        val probabilityDensity2 = 1 / (4 * PI) / sqrt(
            (directionVector2.x * rY * rZ).let { it * it } + (directionVector2.y * rZ * rX).let { it * it } + (directionVector2.z * rX * rY).let { it * it }
        ) * (relativePosition - radiusVector2).length.pow(3) / (abs((relativePosition - radiusVector2) dot localNormal2) / localNormal2.length)
        
        return (if (root1 > 0.0) probabilityDensity1 else 0.0) + (if (root2 > 0.0) probabilityDensity2 else 0.0)
    }
    context(Random)
    override fun lightSourceSample(): Point {
        val randomVectorOnDirectionsSphere = nextVectorOnUnitSphere()
        val radiusVector = Vector(
            randomVectorOnDirectionsSphere.x * rX,
            randomVectorOnDirectionsSphere.y * rY,
            randomVectorOnDirectionsSphere.z * rZ
        )
        return rotation.applyTo(radiusVector) + position
    }
    override val AABB: AABB =
        AABB(
            minX = -rX,
            minY = -rY,
            minZ = -rZ,
            maxX = rX,
            maxY = rY,
            maxZ = rZ,
        ).rotate(rotation).move(this.position)
}

public data class Box(
    val sizeX: Double,
    val sizeY: Double,
    val sizeZ: Double,
    val position: Point,
    val rotation: Rotation
) : FiniteFigure {
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
    
    override fun probabilityDensityFor(ray: Ray): Double {
        val relativePosition = rotation.inverseApplyTo(ray.position - position)
        val relativeDirection = rotation.inverseApplyTo(ray.direction)
        
        val t1X = -relativePosition.x / relativeDirection.x - abs(sizeX / relativeDirection.x)
        val t2X = -relativePosition.x / relativeDirection.x + abs(sizeX / relativeDirection.x)
        val t1Y = -relativePosition.y / relativeDirection.y - abs(sizeY / relativeDirection.y)
        val t2Y = -relativePosition.y / relativeDirection.y + abs(sizeY / relativeDirection.y)
        val t1Z = -relativePosition.z / relativeDirection.z - abs(sizeZ / relativeDirection.z)
        val t2Z = -relativePosition.z / relativeDirection.z + abs(sizeZ / relativeDirection.z)
        
        val t1 = max(max(t1X, t1Y), t1Z)
        val t2 = min(min(t2X, t2Y), t2Z)
        
        val pointAtT1 = relativePosition + relativeDirection * t1
        val pointAtT2 = relativePosition + relativeDirection * t2
        
        val normalAtT1 = when {
            t1 == t1X -> Vector(-1.0, 0.0, 0.0) * sign(relativeDirection.x)
            t1 == t1Y -> Vector(0.0, -1.0, 0.0) * sign(relativeDirection.y)
            else -> Vector(0.0, 0.0, -1.0) * sign(relativeDirection.z)
        }
        val normalAtT2 = when {
            t2 == t2X -> Vector(1.0, 0.0, 0.0) * sign(relativeDirection.x)
            t2 == t2Y -> Vector(0.0, 1.0, 0.0) * sign(relativeDirection.y)
            else -> Vector(0.0, 0.0, 1.0) * sign(relativeDirection.z)
        }
        
        val probabilityDensity1 = 1 / (8 * (sizeX * sizeY + sizeY * sizeZ + sizeZ * sizeX)) *
                (relativePosition - pointAtT1).length.pow(3) / (abs((relativePosition - pointAtT1) dot normalAtT1) / normalAtT1.length)
        val probabilityDensity2 = 1 / (4 * PI) / (8 * (sizeX * sizeY + sizeY * sizeZ + sizeZ * sizeX)) *
                (relativePosition - pointAtT2).length.pow(3) / (abs((relativePosition - pointAtT2) dot normalAtT2) / normalAtT2.length)

        return (if (t1 >= t2) 0.0 else (if (t1 > 0.0) probabilityDensity1 else 0.0) + (if (t2 > 0.0) probabilityDensity2 else 0.0))
    }
    
    context(Random)
    override fun lightSourceSample(): Point {
        val u = nextDouble(sizeX * sizeY + sizeY * sizeZ + sizeZ * sizeX)
        val relativePoint = when {
            u < sizeY * sizeZ -> Vector(if (nextBoolean()) sizeX else -sizeX, sizeY * nextDouble(-1.0, 1.0), sizeZ * nextDouble(-1.0, 1.0))
            u < sizeY * sizeZ + sizeX * sizeZ -> Vector(sizeX * nextDouble(-1.0, 1.0), if (nextBoolean()) sizeY else -sizeY, sizeZ * nextDouble(-1.0, 1.0))
            else -> Vector(sizeX * nextDouble(-1.0, 1.0), sizeY * nextDouble(-1.0, 1.0), if (nextBoolean()) sizeZ else -sizeZ)
        }
        return rotation.applyTo(relativePoint) + position
    }
    
    override val AABB: AABB =
        AABB(
            minX = -sizeX,
            minY = -sizeY,
            minZ = -sizeZ,
            maxX = sizeX,
            maxY = sizeY,
            maxZ = sizeZ,
        ).rotate(rotation).move(position)
}

public data class Triangle(
    val vertex0: Point,
    val vertex1: Point,
    val vertex2: Point,
) : FiniteFigure {
    val normal: Vector = (vertex1 - vertex0) cross (vertex2 - vertex0)
    override fun intersect(ray: Ray): Double? {
        val column0 = vertex1 - vertex0
        val column1 = vertex2 - vertex0
        val column2 = -ray.direction
        val resultColumn = ray.position - vertex0
        val det = column0.x * (column1.y * column2.z - column1.z * column2.y) +
                column0.y * (column1.z * column2.x - column1.x * column2.z) +
                column0.z * (column1.x * column2.y - column1.y * column2.x)
        val u = (resultColumn.x * (column1.y * column2.z - column1.z * column2.y) +
                resultColumn.y * (column1.z * column2.x - column1.x * column2.z) +
                resultColumn.z * (column1.x * column2.y - column1.y * column2.x)) / det
        val v = (resultColumn.x * (column2.y * column0.z - column2.z * column0.y) +
                resultColumn.y * (column2.z * column0.x - column2.x * column0.z) +
                resultColumn.z * (column2.x * column0.y - column2.y * column0.x)) / det
        val t = (resultColumn.x * (column0.y * column1.z - column0.z * column1.y) +
                resultColumn.y * (column0.z * column1.x - column0.x * column1.z) +
                resultColumn.z * (column0.x * column1.y - column0.y * column1.x)) / det
        return if (u > 0.0 && v > 0.0 && u + v < 1.0 && t > 0.0) t else null
    }
    override fun intersectAgain(incomingRay: Ray): Nothing? = null
    override fun outerNormalFor(position: Point): Vector = normal
    override fun probabilityDensityFor(ray: Ray): Double {
        val column0 = vertex1 - vertex0
        val column1 = vertex2 - vertex0
        val column2 = -ray.direction
        val resultColumn = ray.position - vertex0
        val det = column0.x * (column1.y * column2.z - column1.z * column2.y) +
                column0.y * (column1.z * column2.x - column1.x * column2.z) +
                column0.z * (column1.x * column2.y - column1.y * column2.x)
        val u = (resultColumn.x * (column1.y * column2.z - column1.z * column2.y) +
                resultColumn.y * (column1.z * column2.x - column1.x * column2.z) +
                resultColumn.z * (column1.x * column2.y - column1.y * column2.x)) / det
        val v = (resultColumn.x * (column2.y * column0.z - column2.z * column0.y) +
                resultColumn.y * (column2.z * column0.x - column2.x * column0.z) +
                resultColumn.z * (column2.x * column0.y - column2.y * column0.x)) / det
        val t = (resultColumn.x * (column0.y * column1.z - column0.z * column1.y) +
                resultColumn.y * (column0.z * column1.x - column0.x * column1.z) +
                resultColumn.z * (column0.x * column1.y - column0.y * column1.x)) / det
        val distanceVector = ray.direction * t
        return if (u > 0.0 && v > 0.0 && u + v < 1.0 && t > 0.0)
            2 / (column0 cross column1).length * distanceVector.length.pow(3) / (abs(distanceVector dot normal) / normal.length)
        else 0.0
    }
    context(Random)
    override fun lightSourceSample(): Point {
        val u = nextDouble()
        val v = nextDouble()
        return if (u + v <= 1.0) vertex0 + (vertex1 - vertex0) * u + (vertex2 - vertex0) * v
        else vertex0 + (vertex1 - vertex0) * (1.0 - u) + (vertex2 - vertex0) * (1.0 - v)
    }
    override val AABB: AABB =
        AABB(
            minX = doubleArrayOf(vertex0.x, vertex1.x, vertex2.x).min(),
            minY = doubleArrayOf(vertex0.y, vertex1.y, vertex2.y).min(),
            minZ = doubleArrayOf(vertex0.z, vertex1.z, vertex2.z).min(),
            maxX = doubleArrayOf(vertex0.x, vertex1.x, vertex2.x).max(),
            maxY = doubleArrayOf(vertex0.y, vertex1.y, vertex2.y).max(),
            maxZ = doubleArrayOf(vertex0.z, vertex1.z, vertex2.z).max(),
        )
}