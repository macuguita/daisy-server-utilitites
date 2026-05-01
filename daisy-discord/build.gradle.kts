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
	id("macuguita-minecraft")
}

repositories {
	maven {
		name = "Kord"
		url = uri("https://snapshots.kord.dev")
		content {
			includeGroup("dev.kord")
		}
	}
}

val bundled: Configuration by configurations.creating

dependencies {
	api(project(mapOf("path" to ":daisy-base", "configuration" to "namedElements")))
	modImplementation(
		"xyz.nucleoid:server-translations-api:${
			providers.gradleProperty("server_translations_api_version").get()
		}"
	)
	modImplementation("dev.kord:kord-core:${providers.gradleProperty("kord_version").get()}")
	bundled("dev.kord:kord-core:${providers.gradleProperty("kord_version").get()}")
}

// source: <https://github.com/PolyHopper/PolyHopper/blob/main/build.gradle.kts>
val includeBlacklist = setOf(
	"commons-validator:commons-validator",
	"commons-beanutils:commons-beanutils",
	"commons-logging:commons-logging",
	"commons-collections:commons-collections",
	"commons-digester:commons-digester",
	"org.jetbrains:annotations",
	"org.jetbrains.kotlin:kotlin-stdlib",
	"org.jetbrains.kotlin:kotlin-stdlib-common",
	"org.jetbrains.kotlin:kotlin-stdlib-jdk7",
	"org.jetbrains.kotlin:kotlin-stdlib-jdk8",
	"org.slf4j:slf4j-api",
//	"com.ibm.icu:icu4j" // Commenting this out to make it obvious, we need this otherwise our app commands fail to sync with discord.
)

afterEvaluate {
	val ignoredModules = mutableSetOf<String>()
	val mavenCoords = bundled.incoming.resolutionResult.allComponents.filter {
		it.id is ModuleComponentIdentifier
	}.map {
		it.id as ModuleComponentIdentifier
	}

	mavenCoords.forEach {
		if (it.module.endsWith("-jvm")) {
			ignoredModules.add("${it.group}:${it.module.substring(0, it.module.length - 4)}")
		}
	}

	mavenCoords.filter { !ignoredModules.contains("${it.group}:${it.module}") }
		.filter { !includeBlacklist.contains("${it.group}:${it.module}") }
		.map { "${it.group}:${it.module}:${it.version}" }
		.forEach {
			project.dependencies.include(it)
		}
}
