pluginManagement {
    repositories {
        google()            // ✅ needed for Android Gradle Plugin
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AudioManagerPersonal"
include(":app")
