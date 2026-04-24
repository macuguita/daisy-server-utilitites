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
	id("net.fabricmc.fabric-loom")
	kotlin("jvm")
	`maven-publish`
	`java-library`
}

group = rootProject.group
version = rootProject.providers
	.gradleProperty("${project.name}-version")
	.orNull
	?: error("Missing property: ${project.name}-version")

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
		Triple("Nucleoid", "https://maven.nucleoid.xyz", listOf("xyz.nucleoid", "eu.pb4")),
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
	implementation("net.fabricmc:fabric-loader:$loaderVersion")
	implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
	implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

	localRuntime("com.terraformersmc:modmenu:${property("modmenu_version")}")

	compileOnly("org.jspecify:jspecify:1.0.0")
}

java {
	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

kotlin {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
	}
}

tasks.processResources {
	inputs.property("version", project.version)
	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}
