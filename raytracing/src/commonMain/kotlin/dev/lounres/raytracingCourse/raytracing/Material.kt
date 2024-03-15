package dev.lounres.raytracingCourse.raytracing


public interface Material {
    public fun getLightIntensityOf(scene: Scene, currentSceneObjectIndex: Int, incomingRay: Ray, timeToLive: UInt): LightIntensity
}

public object Diffusive: Material {
    override fun getLightIntensityOf(scene: Scene, currentSceneObjectIndex: Int, incomingRay: Ray, timeToLive: UInt): LightIntensity {
        val normal = scene.sceneObjects[currentSceneObjectIndex].figure.normalAt(incomingRay)
        val lightIntensity = scene.lightSources.fold(scene.ambientLight) { lightIntensityAccumulator, lightSource ->
            lightIntensityAccumulator + lightSource.lightIntensityAt(
                scene = scene,
                currentSceneObject = currentSceneObjectIndex,
                point = incomingRay.position,
                normal = normal
            )
        }
        return lightIntensity * scene.sceneObjects[currentSceneObjectIndex].color
    }
}

public object Metallic: Material {
    override fun getLightIntensityOf(scene: Scene, currentSceneObjectIndex: Int, incomingRay: Ray, timeToLive: UInt): LightIntensity {
        TODO("Not yet implemented")
    }
}

public object Dielectric: Material {
    override fun getLightIntensityOf(scene: Scene, currentSceneObjectIndex: Int, incomingRay: Ray, timeToLive: UInt): LightIntensity {
        TODO("Not yet implemented")
    }
}