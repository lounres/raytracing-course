package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.plus
import dev.lounres.raytracingCourse.euclideanGeometry.times


public fun Camera.rayForPixel(x: UInt, y: UInt): Ray =
    Ray(
        position = this.position,
        direction =
            right * (((2 * x.toInt() + 1) / imageWidth.toDouble() - 1) * fovXTan) +
            up * ((2 * y.toInt() + 1) / imageHeight.toDouble() - 1) * fovYTan +
            forward
    )

public typealias Scene = Set<SceneObject>

public data class Intersection(val moment: Double, val color: Color)

public infix fun Scene.intersect(ray: Ray): Intersection? {
    var closestIntersection: Intersection? = null
    for (obj in this) {
        val intersectionMoment = obj.figure intersect ray
        if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment))
            closestIntersection = Intersection(moment = intersectionMoment, color = obj.color)
    }
    return closestIntersection
}

public fun Scene.trace(ray: Ray, backgroundColor: Color): Color {
    return (this intersect ray)?.color ?: backgroundColor
}