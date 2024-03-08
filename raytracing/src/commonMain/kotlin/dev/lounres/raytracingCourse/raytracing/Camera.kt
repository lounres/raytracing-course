package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import kotlin.math.tan


public data class Camera(
    val position: Point,
    val right: Vector,
    val up: Vector,
    val forward: Vector,
    val fovXTan: Double,
    val fovYTan: Double,
    val imageWidth: UInt,
    val imageHeight: UInt,
)

public fun Camera(
    position: Point,
    right: Vector,
    up: Vector,
    forward: Vector,
    fovX: Double,
    imageWidth: UInt,
    imageHeight: UInt,
): Camera {
    val fovXTan = tan(fovX/2)
    return Camera(
        position = position,
        right = right,
        up = up,
        forward = forward,
        fovXTan = fovXTan,
        fovYTan = fovXTan * imageHeight.toInt() / imageWidth.toInt(),
        imageWidth = imageWidth,
        imageHeight = imageHeight,
    )
}