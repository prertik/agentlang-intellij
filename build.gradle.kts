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

val agentlangRepoDir = providers.gradleProperty("agentlangRepo")
    .orElse(providers.environmentVariable("AGENTLANG_REPO"))
    .orElse(projectDir.resolve("../agentlang").absolutePath)
val repoRoot = file(agentlangRepoDir.get()).canonicalFile
val lspSource = repoRoot.resolve("out/language/main.cjs")

tasks {
    processResources {
        exclude("lsp/agentlang-lsp.cjs")

        from(lspSource) {
            into("lsp")
            rename { "agentlang-lsp.cjs" }
        }

        inputs.file(lspSource)
        doFirst {
            if (!lspSource.exists()) {
                error(
                    "Missing LSP server bundle at $lspSource. " +
                        "Set AGENTLANG_REPO or -PagentlangRepo, then run `npm run build` in that repo."
                )
            }
        }
    }

    patchPluginXml {
        sinceBuild.set("253")
    }
}
