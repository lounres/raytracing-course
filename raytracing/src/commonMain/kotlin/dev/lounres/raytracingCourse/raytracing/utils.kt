package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.plus
import dev.lounres.raytracingCourse.euclideanGeometry.times
import kotlin.math.pow


public fun Camera.rayForPixel(x: UInt, y: UInt): Ray =
    Ray(
        position = this.position,
        direction =
            right * (((2 * x.toInt() + 1) / imageWidth.toDouble() - 1) * fovXTan) +
            up * ((2 * y.toInt() + 1) / imageHeight.toDouble() - 1) * fovYTan +
            forward
    )

public fun acesToneMapping(lightIntensity: LightIntensity): Color {
    val a = 2.51f
    val b = 0.03f
    val c = 2.43f
    val d = 0.59f
    val e = 0.14f
    fun acesToneMapping(lightIntensity: Double): Double = ((a * lightIntensity + b) * lightIntensity) / ((c * lightIntensity + d) * lightIntensity + e)
    return Color(
        r = acesToneMapping(lightIntensity.r),
        g = acesToneMapping(lightIntensity.g),
        b = acesToneMapping(lightIntensity.b),
    )
}

public fun gammaCorrection(color: Color): Color {
    val gammaFactor = 1 / 2.2
    fun gammaCorrection(color: Double): Double = color.pow(gammaFactor)
    return Color(
        r = gammaCorrection(color.r),
        g = gammaCorrection(color.g),
        b = gammaCorrection(color.b),
    )
}

public fun Scene.trace(ray: Ray, recursionLimit: UInt): Color =
    ray.intersect(scene = this)?.let {
        val sceneObject = this.sceneObjects[it.sceneObjectIndex]
        gammaCorrection(
            acesToneMapping(
                sceneObject.material.getLightIntensityOf(
                    scene = this,
                    currentSceneObjectIndex = it.sceneObjectIndex,
                    incomingRay = Ray(
                        position = ray.atMoment(it.moment),
                        direction = ray.direction
                    ),
                    timeToLive = recursionLimit,
                )
            )
        )
    } ?: backgroundColor