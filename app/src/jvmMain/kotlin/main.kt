import dev.lounres.raytracingCourse.raytracing.light.Color
import dev.lounres.raytraycingCourse.ppmWriter.writePpmImage
import dev.lounres.raytraycingCourse.sceneDescriptionParser.parseDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.round
import kotlin.random.Random
import kotlin.random.asKotlinRandom


fun Color.asHexString(): String = "#${(r * 255).toInt().toString(16)}${(g * 255).toInt().toString(16)}${(b * 255).toInt().toString(16)}"

val random: Random get() = ThreadLocalRandom.current().asKotlinRandom()

fun main(args: Array<String>) {
    require(args.size == 2) { "Illegal number of arguments of the main program: ${args.size}. The arguments: ${args.contentToString()}" }

    val inputFile = Path(args[0])
    val outputFile = Path(args[1])

    val (scene, camera, recursionLimit, numberOfSamples) = inputFile.readText().parseDescription()

    outputFile.writePpmImage(
        imageWidth = camera.imageWidth,
        imageHeight = camera.imageHeight,
        bytesToWrite =
            buildList<Byte>((camera.imageHeight * camera.imageWidth).toInt() * 3) {
                for (y in (camera.imageHeight - 1u) downTo 0u) for (x in 0u..<camera.imageWidth) {
                    var r = 0.0
                    var g = 0.0
                    var b = 0.0
                    val mutex = Mutex()
                    runBlocking(Dispatchers.Default) {
                        for (i in 0u..<numberOfSamples) launch {
                            val result = scene.trace(
                                camera.rayForPixel(x, y, random),
                                random = random,
                                recursionLimit = recursionLimit
                            )
                            mutex.withLock {
                                r += result.r
                                g += result.g
                                b += result.b
                            }
                        }
                    }
                    r /= numberOfSamples.toInt()
                    g /= numberOfSamples.toInt()
                    b /= numberOfSamples.toInt()
                    add(round(255 * r).toInt().toByte())
                    add(round(255 * g).toInt().toByte())
                    add(round(255 * b).toInt().toByte())
                }
            }.toByteArray()
    )
}