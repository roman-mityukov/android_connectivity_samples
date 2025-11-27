plugins {
    alias(libs.plugins.connectivity.samples.android.feature)
    alias(libs.plugins.connectivity.samples.android.library.compose)
    alias(libs.plugins.connectivity.samples.hilt)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "io.mityukov.connectivity.samples.feature.bclassic.chat"
}

dependencies {
    implementation(project(":core:log"))
    implementation(project(":core:connectivity"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}