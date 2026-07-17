@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.conventions.java)
    alias(libs.plugins.shadow)
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

    maven("https://repo.codemc.io/repository/maven-public/") {
        mavenContent { includeGroup("com.ghostchu") }
    }

    maven("https://nexus.neetgames.com/repository/maven-releases/") {
        mavenContent { includeGroup("com.gmail.nossr50.mcMMO") }
    }

    maven("https://repo.warriorrr.dev/releases") {
        mavenContent { includeGroupAndSubgroups("dev.warriorrr") }
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
    compileOnly(libs.quickshop) {
        exclude("*")
    }
    compileOnly(libs.quickshop.api)
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    compileOnly(libs.mcmmo) {
        exclude("com.sk89q.worldguard")
        exclude("com.comphenix.protocol")
    }
    compileOnly(libs.lynchpin.pursuits)
    compileOnly(libs.lynchpin.towny)
    compileOnly(libs.lynchpin.advancements)
    implementation(libs.inventories)
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        relocate("com.zaxxer.hikari", "net.earthmc.emcapi.libs.hikari")
        relocate("dev.warriorrr.inventories", "net.earthmc.emcapi.libs.inventories")
    }

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
