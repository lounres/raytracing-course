package dev.lounres.raytracingCourse.raytracing


public data class SceneObject(
    val figure: Figure,
    val color: Color,
    val material: Material
)

public data class Scene(
    val backgroundColor: Color,
    val sceneObjects: List<SceneObject>,
    val ambientLight: LightIntensity,
    val lightSources: List<LightSource>,
)

public data class Intersection(
    val moment: Double,
    val sceneObjectIndex: Int,
)

public fun Ray.intersect(scene: Scene, fromSceneObject: Int? = null): Intersection? {
    var closestIntersection: Intersection? = null
    for ((sceneObjectIndex, sceneObject) in scene.sceneObjects.withIndex()) {
        val intersectionMoment = if (sceneObjectIndex != fromSceneObject) sceneObject.figure.intersect(this) else sceneObject.figure.intersectAgain(this)
        if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment))
            closestIntersection = Intersection(moment = intersectionMoment, sceneObjectIndex = sceneObjectIndex)
    }
    return closestIntersection
}