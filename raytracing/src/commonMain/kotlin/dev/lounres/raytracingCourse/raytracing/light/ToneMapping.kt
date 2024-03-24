package dev.lounres.raytracingCourse.raytracing.light

import kotlin.math.min

public interface ToneMapping {
    public fun map(lightIntensity: LightIntensity): Color

    public object Aces: ToneMapping {
        private const val a = 2.51
        private const val b = 0.03
        private const val c = 2.43
        private const val d = 0.59
        private const val e = 0.14

        private fun acesToneMapping(lightIntensity: Double): Double = min(((a * lightIntensity + b) * lightIntensity) / ((c * lightIntensity + d) * lightIntensity + e), 1.0)

        override fun map(lightIntensity: LightIntensity): Color =
            Color(
                r = acesToneMapping(lightIntensity.r),
                g = acesToneMapping(lightIntensity.g),
                b = acesToneMapping(lightIntensity.b),
            )
    }
}