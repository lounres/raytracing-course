package dev.lounres.raytracingCourse.raytracing.random

import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.euclideanGeometry.dot
import dev.lounres.raytracingCourse.euclideanGeometry.normalized
import dev.lounres.raytracingCourse.euclideanGeometry.plus
import dev.lounres.raytracingCourse.euclideanGeometry.unaryMinus
import kotlin.random.Random


internal fun Random.nextVectorInUnitCube(): Vector =
    Vector(nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0))

internal fun Random.nextVectorOnUnitSphere(): Vector {
    var result = nextVectorInUnitCube()
    while (result dot result > 1.0) result = nextVectorInUnitCube()
    return result
}

internal fun Random.nextVectorOnUnitHemisphere(centralVector: Vector): Vector {
    val vectorOnSphere: Vector = nextVectorOnUnitSphere().normalized()
    return if (vectorOnSphere dot centralVector < 0.0) -vectorOnSphere else vectorOnSphere
}

internal fun Random.nextCosineWeightedVectorOnUnitHemisphere(centralVector: Vector): Vector {
    val normalizedCentralVector = centralVector.normalized()
    val vectorOnSphere: Vector = nextVectorOnUnitSphere().normalized()
    val shiftedVector = vectorOnSphere + normalizedCentralVector
    return shiftedVector.normalized()
}