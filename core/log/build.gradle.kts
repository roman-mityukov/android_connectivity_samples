plugins {
    alias(libs.plugins.connectivity.samples.android.library)
}

android {
    namespace = "io.mityukov.connectivity.samples.core.log"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    implementation(libs.treessence)
}