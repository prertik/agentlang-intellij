package ai.agentlang.idea

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

class AgentlangLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (!isAgentlangSource(file)) {
            return
        }
        serverStarter.ensureServerStarted(AgentlangLspServerDescriptor(project))
    }
}

private class AgentlangLspServerDescriptor(project: Project) : LspServerDescriptor(project, "Agentlang") {
    override fun createCommandLine(): GeneralCommandLine {
        val serverPath = AgentlangLspServerExtractor.serverPath()
        val commandLine = GeneralCommandLine(nodeExecutable(), serverPath.toString())
            .withCharset(Charsets.UTF_8)
        project.basePath?.let { commandLine.withWorkDirectory(it) }
        return commandLine
    }

    override fun isSupportedFile(file: VirtualFile): Boolean = isAgentlangSource(file)

    override fun getLanguageId(file: VirtualFile): String = "agentlang"

    private fun nodeExecutable(): String {
        if (SystemInfo.isWindows) {
            return "node.exe"
        }
        return "node"
    }
}

private fun isAgentlangSource(file: VirtualFile): Boolean {
    if (file.extension != "al") {
        return false
    }
    return !file.name.equals("config.al", ignoreCase = true)
}

private object AgentlangLspServerExtractor {
    private val log = Logger.getInstance(AgentlangLspServerExtractor::class.java)
    private val cachedPath = AtomicReference<Path?>()

    fun serverPath(): Path {
        cachedPath.get()?.let { return it }

        val targetDir = Paths.get(PathManager.getSystemPath(), "agentlang-lsp", pluginVersion())
        Files.createDirectories(targetDir)
        val targetFile = targetDir.resolve("agentlang-lsp.cjs")

        if (!Files.exists(targetFile)) {
            val resource = AgentlangLspServerExtractor::class.java.classLoader
                .getResourceAsStream("lsp/agentlang-lsp.cjs")
                ?: error("Agentlang LSP server resource missing")
            resource.use { Files.copy(it, targetFile) }
            log.info("Extracted Agentlang LSP server to $targetFile")
        }

        cachedPath.compareAndSet(null, targetFile)
        return targetFile
    }

    private fun pluginVersion(): String {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("ai.agentlang.intellij"))
        return plugin?.version ?: "unknown"
    }
}
