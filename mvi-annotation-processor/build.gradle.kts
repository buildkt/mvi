plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}
val artifactId = "annotation-processor"
group = "com.buildkt.mvi"
version = "0.2.1"

kotlin {
    jvmToolchain(jdkVersion = 11)
}

dependencies {
    implementation(projects.mviAnnotation)

    implementation(libs.ksp.poet)
    implementation(libs.ksp.symbol.processing.api)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        group.toString(),
        artifactId,
        version.toString()
    )

    pom {
        name = "MVI Annotation Processor"
        description =
            "The KSP (Kotlin Symbol Processing) annotation processor that generates MVI boilerplate for the buildkt MVI framework."
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
