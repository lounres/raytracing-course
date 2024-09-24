rootProject.name = "raytracing-course"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.kotlin.link")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
//        mavenLocal()
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.3.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

stal {
    structure {
        "sceneDescriptionParser"("kotlin multiplatform", "kotlin library settings")
        "ppmWriter"("kotlin multiplatform", "kotlin library settings")
        "quaternions"("kotlin multiplatform", "kotlin library settings")
        "euclideanGeometry"("kotlin multiplatform", "kotlin library settings")
        "raytracing"("kotlin multiplatform", "kotlin library settings")

        "app"("kotlin multiplatform")
    }
    tag {
        "kotlin common settings" since { has("kotlin multiplatform") }
    }
}