plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "ai.agentlang"
version = "0.1.0"

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
val defaultLocalPath = file("${System.getProperty("user.home")}/Applications/WebStorm.app/Contents")

dependencies {
    intellijPlatform {
        if (ideLocalPath.isPresent) {
            local(ideLocalPath.get())
        } else if (defaultLocalPath.exists()) {
            local(defaultLocalPath.absolutePath)
        } else {
            val version = ideVersion.get()
            when (ideType.get().uppercase()) {
                "WS", "WEBSTORM" -> webstorm(version)
                "IU", "IDEA", "INTELLIJ" -> intellijIdea(version)
                else -> create(ideType.get(), version)
            }
        }
    }
}

val agentlangRepoDir = providers.gradleProperty("agentlangRepo")
    .orElse(providers.environmentVariable("AGENTLANG_REPO"))
    .orElse(projectDir.resolve("../agentlang").absolutePath)
val repoRoot = file(agentlangRepoDir.get()).canonicalFile
val lspSource = repoRoot.resolve("out/language/main.cjs")
val textmateSource = repoRoot.resolve("syntaxes/agentlang.tmLanguage.json")

tasks {
    processResources {
        exclude("lsp/agentlang-lsp.cjs")
        exclude("textmate/Agentlang.tmbundle/Syntaxes/agentlang.tmLanguage.json")

        from(lspSource) {
            into("lsp")
            rename { "agentlang-lsp.cjs" }
        }
        from(textmateSource) {
            into("textmate/Agentlang.tmbundle/Syntaxes")
        }

        inputs.file(lspSource)
        inputs.file(textmateSource)
        doFirst {
            if (!lspSource.exists()) {
                error(
                    "Missing LSP server bundle at $lspSource. " +
                        "Set AGENTLANG_REPO or -PagentlangRepo, then run `npm run build` in that repo."
                )
            }
            if (!textmateSource.exists()) {
                error(
                    "Missing TextMate grammar at $textmateSource. " +
                        "Set AGENTLANG_REPO or -PagentlangRepo if the repo is not at ../agentlang."
                )
            }
        }
    }

    patchPluginXml {
        sinceBuild.set("253")
        untilBuild.set("999.*")
    }
}
