package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity


public interface LightSource {
    public fun lightIntensityAt(scene: Scene, currentSceneObject: Int, point: Point, normal: Vector): LightIntensity
}

public data class Attenuation(
    val c0: Double,
    val c1: Double,
    val c2: Double,
) {
    public fun onDistance(distance: Double): Double = 1 / ((c2 * distance + c1) * distance + c0)
}

public data class PointLightSource(
    val position: Point,
    val attenuation: Attenuation,
    val lightIntensity: LightIntensity
) : LightSource {
    override fun lightIntensityAt(scene: Scene, currentSceneObject: Int, point: Point, normal: Vector): LightIntensity {
        val directionToLight = position - point
        val rayToLight = Ray(position = point, direction = directionToLight)
        val nearestIntersection = scene.intersect(ray = rayToLight, fromSceneObject = currentSceneObject)
        return if (nearestIntersection != null && nearestIntersection.moment <= 1) LightIntensity.None else this.lightIntensity * attenuation.onDistance(rayToLight.direction.length) * ((normal dot directionToLight) / normal.length / directionToLight.length)
    }
}

public data class DirectedLightSource(
    val directionToLight: Vector,
    val lightIntensity: LightIntensity,
) : LightSource {
    override fun lightIntensityAt(scene: Scene, currentSceneObject: Int, point: Point, normal: Vector): LightIntensity {
        val rayToLight = Ray(position = point, direction = directionToLight)
        val nearestIntersection = scene.intersect(ray = rayToLight, fromSceneObject = currentSceneObject)
        return if (nearestIntersection != null) LightIntensity.None else ((normal dot directionToLight) / normal.length / directionToLight.length).let { if (it <= .0) LightIntensity.None else lightIntensity * it }
    }
}