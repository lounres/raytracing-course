package dev.lounres.raytraycingCourse.sceneDescriptionParser

import dev.lounres.raytracingCourse.euclideanGeometry.Point
import dev.lounres.raytracingCourse.euclideanGeometry.Vector
import dev.lounres.raytracingCourse.raytracing.*


public data class SceneDescription(
    val scene: Scene,
    val camera: Camera,
    val recursionLimit: UInt,
)

internal data class SceneDescriptionBuilder(
    val sceneBuilder: SceneBuilder = SceneBuilder(),
    val cameraBuilder: CameraBuilder = CameraBuilder(),
    var recursionLimit: UInt? = null
) {
    fun build(): SceneDescription =
        SceneDescription(
            scene = sceneBuilder.build(),
            camera = cameraBuilder.build(),
            recursionLimit = recursionLimit ?: throw IllegalStateException("Recursion limit is not specified.")
        )
}

internal data class SceneBuilder(
    var backgroundColor: Color? = null,
    val sceneObjects: MutableList<SceneObject> = mutableListOf(),
    var newSceneObject: SceneObjectBuilder? = null,
    var ambientLight: LightIntensity? = null,
    val lightSources: MutableList<LightSource> = mutableListOf(),
    var newLightSource: LightSourceBuilder? = null,
) {
    fun build(): Scene {
        newSceneObject?.let { sceneObjects.add(it.build()) }
        newLightSource?.let { lightSources.add(it.build()) }
        return Scene(
            backgroundColor = backgroundColor ?: throw IllegalArgumentException("Background color is not specified."),
            sceneObjects = sceneObjects,
            ambientLight = ambientLight ?: throw IllegalArgumentException("Ambient light is not specified."),
            lightSources = lightSources,
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
        position = position ?: throw IllegalArgumentException("Camera position is not specified."),
        right = right ?: throw IllegalArgumentException("Camera right vector is not specified."),
        up = up ?: throw IllegalArgumentException("Camera up vector is not specified."),
        forward = forward ?: throw IllegalArgumentException("Camera forward vector is not specified."),
        fovX = fovX ?: throw IllegalArgumentException("Camera fovXTan is not specified."),
        imageWidth = imageWidth ?: throw IllegalArgumentException("Camera image width is not specified."),
        imageHeight = imageHeight ?: throw IllegalArgumentException("Camera image height is not specified."),
    )
}

internal data class SceneObjectBuilder(
    val figureBuilder: FigureBuilder,
    var color: Color? = null,
    var material: Material? = null,
) {
    fun build(): SceneObject =
        SceneObject(
            figure = figureBuilder.build(),
            color = color ?: throw IllegalArgumentException("Scene object color is not specified."),
            material = material ?: Diffusive,
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
            valueAtZero = actualPosition.x * actualNormal.x + actualPosition.y * actualNormal.y + actualPosition.z * actualNormal.z,
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

internal interface LightSourceBuilder {
    fun build(): LightSource
    var lightIntensity: LightIntensity?
}

internal data class UndefinedLightSourceBuilder(
    override var lightIntensity: LightIntensity? = null,
): LightSourceBuilder {
    override fun build(): LightSource = throw IllegalArgumentException("Light source type is undefined.")
}

internal data class PointLightSourceBuilder(
    var position: Point? = null,
    var attenuation: Attenuation? = null,
    override var lightIntensity: LightIntensity? = null,
): LightSourceBuilder {
    override fun build(): PointLightSource = PointLightSource(
        position = position ?: throw IllegalArgumentException("Point light source position is not specified."),
        attenuation = attenuation ?: throw IllegalArgumentException("Point light source attenuation is not specified."),
        lightIntensity = lightIntensity ?: throw IllegalArgumentException("Point light source light intensity is not specified."),
    )
}

internal data class DirectedLightSourceBuilder(
    var directionToLight: Vector? = null,
    override var lightIntensity: LightIntensity? = null,
): LightSourceBuilder {
    override fun build(): DirectedLightSource = DirectedLightSource(
        directionToLight = directionToLight ?: throw IllegalArgumentException("Directed light source direction is not specified."),
        lightIntensity = lightIntensity ?: throw IllegalArgumentException("Directed light source light intensity is not specified."),
    )
}

internal data class Command(
    val command: String,
    val parameters: List<Double>,
)

internal fun Command.requireNumberOfArguments(expectedNumber: Int) {
    require(this.parameters.size == expectedNumber) { "Illegal number of arguments of '${this.command}' command: ${this.parameters.size}. Got a command '${this.command} ${this.parameters.joinToString(prefix = "", postfix = "")}'." }
}

public fun String.parseDescription(): SceneDescription {
    val commands = this
        .lines()
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split(" ")
            Command(
                command = parts[0],
                parameters = parts.drop(1).map { it.toDouble() }
            )
        }

    val sceneDescriptionBuilder = SceneDescriptionBuilder()

    for (command in commands) when (command.command) {
        "DIMENSIONS" -> {
            command.requireNumberOfArguments(2)
            require(sceneDescriptionBuilder.cameraBuilder.imageWidth == null && sceneDescriptionBuilder.cameraBuilder.imageHeight == null) { "Either image width or height is already specified." }
            sceneDescriptionBuilder.cameraBuilder.imageWidth = command.parameters[0].toUInt()
            sceneDescriptionBuilder.cameraBuilder.imageHeight = command.parameters[1].toUInt()
        }
        "BG_COLOR" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.sceneBuilder.backgroundColor == null) { "Background color is already specified." }
            sceneDescriptionBuilder.sceneBuilder.backgroundColor = Color(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "RAY_DEPTH" -> {
            command.requireNumberOfArguments(1)
            require(sceneDescriptionBuilder.recursionLimit == null) { "Recursion limit is already specified." }
            sceneDescriptionBuilder.recursionLimit = command.parameters[0].toUInt()
        }
        "AMBIENT_LIGHT" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.sceneBuilder.ambientLight == null) { "Recursion limit is already specified." }
            sceneDescriptionBuilder.sceneBuilder.ambientLight = LightIntensity(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_POSITION" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.cameraBuilder.position == null) { "Camera position is already specified." }
            sceneDescriptionBuilder.cameraBuilder.position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_RIGHT" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.cameraBuilder.right == null) { "Camera right vector is already specified." }
            sceneDescriptionBuilder.cameraBuilder.right = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_UP" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.cameraBuilder.up == null) { "Camera up vector is already specified." }
            sceneDescriptionBuilder.cameraBuilder.up = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_FORWARD" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.cameraBuilder.forward == null) { "Camera forward vector is already specified." }
            sceneDescriptionBuilder.cameraBuilder.forward = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "CAMERA_FOV_X" -> {
//            command.requireNumberOfArguments(1)
            require(sceneDescriptionBuilder.cameraBuilder.fovX == null) { "Camera horizontal field of view is already specified." }
            sceneDescriptionBuilder.cameraBuilder.fovX = command.parameters[0]
        }
        "NEW_PRIMITIVE" -> {
            command.requireNumberOfArguments(0)
            sceneDescriptionBuilder.sceneBuilder.newSceneObject?.let { oldSceneObject -> sceneDescriptionBuilder.sceneBuilder.sceneObjects.add(oldSceneObject.build()) }
            sceneDescriptionBuilder.sceneBuilder.newSceneObject = null
        }
        "PLANE" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.sceneBuilder.newSceneObject == null) { "Previous scene object is not yet processed. Probably there is a scene object declaration without 'NEW_PRIMITIVE' command" }
            sceneDescriptionBuilder.sceneBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = PlaneBuilder(normal = Vector(command.parameters[0], command.parameters[1], command.parameters[2]))
                )
        }
        "ELLIPSOID" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.sceneBuilder.newSceneObject == null) { "Previous scene object is not yet processed. Probably there is a scene object declaration without 'NEW_PRIMITIVE' command" }
            sceneDescriptionBuilder.sceneBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = EllipsoidBuilder(
                        rX = command.parameters[0],
                        rY = command.parameters[1],
                        rZ = command.parameters[2],
                    )
                )
        }
        "BOX" -> {
            command.requireNumberOfArguments(3)
            require(sceneDescriptionBuilder.sceneBuilder.newSceneObject == null) { "Previous scene object is not yet processed. Probably there is a scene object declaration without 'NEW_PRIMITIVE' command" }
            sceneDescriptionBuilder.sceneBuilder.newSceneObject =
                SceneObjectBuilder(
                    figureBuilder = BoxBuilder(
                        sizeX = command.parameters[0],
                        sizeY = command.parameters[1],
                        sizeZ = command.parameters[2],
                    )
                )
        }
        "POSITION" -> {
            command.requireNumberOfArguments(3)
            val newSceneObject = sceneDescriptionBuilder.sceneBuilder.newSceneObject
            require(newSceneObject != null) { "Cannot assign position. There is no current scene object to process." }
            require(newSceneObject.figureBuilder.position == null) { "Scene object position is already specified." }
            newSceneObject.figureBuilder.position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "ROTATION" -> {
            command.requireNumberOfArguments(4)
            val newSceneObject = sceneDescriptionBuilder.sceneBuilder.newSceneObject
            require(newSceneObject != null) { "Cannot assign rotation. There is no current scene object to process." }
            require(newSceneObject.figureBuilder.rotation == null) { "Scene object rotation is already specified." }
            newSceneObject.figureBuilder.rotation = Rotation(x = command.parameters[0], y = command.parameters[1], z = command.parameters[2], w = command.parameters[3])
        }
        "COLOR" -> {
            command.requireNumberOfArguments(3)
            val newSceneObject = sceneDescriptionBuilder.sceneBuilder.newSceneObject
            require(newSceneObject != null) { "Cannot assign color. There is no current scene object to process." }
            require(newSceneObject.color == null) { "Scene object color is already specified." }
            newSceneObject.color = Color(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "METALLIC" -> {
            command.requireNumberOfArguments(0)
            val newSceneObject = sceneDescriptionBuilder.sceneBuilder.newSceneObject
            require(newSceneObject != null) { "Cannot assign rotation. There is no current scene object to process." }
            require(newSceneObject.material == null) { "Scene object material is already specified." }
            newSceneObject.material = Metallic
        }
        "DIELECTRIC" -> {
            command.requireNumberOfArguments(0)
            val newSceneObject = sceneDescriptionBuilder.sceneBuilder.newSceneObject
            require(newSceneObject != null) { "Cannot assign rotation. There is no current scene object to process." }
            require(newSceneObject.material == null) { "Scene object material is already specified." }
            newSceneObject.material = Dielectric
        }
        "IOR" -> {
            TODO("Command is not yet supported")
        }
        "NEW_LIGHT" -> {
            command.requireNumberOfArguments(0)
            sceneDescriptionBuilder.sceneBuilder.newLightSource?.let { oldSceneObject -> sceneDescriptionBuilder.sceneBuilder.lightSources.add(oldSceneObject.build()) }
            sceneDescriptionBuilder.sceneBuilder.newLightSource = UndefinedLightSourceBuilder()
        }
        "LIGHT_INTENSITY" -> {
            command.requireNumberOfArguments(3)
            val newLightSource = sceneDescriptionBuilder.sceneBuilder.newLightSource
            require(newLightSource != null) { "Cannot assign light intensity. There is no current light source to process." }
            require(newLightSource.lightIntensity == null) { "Light source light intensity is already specified." }
            newLightSource.lightIntensity = LightIntensity(command.parameters[0], command.parameters[1], command.parameters[2])
        }
        "LIGHT_DIRECTION" -> {
            command.requireNumberOfArguments(3)
            val newLightSource = sceneDescriptionBuilder.sceneBuilder.newLightSource
            require(newLightSource != null) { "Cannot assign light intensity. There is no current light source to process." }
            sceneDescriptionBuilder.sceneBuilder.newLightSource = when (newLightSource) {
                is DirectedLightSourceBuilder -> newLightSource.also {
                    require(it.directionToLight == null) { "Light source light direction is already specified." }
                    it.directionToLight = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
                }
                is UndefinedLightSourceBuilder -> DirectedLightSourceBuilder(
                    lightIntensity = newLightSource.lightIntensity,
                    directionToLight = Vector(command.parameters[0], command.parameters[1], command.parameters[2])
                )
                else -> throw IllegalArgumentException("Cannot assign light direction. Current light source is of another type.")
            }
        }
        "LIGHT_POSITION" -> {
            command.requireNumberOfArguments(3)
            val newLightSource = sceneDescriptionBuilder.sceneBuilder.newLightSource
            require(newLightSource != null) { "Cannot assign light intensity. There is no current light source to process." }
            sceneDescriptionBuilder.sceneBuilder.newLightSource = when (newLightSource) {
                is PointLightSourceBuilder -> newLightSource.also {
                    require(it.position == null) { "Light source light direction is already specified." }
                    it.position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
                }
                is UndefinedLightSourceBuilder -> PointLightSourceBuilder(
                    lightIntensity = newLightSource.lightIntensity,
                    position = Point(command.parameters[0], command.parameters[1], command.parameters[2])
                )
                else -> throw IllegalArgumentException("Cannot assign light direction. Current light source is of another type.")
            }
        }
        "LIGHT_ATTENUATION" -> {
            command.requireNumberOfArguments(3)
            val newLightSource = sceneDescriptionBuilder.sceneBuilder.newLightSource
            require(newLightSource != null) { "Cannot assign light intensity. There is no current light source to process." }
            sceneDescriptionBuilder.sceneBuilder.newLightSource = when (newLightSource) {
                is PointLightSourceBuilder -> newLightSource.also {
                    require(it.attenuation == null) { "Light source light direction is already specified." }
                    it.attenuation = Attenuation(command.parameters[0], command.parameters[1], command.parameters[2])
                }
                is UndefinedLightSourceBuilder -> PointLightSourceBuilder(
                    lightIntensity = newLightSource.lightIntensity,
                    attenuation = Attenuation(command.parameters[0], command.parameters[1], command.parameters[2])
                )
                else -> throw IllegalArgumentException("Cannot assign light direction. Current light source is of another type.")
            }
        }
        else -> println("Unknown command: ${command.command} ${command.parameters.joinToString()}")
    }
    return sceneDescriptionBuilder.build()
}
