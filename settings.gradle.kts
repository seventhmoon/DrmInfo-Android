pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.android.gms.strict-version-matcher-plugin") {
                useModule("com.google.android.gms:strict-version-matcher-plugin:${requested.version}")
            }
            if (requested.id.id == "com.google.firebase.firebase-perf") {
                useModule("com.google.firebase:perf-plugin:${requested.version}")
            }
            if (requested.id.id == "com.google.firebase.crashlytics") {
                useModule("com.google.firebase:firebase-crashlytics-gradle:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DrmInfo-Android"
include(":app")
