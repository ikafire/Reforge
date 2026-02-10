pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Reforge"

include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:ui")

// Feature modules
include(":feature:exercises")
include(":feature:workout")
include(":feature:history")
include(":feature:profile")
include(":feature:analytics")
include(":feature:measure")
include(":feature:settings")
