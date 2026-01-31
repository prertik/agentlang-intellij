plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "ai.agentlang"
version = "0.0.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(17)
}

val ideType = providers.gradleProperty("ideType").orElse("WS")
val ideVersion = providers.gradleProperty("ideVersion").orElse("2025.3.2")
val ideLocalPath = providers.gradleProperty("ideLocalPath")
    .orElse(providers.environmentVariable("IDEA_HOME"))
val osName = System.getProperty("os.name").lowercase()
val defaultLocalPath = when {
    osName.contains("mac") -> file("${System.getProperty("user.home")}/Applications/WebStorm.app/Contents")
    osName.contains("linux") -> file("${System.getProperty("user.home")}/.local/share/JetBrains/Toolbox/apps/WebStorm")
    else -> null
}

dependencies {
    intellijPlatform {
        if (ideLocalPath.isPresent) {
            local(ideLocalPath.get())
        } else if (defaultLocalPath?.exists() == true) {
            local(defaultLocalPath.absolutePath)
        } else {
            val version = ideVersion.get()
            when (ideType.get().uppercase()) {
                "WS", "WEBSTORM" -> webstorm(version)
                "IU", "IDEA", "INTELLIJ" -> intellijIdea(version)
                else -> create(ideType.get(), version)
            }
        }
        bundledPlugin("org.jetbrains.plugins.textmate")
    }
}

intellijPlatform {
    signing {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }
    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

// Agentlang npm package version to bundle
val agentlangNpmVersion = providers.gradleProperty("agentlangVersion").orElse("0.9.5")

// Optional: Use local repo instead of npm (for development)
val agentlangRepoDir = providers.gradleProperty("agentlangRepo")
    .orElse(providers.environmentVariable("AGENTLANG_REPO"))

val npmCacheDir = layout.buildDirectory.dir("npm-cache")
val lspOutputDir = layout.buildDirectory.dir("generated-resources")

val downloadAgentlangFromNpm by tasks.registering {
    group = "build"
    description = "Downloads agentlang package from npm and extracts LSP server"

    val version = agentlangNpmVersion.get()
    val cacheDir = npmCacheDir.get().asFile
    val outputDir = lspOutputDir.get().asFile
    val lspFile = outputDir.resolve("lsp/agentlang-lsp.cjs")

    inputs.property("agentlangVersion", version)
    outputs.file(lspFile)

    doLast {
        // Check if local repo exists and has LSP built
        val localRepoLsp = agentlangRepoDir.orNull?.let { file(it).resolve("out/language/main.cjs") }
        if (localRepoLsp?.exists() == true) {
            logger.lifecycle("Using LSP server from local repo: $localRepoLsp")
            outputDir.resolve("lsp").mkdirs()
            localRepoLsp.copyTo(lspFile, overwrite = true)
            return@doLast
        }

        // Download from npm
        cacheDir.mkdirs()
        outputDir.resolve("lsp").mkdirs()

        val tarball = cacheDir.resolve("agentlang-$version.tgz")

        // Download the tarball from npm if not cached
        if (!tarball.exists()) {
            logger.lifecycle("Downloading agentlang@$version from npm...")
            exec {
                commandLine("npm", "pack", "agentlang@$version", "--pack-destination", cacheDir.absolutePath)
            }
        }

        // Extract the LSP server file from the tarball
        logger.lifecycle("Extracting LSP server from agentlang package...")
        val tempDir = cacheDir.resolve("extract-temp")
        tempDir.mkdirs()

        exec {
            commandLine(
                "tar", "-xzf", tarball.absolutePath,
                "-C", tempDir.absolutePath,
                "package/out/language/main.cjs"
            )
        }

        // Copy to output location
        val extractedFile = tempDir.resolve("package/out/language/main.cjs")
        if (extractedFile.exists()) {
            extractedFile.copyTo(lspFile, overwrite = true)
            tempDir.deleteRecursively()
            logger.lifecycle("LSP server extracted to: $lspFile")
        } else {
            error("Failed to extract LSP server from npm package")
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir(lspOutputDir)
        }
    }
}

tasks {
    processResources {
        dependsOn(downloadAgentlangFromNpm)
    }

    patchPluginXml {
        sinceBuild.set("253")
    }
}
