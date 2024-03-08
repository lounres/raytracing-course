kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.quaternions)
                api(projects.euclideanGeometry)
            }
        }
    }
}