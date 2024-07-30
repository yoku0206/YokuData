@file:Suppress("VulnerableLibrariesLocal", "SpellCheckingInspection")

import java.io.FileInputStream
import java.util.Properties

plugins {
    idea
    java
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("kr.entree.spigradle") version "2.4.3"
    id("maven-publish")
}

val mcVersion = "1.19"
val github = Properties().apply { load(FileInputStream(File("${System.getenv("USERPROFILE")}/.m2/", "github.properties"))) }

group = "me.yoku"
version = "4.0"

repositories {

    mavenCentral()
    mavenLocal()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven {
        url = uri("https://maven.pkg.github.com/yoku0206/YokuLib")
        credentials {
            username = github["gpr.user"] as String
            password = github["gpr.key"] as String
        }
    }

}

dependencies {

    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.19.4-R0.1-SNAPSHOT")

    compileOnly("me.yoku:yokulib:1.0")

}

spigot {

    authors = listOf("Yoku")
    apiVersion = "1.13"
    excludeLibraries("*")

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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    withSourcesJar()
}

kotlin {

    jvmToolchain(17)

}

tasks {

    compileKotlin {

        kotlinOptions.jvmTarget = "17"

    }

    shadowJar {

        archiveBaseName.set("${project.name}-${project.version}")
        archiveClassifier.set("")
        archiveVersion.set("")

    }

    build {

        dependsOn(shadowJar)
        dependsOn(generateSpigotDescription)

    }

}