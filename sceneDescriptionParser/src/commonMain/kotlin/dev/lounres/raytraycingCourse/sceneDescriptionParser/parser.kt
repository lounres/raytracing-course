package dev.lounres.raytraycingCourse.sceneDescriptionParser

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.raytracing.*


public data class SceneDescription(
    val scene: Scene,
    val backgroundColor: Color,
    val camera: Camera,
)

internal data class SceneDescriptionBuilder(
    val builtScene: MutableSet<SceneObject> = mutableSetOf(),
    var newSceneObject: SceneObjectBuilder? = null,
    var backgroundColor: Color? = null,
    val cameraBuilder: CameraBuilder = CameraBuilder(),
) {
    fun build(): SceneDescription {
        newSceneObject?.let { builtScene.add(it.build()) }
        return SceneDescription(
            scene = builtScene,
            backgroundColor = backgroundColor ?: error("Background color is not set"),
            camera = cameraBuilder.build()
        )
    }
}

internal data class CameraBuilder(
    var position: Point? = null,
    var right: Vector? = null,
    var up: Vector? = null,
    var forward: Vector? = null,
    var fovX: Double? = null,
    var imageWidth: UInt? = null,
    var imageHeight: UInt? = null,
) {
    fun build(): Camera = Camera(
        position = position ?: error("Camera position is not specified."),
        right = right ?: error("Camera right vector is not specified."),
        up = up ?: error("Camera up vector is not specified."),
        forward = forward ?: error("Camera forward vector is not specified."),
        fovX = fovX ?: error("Camera fovXTan is not specified."),
        imageWidth = imageWidth ?: error("Camera image width is not specified."),
        imageHeight = imageHeight ?: error("Camera image height is not specified."),
    )
}

internal data class SceneObjectBuilder(
    val figureBuilder: FigureBuilder,
    var color: Color? = null,
) {
    fun build(): SceneObject =
        SceneObject(
            figure = figureBuilder.build(),
            color = color ?: error("Scene object color is not specified."),
        )
}

internal sealed interface FigureBuilder {
    fun build(): Figure
    var position: Point?
    var rotation: Rotation?
}

internal data class PlaneBuilder(
    val normal: Vector,
    override var position: Point? = null,
    override var rotation: Rotation? = null,
): FigureBuilder {
    override fun build(): Plane {
        val actualRotation = rotation ?: Rotation(1.0 ,0.0, 0.0, 0.0)
        val actualNormal = actualRotation.applyTo(normal)
        val actualPosition = position ?: Point(0.0, 0.0, 0.0)
        return Plane(
            normal = actualNormal,
            valueAtZero = -(actualPosition.x * actualNormal.x + actualPosition.y * actualNormal.y + actualPosition.z * actualNormal.z),
        )
    }
}

internal data class EllipsoidBuilder(
    val rX: Double,
    val rY: Double,
    val rZ: Double,
    override var position: Point? = null,
    override var rotation: Rotation? = null,
): FigureBuilder {
    override fun build(): Ellipsoid = Ellipsoid(
        rX = rX,
        rY = rY,
        rZ = rZ,
        position = position ?: Point(0.0, 0.0, 0.0),
        rotation = rotation ?: Rotation(1.0 ,0.0, 0.0, 0.0),
    )
}

internal data class BoxBuilder(
    val sizeX: Double,
    val sizeY: Double,
    val sizeZ: Double,
    override var position: Point? = null,
    override var rotation: Rotation? = null,
): FigureBuilder {
    override fun build(): Box = Box(
        sizeX = sizeX,
        sizeY = sizeY,
        sizeZ = sizeZ,
        position = position ?: Point(0.0, 0.0, 0.0),
        rotation = rotation ?: Rotation(1.0 ,0.0, 0.0, 0.0),
    )
}

internal data class Command(
    val command: String,
    val parameters: List<Double>,
)

public fun String.parseDescription(): SceneDescription {
    val commands = this
        .split("\r\n")
        .filter { it.isNotBlank() }
        .map {
            val parts = it.split(" ")
            Command(
                command = parts[0],
                parameters = parts.drop(1).map { it.toDouble() }
            )
        }

    val sceneDescriptionBuilder = SceneDescriptionBuilder()

    for (command in commands) when (command.command) {
        "DIMENSIONS" -> {
            require(command.parameters.size == 2)
            require(sceneDescriptionBuilder.cameraBuilder.imageWidth == null && sceneDescriptionBuilder.cameraBuilder.imageHeight == null)
            sceneDescriptionBuilder.cameraBuilder.imageWidth = command.parameters[0].toUInt()
            sceneDescriptionBuilder.cameraBuilder.imageHeight = command.parameters[1].toUInt()
        }
        "BG_COLOR" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.backgroundColor == null)
            sceneDescriptionBuilder.backgroundColor = Color(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_POSITION" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.cameraBuilder.position == null)
            sceneDescriptionBuilder.cameraBuilder.position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_RIGHT" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.cameraBuilder.right == null)
            sceneDescriptionBuilder.cameraBuilder.right = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_UP" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.cameraBuilder.up == null)
            sceneDescriptionBuilder.cameraBuilder.up = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_FORWARD" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.cameraBuilder.forward == null)
            sceneDescriptionBuilder.cameraBuilder.forward = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_FOV_X" -> {
            require(command.parameters.size == 1)
            require(sceneDescriptionBuilder.cameraBuilder.fovX == null)
            sceneDescriptionBuilder.cameraBuilder.fovX = command.parameters[0]
        }
        "NEW_PRIMITIVE" -> {
            require(command.parameters.size == 0)
            sceneDescriptionBuilder.newSceneObject?.let { oldSceneObject -> sceneDescriptionBuilder.builtScene.add(oldSceneObject.build()) }
            sceneDescriptionBuilder.newSceneObject = null
        }
        "PLANE" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.newSceneObject == null)
            sceneDescriptionBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = PlaneBuilder(normal = Vector(command.parameters[0], command.parameters[1], command.parameters[2]))
                )
        }
        "ELLIPSOID" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.newSceneObject == null)
            sceneDescriptionBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = EllipsoidBuilder(
                        rX = command.parameters[0],
                        rY = command.parameters[1],
                        rZ = command.parameters[2],
                    )
                )
        }
        "BOX" -> {
            require(command.parameters.size == 3)
            require(sceneDescriptionBuilder.newSceneObject == null)
            sceneDescriptionBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = BoxBuilder(
                        sizeX = command.parameters[0],
                        sizeY = command.parameters[1],
                        sizeZ = command.parameters[2],
                    )
                )
        }
        "POSITION" -> {
            require(command.parameters.size == 3)
            val newSceneObject = sceneDescriptionBuilder.newSceneObject
            require(newSceneObject != null)
            require(newSceneObject.figureBuilder.position == null)
            newSceneObject.figureBuilder.position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "ROTATION" -> {
            require(command.parameters.size == 4)
            val newSceneObject = sceneDescriptionBuilder.newSceneObject
            require(newSceneObject != null)
            require(newSceneObject.figureBuilder.rotation == null)
            newSceneObject.figureBuilder.rotation = Rotation(x = command.parameters[0], y = command.parameters[1], z = command.parameters[2], w = command.parameters[3])
        }
        "COLOR" -> {
            require(command.parameters.size == 3)
            val newSceneObject = sceneDescriptionBuilder.newSceneObject
            require(newSceneObject != null)
            require(newSceneObject.color == null)
            newSceneObject.color = Color(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        else -> println("Unknown command: ${command.command} ${command.parameters.joinToString()}")
    }
    return sceneDescriptionBuilder.build()
}