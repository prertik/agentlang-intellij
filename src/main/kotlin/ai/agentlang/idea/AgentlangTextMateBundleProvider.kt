package ai.agentlang.idea

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

class AgentlangTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        val bundlePath = AgentlangTextMateBundleExtractor.bundlePath()
        return listOf(TextMateBundleProvider.PluginBundle("Agentlang", bundlePath))
    }
}

private object AgentlangTextMateBundleExtractor {
    private val log = Logger.getInstance(AgentlangTextMateBundleExtractor::class.java)
    private val cachedPath = AtomicReference<Path?>()

    fun bundlePath(): Path {
        cachedPath.get()?.let { return it }

        val targetDir = Paths.get(PathManager.getSystemPath(), "agentlang-textmate", pluginVersion())
        val bundleDir = targetDir.resolve("Agentlang.tmbundle")
        val syntaxDir = bundleDir.resolve("Syntaxes")
        Files.createDirectories(syntaxDir)

        copyResource("textmate/Agentlang.tmbundle/info.plist", bundleDir.resolve("info.plist"))
        copyResource(
            "textmate/Agentlang.tmbundle/Syntaxes/agentlang.tmLanguage.json",
            syntaxDir.resolve("agentlang.tmLanguage.json")
        )

        cachedPath.compareAndSet(null, bundleDir)
        log.info("Extracted Agentlang TextMate bundle to $bundleDir")
        return bundleDir
    }

    private fun copyResource(resourcePath: String, target: Path) {
        if (Files.exists(target)) {
            return
        }
        val resource = AgentlangTextMateBundleExtractor::class.java.classLoader
            .getResourceAsStream(resourcePath)
            ?: error("Agentlang TextMate resource missing: $resourcePath")
        resource.use { Files.copy(it, target) }
    }

    private fun pluginVersion(): String {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("ai.agentlang"))
        return plugin?.version ?: "unknown"
    }
}
