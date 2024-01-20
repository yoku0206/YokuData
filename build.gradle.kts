@file:Suppress("VulnerableLibrariesLocal", "SpellCheckingInspection")

import java.io.FileInputStream
import java.util.Properties

plugins {
    idea
    java
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("kr.entree.spigradle") version "2.4.3"
    id("maven-publish")
}

val mcVersion = "1.19"
val github = Properties().apply { load(FileInputStream(File("${System.getenv("USERPROFILE")}/.m2/", "github.properties"))) }

group = "me.yoku"
version = "2.3"

repositories {

    mavenCentral()
    mavenLocal()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")

}

dependencies {

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.19.4-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.mongodb:mongodb-driver-sync:4.10.1")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.10.1")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.mongodb:bson-kotlinx:4.10.1")

}

spigot {

    authors = listOf("Yoku")
    excludeLibraries = listOf("*")
    this.apiVersion = "1.13"

}

publishing {

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/yoku0206/YokuData")
            credentials {
                username = github["gpr.user"] as String? ?: System.getenv("GITHUB_ACTOR")
                password = github["gpr.key"] as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            groupId = rootProject.group as String
            artifactId = "yokudata"
            version = rootProject.version as String
            from(components["java"])
        }
    }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {

    wrapper {
        gradleVersion = "7.4.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    shadowJar {

//        minimize {
//            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:1.8.0"))
//            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:1.8.0"))
//        }

        archiveBaseName.set("${project.name}-${project.version}")
        archiveClassifier.set("")
        archiveVersion.set("")

    }

    build {

        dependsOn(shadowJar)
        dependsOn(generateSpigotDescription)

    }

}