package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.euclideanGeometry.*
import dev.lounres.raytracingCourse.raytracing.light.LightIntensity
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random


internal fun Random.nextVectorInUnitCube(): Vector =
    Vector(nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0))

internal fun Random.nextVectorInUnitSphere(): Vector {
    var result = nextVectorInUnitCube()
    while (result dot result > 1.0) result = nextVectorInUnitCube()
    return result
}

context(Scene.LocalEnvironment)
internal fun randomVectorOnUnitHemisphere(centralVector: Vector): Vector {
    val vectorOnSphere: Vector = random.nextVectorInUnitSphere().normalized()
    return if (vectorOnSphere dot centralVector < 0.0) -vectorOnSphere else vectorOnSphere
}

context(Scene.LocalEnvironment)
internal fun cosineWeightedRandomVectorOnUnitHemisphere(centralVector: Vector): Vector {
    val normalizedCentralVector = centralVector.normalized()
    val vectorOnSphere: Vector = random.nextVectorInUnitSphere().normalized()
    val shiftedVector = vectorOnSphere + normalizedCentralVector
    return shiftedVector / shiftedVector.length
}

public interface Material {
    context(Scene.LocalEnvironment)
    public fun traceIncomingRay(incomingRay: Vector): LightIntensity
}

public data object Diffusive: Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity {
        val outgoingRay: Vector = randomVectorOnUnitHemisphere(localOuterNormal.let { if (it dot incomingRay < 0.0) it else -it })
//        val outgoingRay: Vector = cosineWeightedRandomVectorOnUnitHemisphere(localOuterNormal.let { if (it dot incomingRay < 0.0) it else -it })
        return emission + traceOutgoingRay(outgoingRay) * objectColor * (2.0 * abs(incomingRay dot localOuterNormal) / localOuterNormal.length)
//        return emission + traceOutgoingRay(outgoingRay) * objectColor
    }
}

public data object Metallic: Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity =
        emission + traceOutgoingRay(incomingRay - localOuterNormal * (localOuterNormal dot incomingRay * 2.0 / localOuterNormal.norm)) * objectColor
}

public data class Dielectric(
    public val indexOfReflection: Double
): Material {
    context(Scene.LocalEnvironment)
    override fun traceIncomingRay(incomingRay: Vector): LightIntensity {
        val reflectedVector = incomingRay - localOuterNormal * (localOuterNormal dot incomingRay * 2.0 / localOuterNormal.norm)

        val etaRatio = if (localOuterNormal dot incomingRay > 0.0) 1 / indexOfReflection else indexOfReflection
        val cosTheta1 = abs(localOuterNormal dot incomingRay / localOuterNormal.length / incomingRay.length)
        val sinTheta2 = etaRatio * sqrt(1 - cosTheta1 * cosTheta1)

        return emission + if (sinTheta2 < 1.0) {
            val cosTheta2 = sqrt(1 - sinTheta2 * sinTheta2)
            val refractedVector = incomingRay.normalized() * etaRatio + localOuterNormal.normalized().let { if (it dot incomingRay > 0.0) it else -it } * (etaRatio * cosTheta1 - cosTheta2)

            val R0 = ((1 - indexOfReflection) / (1 + indexOfReflection)).pow(2)
            val R = R0 + (1 - R0) * (1 - cosTheta1).pow(5)

            if (random.nextDouble() < R) traceOutgoingRay(reflectedVector) else traceOutgoingRay(refractedVector)
        } else {
            traceOutgoingRay(reflectedVector)
        } * objectColor
    }
}