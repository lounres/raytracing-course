import dev.lounres.raytracingCourse.raytracing.light.Color
import dev.lounres.raytracingCourse.raytracing.light.GammaCorrection
import dev.lounres.raytracingCourse.raytracing.light.ToneMapping
import dev.lounres.raytraycingCourse.ppmWriter.writePpmImage
import dev.lounres.raytraycingCourse.sceneDescriptionParser.parseDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.round
import kotlin.random.Random
import kotlin.random.asKotlinRandom


fun Color.asHexString(): String = "#${(r * 255).toInt().toString(16)}${(g * 255).toInt().toString(16)}${(b * 255).toInt().toString(16)}"

val gammaCorrection: GammaCorrection = GammaCorrection
val toneMapping: ToneMapping = ToneMapping.Aces
val random: Random get() = ThreadLocalRandom.current().asKotlinRandom()

fun main(args: Array<String>) {
    require(args.size == 2) { "Illegal number of arguments of the main program: ${args.size}. The arguments: ${args.contentToString()}" }

    val inputFile = Path(args[0])
    val outputFile = Path(args[1])

    val (scene, camera, recursionLimit, numberOfSamples) = inputFile.readText().parseDescription()

    with(random) {
        outputFile.writePpmImage(
            imageWidth = camera.imageWidth,
            imageHeight = camera.imageHeight,
            bytesToWrite = runBlocking(Dispatchers.Default) {
                List(camera.imageHeight.toInt()) {
                    val y = camera.imageHeight - 1u - it.toUInt()
                    List(camera.imageWidth.toInt()) {
                        val x = it.toUInt()
                        async {
                            val lightIntensity =
                                List(numberOfSamples.toInt()) {
                                    run {
                                        scene.trace(
                                            camera.rayForPixel(x, y, random),
                                            recursionLimit = recursionLimit
                                        )
                                    }
                                }.reduce { acc, lightIntensity -> acc + lightIntensity } / numberOfSamples.toDouble()
                            val color = gammaCorrection.correct(toneMapping.map(lightIntensity))
                            listOf(
                                round(255 * color.r).toInt().toByte(),
                                round(255 * color.g).toInt().toByte(),
                                round(255 * color.b).toInt().toByte(),
                            )
                        }
                    }
                }.flatten().awaitAll().flatten().toByteArray()
            }
        )
    }
}