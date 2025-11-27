plugins {
    alias(libs.plugins.connectivity.samples.android.library)
    alias(libs.plugins.connectivity.samples.hilt)
}

android {
    namespace = "io.mityukov.connectivity.samples.core.connectivity"
}
dependencies {
    implementation(project(":core:log"))
    implementation(libs.kotlinx.coroutines.android)
}