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
        Triple("Terraformers (Mod Menu)", "https://maven.terraformersmc.com/releases/", listOf("com.terraformersmc", "dev.emi")),
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