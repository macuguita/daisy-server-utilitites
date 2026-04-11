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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	kotlin("jvm") version "2.3.10"
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
	mavenCentral()

	exclusiveContent {
		forRepository {
			maven {
				name = "FabricMC's Maven"
				url = uri("https://maven.fabricmc.net/")
			}
		}
		filter {
			includeGroupAndSubgroups("net.fabricmc")
		}
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
		languageVersion = KotlinVersion.KOTLIN_2_3
		apiVersion = KotlinVersion.KOTLIN_2_3
	}
}

dependencies {
	// https://maven.fabricmc.net/net/fabricmc/fabric-loom/
	implementation("net.fabricmc:fabric-loom:1.16.+")
	// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
}
