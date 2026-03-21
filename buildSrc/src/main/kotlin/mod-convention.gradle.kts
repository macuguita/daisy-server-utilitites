/*
 * Copyright (c) 2026 macuguita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    `maven-publish`
    `java-library`
}

group = rootProject.group
version = project.version

val minecraftVersion = rootProject.providers.gradleProperty("minecraft_version").get()
val loaderVersion = rootProject.providers.gradleProperty("loader_version").get()

repositories {
    mavenLocal()
    mavenCentral()
    val exclusiveRepos: List<Triple<String, String, List<String>>> = listOf(
        Triple("macuguita Maven", "https://maven.macuguita.com/releases/", emptyList()),
        Triple("Minecraft Forge", "https://maven.minecraftforge.net", emptyList()),
        Triple("shedaniel (Cloth Config)", "https://maven.shedaniel.me/", listOf("me.shedaniel")),
        Triple("Xander Maven", "https://maven.isxander.dev/releases/", listOf("dev.isxander")),
        Triple(
            "Terraformers (Mod Menu)",
            "https://maven.terraformersmc.com/releases/",
            listOf("com.terraformersmc", "dev.emi")
        ),
        Triple("Wisp Forest Maven", "https://maven.wispforest.io/releases/", listOf("io.wispforest")),
        Triple("Modrinth", "https://api.modrinth.com/maven", listOf("maven.modrinth")),
        Triple("Parchment Mappings", "https://maven.parchmentmc.org", listOf("org.parchmentmc")),
        Triple("Jitpack", "https://jitpack.io", emptyList()),
    )

    exclusiveRepos.forEach { (name, url, groups) ->
        if (groups.isNotEmpty()) {
            exclusiveContent {
                forRepository {
                    maven {
                        this.name = name
                        setUrl(url)
                    }
                }
                filter {
                    groups.forEach { includeGroupAndSubgroups(it) }
                }
            }
        } else {
            maven {
                this.name = name
                setUrl(url)
            }
        }
    }
}

fun property(name: String): String = rootProject.providers.gradleProperty(name).get()

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    modLocalRuntime("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = project.name
            val remapJar = tasks.named("remapJar")
            artifact(remapJar) { builtBy(remapJar) }
            val remapSourcesJar = tasks.named("remapSourcesJar")
            artifact(remapSourcesJar) { builtBy(remapSourcesJar) }
        }
    }
}
