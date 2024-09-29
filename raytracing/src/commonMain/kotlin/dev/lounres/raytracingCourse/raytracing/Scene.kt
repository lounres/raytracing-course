package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.raytracing.figure.Figure
import dev.lounres.raytracingCourse.raytracing.figure.FiniteFigure
import dev.lounres.raytracingCourse.raytracing.geometry.Ray
import dev.lounres.raytracingCourse.raytracing.light.Color
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity
import kotlin.random.Random


internal data class Intersection(
    val moment: Double,
    val sceneObject: SceneObject<Figure>,
)

public interface Scene {
    context(Random)
    public fun trace(
        ray: Ray,
        recursionLimit: UInt,
    ): LightIntensity
    
    context(Random)
    public fun LocalEnvironment.traceOutgoingRay(
        outgoingRayDirection: Vector,
    ): LightIntensity
    
//    public fun probabilityDensityForRay(ray: Ray): Double
    
    public data class LocalEnvironment(
        public val sceneObject: SceneObject<Figure>,
        public val position: Point,
        public val localOuterNormal: Vector,
        public val recursionLimit: UInt,
    )
}

public class SceneObject<out F: Figure>(
    public val figure: F,
    public val color: Color,
    public val material: Material,
    public val emission: LightIntensity,
)

public class SimpleScene(
    private val backgroundLightIntensity: LightIntensity,
    finiteSceneObjects: List<SceneObject<FiniteFigure>>,
    private val sceneObjects: List<SceneObject<Figure>>,
) : Scene {
    private val bvh: BVH = BVH(finiteSceneObjects)
    
    private fun intersect(ray: Ray, fromSceneObject: SceneObject<Figure>? = null): Intersection? {
        var closestIntersection: Intersection? = null
        for (sceneObject in sceneObjects) {
            val intersectionMoment = if (sceneObject != fromSceneObject) sceneObject.figure.intersect(ray) else sceneObject.figure.intersectAgain(ray)
            if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment))
                closestIntersection = Intersection(moment = intersectionMoment, sceneObject = sceneObject)
        }
        val bvhIntersection = bvh.intersect(ray, fromSceneObject)
        if (bvhIntersection != null && (closestIntersection == null || closestIntersection.moment > bvhIntersection.moment))
            closestIntersection = bvhIntersection
        return closestIntersection
    }
    
    context(Random)
    override fun trace(
        ray: Ray,
        recursionLimit: UInt,
    ): LightIntensity {
        val firstIntersection = this@SimpleScene.intersect(ray)
        val lightIntensity = if (firstIntersection != null) {
            val firstObject = firstIntersection.sceneObject
            val firstPosition = ray.atMoment(firstIntersection.moment)
            val firstLocalEnvironment = Scene.LocalEnvironment(
                sceneObject = firstIntersection.sceneObject,
                position = firstPosition,
                localOuterNormal = firstObject.figure.outerNormalFor(position = firstPosition),
                recursionLimit = recursionLimit,
            )
            with(firstLocalEnvironment) { firstObject.material.traceIncomingRay(incomingRay = ray.direction) }
        } else backgroundLightIntensity

        return lightIntensity
    }
    
    context(Random)
    override fun Scene.LocalEnvironment.traceOutgoingRay(
        outgoingRayDirection: Vector,
    ): LightIntensity {
        if (recursionLimit == 0u) return LightIntensity.None
        
        val outgoingRay = Ray(position = position, direction = outgoingRayDirection)
        val nextIntersection = this@SimpleScene.intersect(outgoingRay, fromSceneObject = sceneObject)
        return if (nextIntersection != null) {
            val nextObject = nextIntersection.sceneObject
            val nextPosition = outgoingRay.atMoment(nextIntersection.moment)
            val nextLocalEnvironment = Scene.LocalEnvironment(
                sceneObject = nextIntersection.sceneObject,
                position = nextPosition,
                localOuterNormal = nextObject.figure.outerNormalFor(nextPosition),
                recursionLimit = recursionLimit - 1u,
            )
            with(nextLocalEnvironment) { nextObject.material.traceIncomingRay(incomingRay = outgoingRayDirection) }
        } else backgroundLightIntensity
    }
}