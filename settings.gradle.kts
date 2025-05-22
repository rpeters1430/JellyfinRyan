pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.2.2" apply false
        id("org.jetbrains.kotlin.android") version "2.1.21" apply false
        id("dagger.hilt.android.plugin") version "2.47" apply false
        id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
        id("androidx.compose.compiler") version "1.5.7" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "JellyfinTV"
include(":app")