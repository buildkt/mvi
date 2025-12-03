package com.buildkt.conventions

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("org.jetbrains.dokka")
        project.plugins.apply("org.jetbrains.kotlin.android")

        project.extensions.configure<LibraryExtension> {
            compileSdk = 36

            buildTypes { release { isMinifyEnabled = false } }

            defaultConfig {
                minSdk = 26

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            project.extensions.configure<KotlinAndroidProjectExtension> { jvmToolchain(jdkVersion = 11) }
        }
    }
}
