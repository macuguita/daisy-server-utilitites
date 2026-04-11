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
	`java-library`
	`maven-publish`
}

val base = extensions.getByType<BasePluginExtension>()

loom {
	runs {
		register("rootClient") {
			client()
		}
	}
}

subprojects {
	apply(plugin = "macuguita-minecraft")
}

subprojects.forEach { sub ->
	dependencies {
		add(
			"api", project(
				mapOf(
					"path" to sub.path,
				)
			)
		)
	}
}

val nestedJars by configurations.creating
nestedJars.setTransitive(false)

dependencies {
	subprojects.forEach { sub ->
		nestedJars(project("${sub.path}"))
	}
}

loom.nestJars(tasks.jar, nestedJars)

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

// Publishing (fat jar)
//publishing {
//	publications {
//		named<MavenPublication>("mavenJava") {
//			artifactId = base.archivesName.get()
//
//			val remapJar = tasks.named("remapJar")
//			artifact(remapJar) { builtBy(remapJar) }
//
//			val remapSourcesJar = tasks.named("remapSourcesJar")
//			artifact(remapSourcesJar) { builtBy(remapSourcesJar) }
//		}
//	}
//}



