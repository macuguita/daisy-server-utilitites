plugins {
	id("mod-convention")
	`base`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

base {
	archivesName = providers.gradleProperty("archives_base_name")
}

subprojects {
	apply(plugin = "mod-convention")
}

dependencies {
	afterEvaluate {
		subprojects.forEach { sub ->
			api(project(mapOf("path" to sub.path, "configuration" to "namedElements")))
		}
	}
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
	subprojects.forEach { sub ->
		val subRemapJar = sub.tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar")
		dependsOn(subRemapJar)
		nestedJars.from(subRemapJar.flatMap { it.archiveFile })
	}
}

tasks.named<Jar>("jar") {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

publishing {
	publications {
		named<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			val remapJar = tasks.named("remapJar")
			artifact(remapJar) { builtBy(remapJar) }
			val remapSourcesJar = tasks.named("remapSourcesJar")
			artifact(remapSourcesJar) { builtBy(remapSourcesJar) }
		}
	}
}