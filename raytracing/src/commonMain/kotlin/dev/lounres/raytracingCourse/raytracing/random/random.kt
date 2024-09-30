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

internal class AliasMethod(val size: Int, val probabilityTable: List<Double>, val aliasTable: List<Int>) {
    init {
        require(probabilityTable.size == size && aliasTable.size == size)
    }
    context(Random)
    fun sample(): Int {
        val i = nextInt(size)
        val u = nextDouble()
        return if (u < probabilityTable[i]) i else aliasTable[i]
    }
}

internal fun AliasMethod(inputProbabilities: List<Double>): AliasMethod {
    val size = inputProbabilities.size
    val aliasTable = MutableList(size) { it }
    val probabilityTable = MutableList(size) { inputProbabilities[it] }
    val (small, large) = (0 ..< size).partition { probabilityTable[it] < 1.0 }.let { (small, large) -> Pair(small.toMutableList(), large.toMutableList()) }
    while (small.isNotEmpty() && large.isNotEmpty()) {
        val i = small.removeLast()
        val j = large.removeLast()
        aliasTable[i] = j
        val q = probabilityTable[i] + probabilityTable[j] - 1
        probabilityTable[j] = q
        if (q < 1.0) small.add(j) else large.add(j)
    }
    return AliasMethod(size = size, aliasTable = aliasTable, probabilityTable = probabilityTable)
}