import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka") version "1.4.20"
    `maven-publish`
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 3
        versionName = "0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    api("androidx.startup:startup-runtime:1.0.0")
    api("androidx.activity:activity-ktx:1.2.0-rc01")
    api("androidx.fragment:fragment-ktx:1.3.0-rc01")
}

group = "com.marcelpinto"
version = android.defaultConfig.versionName.toString()

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            displayName.set("permissions-ktx")
            //includes.from("../README.md")
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(
                    URL("https://github.com/marcelpinto/permissions-ktx/tree/main/lib/src/main/java")
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}

val androidJavadocJar by tasks.register<Jar>("androidJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val androidHtmlJar by tasks.register<Jar>("androidHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

val androidSourcesJar by tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
    publications {
        register<MavenPublication>("mavenAndroid") {
            artifactId = "permissions-ktx"

            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
            artifact(tasks.getByName("androidJavadocJar"))
            artifact(tasks.getByName("androidHtmlJar"))
            artifact(tasks.getByName("androidSourcesJar"))

            pom {
                name.set("permissions-ktx")
                description.set("Kotlin Lightweight Android permissions library that follows the best practices.")
                url.set("https://github.com/marcelpinto/permissions-ktx")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("marcelpinto")
                        name.set("Marcel Pinto")
                        email.set("marcel.pinto.biescas@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/marcelpinto/permissions-ktx.git")
                    developerConnection.set("scm:git:ssh://github.com/marcelpinto/permissions-ktx.git")
                    url.set("https://github.com/marcelpinto/permissions-ktx")
                }

                withXml {
                    fun groovy.util.Node.addDependency(dependency: Dependency, scope: String) {
                        appendNode("dependency").apply {
                            appendNode("groupId", dependency.group)
                            appendNode("artifactId", dependency.name)
                            appendNode("version", dependency.version)
                            appendNode("scope", scope)
                        }
                    }

                    asNode().appendNode("dependencies").let { dependencies ->
                        // List all "api" dependencies as "compile" dependencies
                        configurations.api.get().dependencies.forEach {
                            dependencies.addDependency(it, "compile")
                        }
                        // List all "implementation" dependencies as "runtime" dependencies
                        configurations.implementation.get().dependencies.forEach {
                            dependencies.addDependency(it, "runtime")
                        }
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "bintray"
            credentials {
                val properties = gradleLocalProperties(rootDir)
                username = properties.getProperty("bintray.username")
                password = properties.getProperty("bintray.password")
            }
            url = uri("https://api.bintray.com/maven/skimarxall/maven/permissions-ktx/;publish=1")
        }
    }
}
