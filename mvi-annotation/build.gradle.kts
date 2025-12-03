plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

val artifactId = "annotation"
group = "com.buildkt.mvi"
version = "0.1.0"

kotlin {
    jvmToolchain(jdkVersion = 11)
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
            "Provides the KSP annotations (@MviScreen, @TriggersSideEffect, @NavArgument) for the buildkt MVI framework's code generation engine."
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
