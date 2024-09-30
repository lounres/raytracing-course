package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.geometry.Ray
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity
import dev.lounres.raytracingCourse.raytracing.random.nextCosineWeightedVectorOnUnitHemisphere
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random


public interface Material {
    context(Random, Scene, Scene.LocalEnvironment)
    public fun traceIncomingRay(incomingRay: Vector): LightIntensity
}

public data object Diffusive: Material {
    context(Random, Scene, Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity {
        // Cosine-weighted-only IS
//        val correctNormal = localOuterNormal.let { if (it dot incomingRay < 0.0) it else -it }
//        val outgoingRayDirection: Vector = nextCosineWeightedVectorOnUnitHemisphere(correctNormal)
//        val incomingLightIntensity: LightIntensity = traceOutgoingRay(outgoingRayDirection)
//        val probabilityDensity = max(0.0, (outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length)
//        return sceneObject.emission + incomingLightIntensity * sceneObject.color *
//                ((outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length / probabilityDensity).also { if (!it.isFinite()) println("$probabilityDensity $outgoingRayDirection") }
        
        // MIS with stupid solution when there is no other light source
//        val correctNormal = localOuterNormal.let { if (it dot incomingRay < 0.0) it else -it }
//        val (outgoingRayDirection, incomingLightIntensity) =
//            if (nextBoolean()) {
//                val outgoingRayDirection = nextCosineWeightedVectorOnUnitHemisphere(correctNormal)
//                TracingSample(outgoingRayDirection, traceOutgoingRay(outgoingRayDirection))
//            } else randomTracingSampleFor(position, sceneObject) ?: run {
//                val outgoingRayDirection =  nextCosineWeightedVectorOnUnitHemisphere(correctNormal)
//                TracingSample(outgoingRayDirection, traceOutgoingRay(outgoingRayDirection))
//            }
//        val probabilityDensity =
//            0.5 * max(0.0, (outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length) / PI +
//                    0.5 * probabilityDensityForRay(Ray(position, outgoingRayDirection), sceneObject)
//        return sceneObject.emission + incomingLightIntensity * sceneObject.color *
//                (abs(outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length / PI / probabilityDensity)
        
        // MIS with rollback to cosine-weighted-only IS when there is no other light source
        val correctNormal = localOuterNormal.let { if (it dot incomingRay < 0.0) it else -it }
        if (nextBoolean()) {
            val outgoingRayDirection = nextCosineWeightedVectorOnUnitHemisphere(correctNormal)
            val incomingLightIntensity = traceOutgoingRay(outgoingRayDirection)
            val probabilityDensity =
                0.5 * max(0.0, (outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length) / PI +
                        0.5 * probabilityDensityForRay(Ray(position, outgoingRayDirection), sceneObject)
            return sceneObject.emission + incomingLightIntensity * sceneObject.color *
                    (abs(outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length / PI / probabilityDensity)
        } else {
            val sourceLightTracingAttempt = randomTracingSampleFor(position, sceneObject)
            if (sourceLightTracingAttempt != null) {
                val (outgoingRayDirection, incomingLightIntensity) = sourceLightTracingAttempt
                val probabilityDensity =
                    0.5 * max(0.0, (outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length) / PI +
                            0.5 * probabilityDensityForRay(Ray(position, outgoingRayDirection), sceneObject)
                return sceneObject.emission + incomingLightIntensity * sceneObject.color *
                        (abs(outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length / PI / probabilityDensity)
            } else {
                val outgoingRayDirection = nextCosineWeightedVectorOnUnitHemisphere(correctNormal)
                val incomingLightIntensity = traceOutgoingRay(outgoingRayDirection)
                val probabilityDensity = max(0.0, (outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length) / PI
                return sceneObject.emission + incomingLightIntensity * sceneObject.color *
                        (abs(outgoingRayDirection dot correctNormal) / outgoingRayDirection.length / correctNormal.length / PI / probabilityDensity)
            }
        }
    }
}

public data object Metallic: Material {
    context(Random, Scene, Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity =
        sceneObject.emission + traceOutgoingRay(incomingRay - localOuterNormal * (localOuterNormal dot incomingRay * 2.0 / localOuterNormal.norm)) * sceneObject.color
}

public data class Dielectric(
    public val indexOfReflection: Double
): Material {
    context(Random, Scene, Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity {
        val reflectedVector = incomingRay - localOuterNormal * ((localOuterNormal dot incomingRay) * 2.0 / localOuterNormal.norm)

        val etaRatio = if (localOuterNormal dot incomingRay > 0.0) 1 / indexOfReflection else indexOfReflection
        val cosTheta1 = abs((localOuterNormal dot incomingRay) / localOuterNormal.length / incomingRay.length)
        val sinTheta2 = etaRatio * sqrt(1 - cosTheta1 * cosTheta1)

        return sceneObject.emission + if (sinTheta2 < 1.0) {
            val cosTheta2 = sqrt(1 - sinTheta2 * sinTheta2)
            val refractedVector = incomingRay.normalized() * etaRatio + localOuterNormal.normalized().let { if (it dot incomingRay > 0.0) it else -it } * (etaRatio * cosTheta1 - cosTheta2)

            val R0 = ((1 - indexOfReflection) / (1 + indexOfReflection)).pow(2)
            val R = R0 + (1 - R0) * (1 - cosTheta1).pow(5)

            if (nextDouble() < R) traceOutgoingRay(reflectedVector) else traceOutgoingRay(refractedVector)
        } else {
            traceOutgoingRay(reflectedVector)
        } * sceneObject.color
    }
}