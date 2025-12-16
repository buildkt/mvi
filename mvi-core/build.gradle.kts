plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

val artifactId = "core"
group = "com.buildkt.mvi"
version = "0.2.0"

android {
    namespace = "com.buildkt.mvi"

    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        group.toString(),
        artifactId,
        version.toString(),
    )

    pom {
        name = "MVI Annotation Library"
        description =
            "The platform-agnostic core of the buildkt MVI framework, providing fundamental components like Reducer, SideEffect, and Middleware."
        inceptionYear = "2025"
        url = "https://github.com/buildkt/mvi/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "buildkt"
                email = "matiasdelbel@gmail.com"
                name = "Matias Del Bel"
                organization = "build kt"
                organizationUrl = "https://github.com/buildkt/"
            }
        }
        scm {
            url = "https://github.com/buildkt/mvi/"
            connection = "scm:git:git://github.com/buildkt/mvi.git"
            developerConnection = "scm:git:ssh://git@github.com/buildkt/mvi.git"
        }
    }
}
