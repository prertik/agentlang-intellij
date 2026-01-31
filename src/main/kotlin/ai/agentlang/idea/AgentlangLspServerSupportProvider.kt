package ai.agentlang.idea

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.LspServerSupportProvider
import org.eclipse.lsp4j.PublishDiagnosticsParams
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicReference

class AgentlangLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (isAgentlangSource(file)) {
            serverStarter.ensureServerStarted(AgentlangLspServerDescriptor(project))
        }
    }
}

private class AgentlangLspServerDescriptor(project: Project) : LspServerDescriptor(project, "Agentlang") {
    override fun createCommandLine(): GeneralCommandLine {
        val serverPath = AgentlangLspServerExtractor.serverPath()
        val commandLine = GeneralCommandLine(nodeExecutable(), serverPath.toString(), "--stdio")
            .withCharset(Charsets.UTF_8)
        project.basePath?.let { commandLine.withWorkDirectory(it) }
        return commandLine
    }

    override fun isSupportedFile(file: VirtualFile): Boolean = isAgentlangSource(file)

    override fun getLanguageId(file: VirtualFile): String = "agentlang"

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient {
        val filteringHandler = object : LspServerNotificationsHandler by handler {
            override fun publishDiagnostics(params: PublishDiagnosticsParams) {
                if (isConfigUri(params.uri)) {
                    return
                }
                handler.publishDiagnostics(params)
            }
        }
        return Lsp4jClient(filteringHandler)
    }

    private fun nodeExecutable(): String {
        if (SystemInfo.isWindows) {
            return "node.exe"
        }
        return "node"
    }
}

private fun isAgentlangSource(file: VirtualFile): Boolean {
    if (file.extension?.equals("al", ignoreCase = true) != true) {
        return false
    }
    if (isAgentlangConfigFile(file)) {
        return false
    }
    return true
}

private fun isConfigUri(uri: String): Boolean {
    return try {
        val fileName = Paths.get(URI(uri)).fileName?.toString()
        fileName != null && fileName.equals(AGENTLANG_CONFIG_FILE_NAME, ignoreCase = true)
    } catch (_: Exception) {
        uri.endsWith("/$AGENTLANG_CONFIG_FILE_NAME", ignoreCase = true) ||
                uri.endsWith("\\$AGENTLANG_CONFIG_FILE_NAME", ignoreCase = true)
    }
}

private object AgentlangLspServerExtractor {
    private val log = Logger.getInstance(AgentlangLspServerExtractor::class.java)
    private val cachedPath = AtomicReference<Path?>()

    fun serverPath(): Path {
        cachedPath.get()?.let { return it }

        val targetDir = Paths.get(PathManager.getSystemPath(), "agentlang-lsp", pluginVersion())
        Files.createDirectories(targetDir)
        val targetFile = targetDir.resolve("agentlang-lsp.cjs")

        val resource = AgentlangLspServerExtractor::class.java.classLoader
            .getResourceAsStream("lsp/agentlang-lsp.cjs")
            ?: error("Agentlang LSP server resource missing")
        resource.use { Files.copy(it, targetFile, StandardCopyOption.REPLACE_EXISTING) }
        log.info("Extracted Agentlang LSP server to $targetFile")

        cachedPath.compareAndSet(null, targetFile)
        return targetFile
    }

    private fun pluginVersion(): String {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("ai.agentlang"))
        return plugin?.version ?: "unknown"
    }
}
