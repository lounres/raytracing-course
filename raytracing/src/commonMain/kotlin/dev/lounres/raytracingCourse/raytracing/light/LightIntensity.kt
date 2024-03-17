package dev.lounres.raytracingCourse.raytracing.light

public data class LightIntensity(
    public val r: Double,
    public val g: Double,
    public val b: Double,
) {

    public operator fun plus(other: LightIntensity): LightIntensity =
        LightIntensity(
            r = this.r + other.r,
            g = this.g + other.g,
            b = this.b + other.b,
        )

    public operator fun times(other: Double): LightIntensity =
        LightIntensity(
            r = this.r * other,
            g = this.g * other,
            b = this.b * other,
        )

    public operator fun div(other: Double): LightIntensity =
        LightIntensity(
            r = this.r / other,
            g = this.g / other,
            b = this.b / other,
        )

    public operator fun times(other: Color): LightIntensity =
        LightIntensity(
            r = this.r * other.r,
            g = this.g * other.g,
            b = this.b * other.b,
        )

    public companion object {
        public val None: LightIntensity = LightIntensity(.0, .0, .0)
        public val Unit: LightIntensity = LightIntensity(1.0, 1.0, 1.0)
    }
}