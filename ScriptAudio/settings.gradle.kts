/**
 * settings.gradle.kts
 *
 * 프로젝트 전체 플러그인 관리
 */

pluginManagement {

    repositories {

        google()

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

rootProject.name = "ScriptAudio"

include(":app")