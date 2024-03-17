package dev.lounres.raytracingCourse.raytracing.light

import kotlin.math.pow

public interface GammaCorrection {
    public fun correct(color: Color): Color

    public companion object: GammaCorrection {
        private const val gammaFactor = 1 / 2.2
        private fun gammaCorrection(color: Double): Double = color.pow(gammaFactor)
        override fun correct(color: Color): Color =
            Color(
                r = gammaCorrection(color.r),
                g = gammaCorrection(color.g),
                b = gammaCorrection(color.b),
            )
    }
}