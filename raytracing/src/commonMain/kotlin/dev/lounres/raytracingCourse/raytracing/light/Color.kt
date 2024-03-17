package dev.lounres.raytracingCourse.raytracing.light

public data class Color(val r: Double, val g: Double, val b: Double) {
    init {
        require(r in 0.0..1.0 && g in 0.0..1.0 && b in 0.0..1.0) { "Color components must be between 0 and 1." }
    }
}