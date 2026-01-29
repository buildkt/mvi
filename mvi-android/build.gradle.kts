plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

val artifactId = "android"
group = "com.buildkt.mvi"
version = "0.2.0"

android {
    namespace = "com.buildkt.mvi.android"
}

dependencies {
    api(projects.mviAnnotation)
    api(projects.mviCore)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.icons.core)
    implementation(libs.androidx.material3.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation.runtime.android)
}

tasks.register<Jar>(name = "dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

tasks.register<Jar>(name = "dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
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
        name = "MVI Android Library"
        description =
            "The Android-specific module for the buildkt MVI framework, providing the base ViewModel, navigation extensions, and UI event collectors."
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
