import com.android.build.api.dsl.LibraryExtension
import io.mityukov.android.build.convention.libsExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "connectivity.samples.android.library")
            apply(plugin = "connectivity.samples.hilt")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true

                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }

            dependencies {
                "implementation"(project(":core:common"))
                "implementation"(libsExt.findLibrary("androidx.hilt.navigation.compose").get())
                "implementation"(libsExt.findLibrary("androidx.navigation3.runtime").get())
                "implementation"(libsExt.findLibrary("androidx.navigation3.ui").get())
                "implementation"(libsExt.findLibrary("androidx.compose.material.iconsExtended").get())
                "implementation"(
                    libsExt.findLibrary("androidx.lifecycle.viewmodel.navigation3").get()
                )

                "implementation"(libsExt.findLibrary("kotlinx.serialization.json").get())

                "testImplementation"(libsExt.findLibrary("androidx.compose.bom").get())

                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.compose.bom").get(),
                )
            }
        }
    }
}
