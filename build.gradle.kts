@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.conventions.java)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.glaremasters.me/repository/towny/") {
        mavenContent { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://jitpack.io") {
        mavenContent { includeGroupAndSubgroups("com.github") }
    }

    maven("https://nexus.scarsz.me/content/groups/public/") {
        mavenContent { includeGroup("com.discordsrv") }
    }

    maven("https://repo.earthmc.net/public/") {
        mavenContent { includeGroupAndSubgroups("net.earthmc") }
    }
}

dependencies {
    compileOnly(libs.javalin)
    compileOnly(libs.paper)
    compileOnly(libs.towny)
    compileOnly(libs.quarters)
    compileOnly(libs.discordsrv)
    compileOnly(libs.superbvote)
    compileOnly(libs.mysterymaster.api)
}

tasks {
    processResources {
        val shortCommitId = providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }.standardOutput.asText.get().trim()
        val commitId = providers.exec { commandLine("git", "rev-parse", "HEAD") }.standardOutput.asText.get().trim()

        expand(
            "version" to shortCommitId,
            "commit" to commitId,
            "javalin_version" to libs.versions.javalin.get()
        )
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}
