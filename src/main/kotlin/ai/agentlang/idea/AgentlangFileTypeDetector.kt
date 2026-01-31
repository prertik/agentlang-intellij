package ai.agentlang.idea

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

class AgentlangFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    override fun detect(
        file: VirtualFile,
        content: ByteSequence,
        textContent: CharSequence?
    ): FileType? {
        val extension = file.extension ?: return null
        if (!extension.equals("al", ignoreCase = true)) {
            return null
        }
        if (isAgentlangConfigFile(file)) {
            return AgentlangFileType
        }
        val text = when {
            textContent != null && textContent.isNotEmpty() -> textContent
            else -> String(content.toBytes(), StandardCharsets.UTF_8)
        }
        return if (startsWithModuleKeyword(text)) AgentlangFileType else null
    }

    override fun getDesiredContentPrefixLength(): Int = 4096
}
