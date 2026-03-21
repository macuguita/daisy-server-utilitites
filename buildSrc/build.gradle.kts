plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
}

dependencies {
    implementation("net.fabricmc:fabric-loom:1.15.+")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.10")
}