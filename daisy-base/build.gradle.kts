version = rootProject.findProperty("${project.name}-version") as String?
    ?: throw NullPointerException("No version for ${project.name}")

dependencies {
}
