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

rootProject.name = "txt2ttsreader"
include(    ":app",
    ":core:designsystem",
    ":core:network",
    ":core:datastore",
    ":core:database",
    ":data",
    ":domain",
    ":feature:reader",
    ":feature:auth",
    ":benchmark")
include(":feature")
include(":coredesignsystem")
