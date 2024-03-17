package dev.lounres.raytracingCourse.raytracing.light

public interface ToneMapping {
    public fun map(lightIntensity: LightIntensity): Color

    public object Aces: ToneMapping {
        private const val a = 2.51
        private const val b = 0.03f
        private const val c = 2.43f
        private const val d = 0.59f
        private const val e = 0.14f

        private fun acesToneMapping(lightIntensity: Double): Double = ((a * lightIntensity + b) * lightIntensity) / ((c * lightIntensity + d) * lightIntensity + e)

        override fun map(lightIntensity: LightIntensity): Color =
            Color(
                r = acesToneMapping(lightIntensity.r),
                g = acesToneMapping(lightIntensity.g),
                b = acesToneMapping(lightIntensity.b),
            )
    }
}