package dev.lounres.raytracingCourse.euclideanGeometry

import kotlin.math.sqrt


public data class Vector(var x: Double, var y: Double, var z: Double)
public data class Point(val x: Double, val y: Double, var z: Double)

public operator fun Vector.unaryMinus(): Vector = Vector(-this.x, -this.y, -this.z)

public operator fun Vector.plus(other: Vector): Vector = Vector(this.x + other.x, this.y + other.y, this.z + other.z)

public operator fun Vector.minus(other: Vector): Vector = Vector(this.x - other.x,  this.y - other.y, this.z - other.z)

public operator fun Vector.times(other: Double): Vector = Vector(this.x * other, this.y * other, this.z * other)

public operator fun Vector.div(other: Double): Vector = Vector(this.x / other, this.y / other, this.z / other)

public operator fun Point.plus(other: Vector): Point = Point(this.x + other.x, this.y + other.y, this.z + other.z)

public operator fun Point.minus(other: Point): Vector = Vector(this.x - other.x, this.y - other.y, this.z - other.z)

public val Vector.length: Double get() = sqrt(x * x + y * y + z * z)

public infix fun Vector.dot(other: Vector): Double = x * other.x + y * other.y + z * other.z