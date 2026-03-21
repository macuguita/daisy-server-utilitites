version = rootProject.findProperty("${project.name}-version") as String?
        ?: throw NullPointerException("No version for ${project.name}")

dependencies {
    api(project(mapOf("path" to ":daisy-base", "configuration" to "namedElements")))
}