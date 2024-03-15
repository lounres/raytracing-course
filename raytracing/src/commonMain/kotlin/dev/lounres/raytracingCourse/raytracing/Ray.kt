package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.euclideanGeometry.plus
import dev.lounres.raytracingCourse.euclideanGeometry.times


public data class Ray(
    val position: Point,
    val direction: Vector
) {
    public fun atMoment(moment: Double): Point = position + direction * moment
}