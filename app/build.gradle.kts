plugins {
    application
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.sceneDescriptionParser)
                implementation(projects.raytracing)
                implementation(projects.ppmWriter)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

application {
    mainClass = "MainKt"
}