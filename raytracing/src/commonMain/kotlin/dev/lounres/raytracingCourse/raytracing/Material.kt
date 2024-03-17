package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.light.Color
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt



public interface Material {
    context(Scene.LocalEnvironment)
    public fun traceIncomingRay(incomingRay: Vector, objectColor: Color): LightIntensity
}

public object Diffusive: Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector, objectColor: Color): LightIntensity =
        traceIncomingLightIntensity() * objectColor
}

public object Metallic: Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector, objectColor: Color): LightIntensity =
        traceOutgoingRay(incomingRay - localOuterNormal * (localOuterNormal dot incomingRay * 2.0 / localOuterNormal.length.pow(2))) * objectColor
}

public class Dielectric(
    public val indexOfReflection: Double
): Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector, objectColor: Color): LightIntensity {
        val reflectedVector = incomingRay - localOuterNormal * (localOuterNormal dot incomingRay * 2.0 / localOuterNormal.length.pow(2))

        val etaRatio = if (localOuterNormal dot incomingRay > 0.0) 1 / indexOfReflection else indexOfReflection
        val cosTheta1 = abs(localOuterNormal dot incomingRay / localOuterNormal.length / incomingRay.length)
        val sinTheta2 = etaRatio * sqrt(1 - cosTheta1 * cosTheta1)

        return if (sinTheta2 < 1.0) {
            val cosTheta2 = sqrt(1 - sinTheta2 * sinTheta2)
            val refractedVector = incomingRay * etaRatio + localOuterNormal.let { if (it dot incomingRay > 0.0) it else -it } * (etaRatio * cosTheta1 - cosTheta2)

            val R0 = ((1 - indexOfReflection) / (1 + indexOfReflection)).pow(2)
            val R = R0 + (1 - R0) * (1 - cosTheta1).pow(5)

            traceOutgoingRay(reflectedVector) * R + traceOutgoingRay(refractedVector) * (1 - R)
        } else {
            traceOutgoingRay(reflectedVector)
        } * objectColor
    }
}