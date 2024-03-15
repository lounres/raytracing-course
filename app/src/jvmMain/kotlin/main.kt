import dev.lounres.raytracingCourse.raytracing.*
import dev.lounres.raytraycingCourse.ppmWriter.writePpmImage
import dev.lounres.raytraycingCourse.sceneDescriptionParser.parseDescription
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.round


fun main(args: Array<String>) {
    require(args.size == 2) { "Illegal number of arguments of the main program: ${args.size}. The arguments: ${args.contentToString()}" }

    val inputFile = Path(args[0])
    val outputFile = Path(args[1])

    val (scene, camera, recursionLimit) = inputFile.readText().parseDescription()

    outputFile.writePpmImage(
        imageWidth = camera.imageWidth,
        imageHeight = camera.imageHeight,
        bytesToWrite =
            buildList<Byte>((camera.imageHeight * camera.imageWidth).toInt() * 3) {
                for (y in (camera.imageHeight - 1u) downTo 0u) for (x in 0u..<camera.imageWidth) {
                    if (x == 750u && y == camera.imageHeight - 775u)
                        run {}
                    val pixelRay = camera.rayForPixel(x, y)
                    val pixelColor = scene.trace(ray = pixelRay, recursionLimit = recursionLimit)
                    add(round(255 * pixelColor.r).toInt().toByte())
                    add(round(255 * pixelColor.g).toInt().toByte())
                    add(round(255 * pixelColor.b).toInt().toByte())
                }
            }.toByteArray()
    )
}