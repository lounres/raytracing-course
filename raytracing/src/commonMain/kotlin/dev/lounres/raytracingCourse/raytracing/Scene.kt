package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.raytracing.figure.Figure
import dev.lounres.raytracingCourse.raytracing.light.Color
import dev.lounres.raytracingCourse.raytracing.light.GammaCorrection
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity
import dev.lounres.raytracingCourse.raytracing.light.ToneMapping


public data class Intersection(
    val moment: Double,
    val sceneObjectIndex: Int,
)

public interface Scene {
    public fun trace(
        ray: Ray,
        recursionLimit: UInt,
        toneMapping: ToneMapping = ToneMapping.Aces,
        gammaCorrection: GammaCorrection = GammaCorrection
    ): Color

    public fun intersect(ray: Ray, fromSceneObject: Int? = null): Intersection?

    public interface LocalEnvironment {
        public val position: Point
        public val localOuterNormal: Vector

        public fun traceOutgoingRay(outgoingRayDirection: Vector): LightIntensity
        public fun traceIncomingLightIntensity(): LightIntensity
    }
}

public data class SceneObject(
    val figure: Figure,
    val color: Color,
    val material: Material
)

public data class SimpleScene(
    val backgroundLightIntensity: LightIntensity,
    val sceneObjects: List<SceneObject>,
    val ambientLight: LightIntensity,
    val lightSources: List<LightSource>,
) : Scene {
    override fun intersect(ray: Ray, fromSceneObject: Int?): Intersection? {
        var closestIntersection: Intersection? = null
        for ((sceneObjectIndex, sceneObject) in sceneObjects.withIndex()) {
            val intersectionMoment = if (sceneObjectIndex != fromSceneObject) sceneObject.figure.intersect(ray) else sceneObject.figure.intersectAgain(ray)
            if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment))
                closestIntersection = Intersection(moment = intersectionMoment, sceneObjectIndex = sceneObjectIndex)
        }
        return closestIntersection
    }

    private inner class LocalEnvironmentImpl(
        val currentObject: Int,
        override val position: Point,
        override val localOuterNormal: Vector,
        val recursionLimit: UInt,
    ) : Scene.LocalEnvironment {
        override fun traceIncomingLightIntensity(): LightIntensity =
            lightSources.fold(ambientLight) { acc, lightSource -> acc + lightSource.lightIntensityAt(this@SimpleScene, currentObject, position, localOuterNormal) }
        override fun traceOutgoingRay(outgoingRayDirection: Vector): LightIntensity {
            if (recursionLimit == 0u) return LightIntensity.None

            val outgoingRay = Ray(position = position, direction = outgoingRayDirection)
            val nextIntersection = this@SimpleScene.intersect(outgoingRay, fromSceneObject = currentObject)
            return if (nextIntersection != null) {
                val nextObject = sceneObjects[nextIntersection.sceneObjectIndex]
                val nextPosition = outgoingRay.atMoment(nextIntersection.moment)
                val nextLocalEnvironment = LocalEnvironmentImpl(
                    currentObject = nextIntersection.sceneObjectIndex,
                    position = nextPosition,
                    localOuterNormal = nextObject.figure.outerNormalFor(nextPosition),
                    recursionLimit = recursionLimit - 1u,
                )
                with(nextLocalEnvironment) { nextObject.material.traceIncomingRay(outgoingRayDirection, nextObject.color) }
            } else backgroundLightIntensity
        }
    }

    override fun trace(ray: Ray, recursionLimit: UInt, toneMapping: ToneMapping, gammaCorrection: GammaCorrection): Color {
        val firstIntersection = this@SimpleScene.intersect(ray)
        val lightIntensity = if (firstIntersection != null) {
            val firstObject = sceneObjects[firstIntersection.sceneObjectIndex]
            val firstPosition = ray.atMoment(firstIntersection.moment)
            val firstLocalEnvironment = LocalEnvironmentImpl(
                currentObject = firstIntersection.sceneObjectIndex,
                position = firstPosition,
                localOuterNormal = firstObject.figure.outerNormalFor(position = firstPosition),
                recursionLimit = recursionLimit,
            )
            with(firstLocalEnvironment) { firstObject.material.traceIncomingRay(ray.direction, firstObject.color) }
        } else backgroundLightIntensity

        return gammaCorrection.correct(toneMapping.map(lightIntensity))
    }
}