pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "daisy-server-utilities"

include("daisy-base")
include("daisy-tpa")
include("daisy-home")
include("daisy-warp")
include("daisy-player-management")
