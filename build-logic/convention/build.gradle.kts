plugins {
    `kotlin-dsl`
}

group = "io.mityukov.connectivity.samples.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}


gradlePlugin {
    plugins {
        register("androidLibraryCompose") {
            id = libs.plugins.connectivity.samples.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.connectivity.samples.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.connectivity.samples.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.connectivity.samples.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.connectivity.samples.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("lint") {
            id = libs.plugins.connectivity.samples.lint.get().pluginId
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("root") {
            id = libs.plugins.connectivity.samples.root.get().pluginId
            implementationClass = "RootPlugin"
        }
    }
}