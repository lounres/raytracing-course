package dev.lounres.raytracingCourse.euclideanGeometry

import kotlin.math.sqrt


public data class Vector(val x: Double, val y: Double, val z: Double)
public data class Point(val x: Double, val y: Double, val z: Double)

public val Vector.length: Double get() = sqrt(x * x + y * y + z * z)
public val Vector.norm: Double get() = x * x + y * y + z * z
public fun Vector.normalized(): Vector = Vector(this.x / length, this.y / length, this.z / length)

public operator fun Vector.unaryMinus(): Vector = Vector(-this.x, -this.y, -this.z)

public operator fun Vector.plus(other: Vector): Vector = Vector(this.x + other.x, this.y + other.y, this.z + other.z)

public operator fun Vector.minus(other: Vector): Vector = Vector(this.x - other.x,  this.y - other.y, this.z - other.z)

public operator fun Vector.times(other: Double): Vector = Vector(this.x * other, this.y * other, this.z * other)

public operator fun Vector.div(other: Double): Vector = Vector(this.x / other, this.y / other, this.z / other)

public operator fun Point.plus(other: Vector): Point = Point(this.x + other.x, this.y + other.y, this.z + other.z)

public operator fun Vector.plus(other: Point): Point = Point(this.x + other.x, this.y + other.y, this.z + other.z)

public operator fun Point.minus(other: Point): Vector = Vector(this.x - other.x, this.y - other.y, this.z - other.z)

public infix fun Vector.dot(other: Vector): Double = x * other.x + y * other.y + z * other.z

public infix fun Vector.cross(other: Vector): Vector =
    Vector(
        this.y * other.z - this.z * other.y,
        this.z * other.x - this.x * other.z,
        this.x * other.y - this.y * other.x
    )