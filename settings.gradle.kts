enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

dependencyResolutionManagement {

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

include(":mvi-core")
include(":mvi-android")
include(":mvi-annotation")
include(":mvi-annotation-processor")

include(":samples:address")
include(":samples:design-system")
include(":samples:restaurants")

include(":sample-app")