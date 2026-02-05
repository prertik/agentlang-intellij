package ai.agentlang.idea

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class AgentlangFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    override fun detect(
        file: VirtualFile,
        content: ByteSequence,
        textContent: CharSequence?,
    ): FileType? {
        val extension = file.extension ?: return null
        return if (extension.equals("al", ignoreCase = true)) {
            AgentlangFileType
        } else {
            null
        }
    }

    override fun getDesiredContentPrefixLength(): Int = 0
}
