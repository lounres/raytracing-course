import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

val jvmTargetVersion : String by properties

fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

stal {
    action {
        "kotlin multiplatform" {
            apply<KotlinMultiplatformPluginWrapper>()
            configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                jvm {
                    compilations.all {
                        kotlinOptions {
                            jvmTarget = jvmTargetVersion
                            freeCompilerArgs += listOf(
                                "-Xlambdas=indy"
                            )
                        }
                    }
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }

                js(IR) {
                    browser()
                    nodejs()
                }

                @Suppress("UNUSED_VARIABLE")
                sourceSets {
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
            afterEvaluate {
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
        }
        "kotlin common settings" {
            pluginManager.withPlugins(rootProject.libs.plugins.kotlin.jvm, rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinProjectExtension> {
                    sourceSets {
                        all {
                            languageSettings {
                                progressiveMode = true
                                enableLanguageFeature("ContextReceivers")
                                enableLanguageFeature("ValueClasses")
                                enableLanguageFeature("ContractSyntaxV2")
                                optIn("kotlin.contracts.ExperimentalContracts")
                                optIn("kotlin.ExperimentalStdlibApi")
                                optIn("kotlin.ExperimentalUnsignedTypes")
                            }
                        }
                    }
                }
            }
            pluginManager.withPlugin("org.gradle.java") {
                configure<JavaPluginExtension> {
                    targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
                }
                tasks.withType<Test> {
                    useJUnitPlatform()
                }
            }
        }
        "kotlin library settings" {
            configure<KotlinProjectExtension> {
                explicitApi = ExplicitApiMode.Warning
            }
        }
    }
}