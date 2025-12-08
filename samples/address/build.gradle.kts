plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.buildkt.feature.address"
}

dependencies {
    implementation(projects.mviAndroid)
    implementation(projects.samples.designSystem)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.icons.core)
    implementation(libs.androidx.material3.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    debugImplementation(libs.androidx.ui.tooling)

    ksp(libs.room.compiler)
    ksp(projects.mviAnnotationProcessor)
}
