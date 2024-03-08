kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.raytracing)
            }
        }
    }
}