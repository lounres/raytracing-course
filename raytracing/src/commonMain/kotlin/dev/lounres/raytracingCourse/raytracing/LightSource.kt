package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*


public data class LightIntensity(
    public val r: Double,
    public val g: Double,
    public val b: Double,
) {

    public operator fun plus(other: LightIntensity): LightIntensity =
        LightIntensity(
            r = this.r + other.r,
            g = this.g + other.g,
            b = this.b + other.b,
        )

    public operator fun times(other: Double): LightIntensity =
        LightIntensity(
            r = this.r * other,
            g = this.g * other,
            b = this.b * other,
        )

    public operator fun div(other: Double): LightIntensity =
        LightIntensity(
            r = this.r / other,
            g = this.g / other,
            b = this.b / other,
        )

    public operator fun times(other: Color): LightIntensity =
        LightIntensity(
            r = this.r * other.r,
            g = this.g * other.g,
            b = this.b * other.b,
        )

    public companion object {
        public val None: LightIntensity = LightIntensity(.0, .0, .0)
    }
}

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
        val nearestIntersection = rayToLight.intersect(scene = scene, fromSceneObject = currentSceneObject)
        return if (nearestIntersection != null && nearestIntersection.moment <= 1) LightIntensity.None else this.lightIntensity * attenuation.onDistance(rayToLight.direction.length) * ((normal dot directionToLight) / normal.length / directionToLight.length)
    }
}

public data class DirectedLightSource(
    val directionToLight: Vector,
    val lightIntensity: LightIntensity,
) : LightSource {
    override fun lightIntensityAt(scene: Scene, currentSceneObject: Int, point: Point, normal: Vector): LightIntensity {
        val rayToLight = Ray(position = point, direction = directionToLight)
        val nearestIntersection = rayToLight.intersect(scene = scene, fromSceneObject = currentSceneObject)
        return if (nearestIntersection != null) LightIntensity.None else ((normal dot directionToLight) / normal.length / directionToLight.length).let { if (it <= .0) LightIntensity.None else lightIntensity * it }
    }
}