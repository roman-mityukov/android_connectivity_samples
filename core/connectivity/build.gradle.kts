plugins {
    alias(libs.plugins.connectivity.samples.android.library)
    alias(libs.plugins.connectivity.samples.hilt)
}

android {
    namespace = "io.mityukov.connectivity.samples.core.connectivity"
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:log"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}