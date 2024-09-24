package dev.lounres.raytracingCourse.raytracing.geometry

import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.quaternions.Quaternion
import kotlin.jvm.JvmInline


@JvmInline
public value class Rotation(public val quaternion: Quaternion) {
    public constructor(w: Double, x: Double, y: Double, z: Double) : this(Quaternion(w, x, y, z))
}

public fun Rotation.applyTo(vector: Vector): Vector {
    val vectorAsQuaternion = Quaternion(
        w = 0.0,
        x = vector.x,
        y = vector.y,
        z = vector.z,
    )
    val resultQuaternion = this.quaternion * vectorAsQuaternion * this.quaternion.reciprocal
    return Vector(
        x = resultQuaternion.x,
        y = resultQuaternion.y,
        z = resultQuaternion.z,
    )
}

public fun Rotation.inverseApplyTo(vector: Vector): Vector {
    val vectorAsQuaternion = Quaternion(
        w = 0.0,
        x = vector.x,
        y = vector.y,
        z = vector.z,
    )
    val resultQuaternion = this.quaternion.reciprocal * vectorAsQuaternion * this.quaternion
    return Vector(
        x = resultQuaternion.x,
        y = resultQuaternion.y,
        z = resultQuaternion.z,
    )
}