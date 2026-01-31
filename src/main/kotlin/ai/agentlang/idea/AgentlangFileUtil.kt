package ai.agentlang.idea

import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

internal const val AGENTLANG_CONFIG_FILE_NAME = "config.al"

internal fun isAgentlangConfigFile(file: VirtualFile?): Boolean {
    val name = file?.name ?: return false
    return name.equals(AGENTLANG_CONFIG_FILE_NAME, ignoreCase = true)
}

internal fun readFilePrefix(file: VirtualFile, maxBytes: Int = 4096): String? {
    return try {
        file.inputStream.use { stream ->
            val buffer = ByteArray(maxBytes)
            val read = stream.read(buffer)
            if (read <= 0) {
                ""
            } else {
                String(buffer, 0, read, StandardCharsets.UTF_8)
            }
        }
    } catch (_: Exception) {
        null
    }
}

internal fun startsWithModuleKeyword(text: CharSequence): Boolean {
    var i = 0
    val len = text.length
    while (i < len) {
        when (text[i]) {
            ' ', '\t', '\r', '\n' -> i++
            '/' -> {
                if (i + 1 >= len) return false
                val next = text[i + 1]
                if (next == '/') {
                    i += 2
                    while (i < len && text[i] != '\n') i++
                } else if (next == '*') {
                    i += 2
                    while (i + 1 < len && !(text[i] == '*' && text[i + 1] == '/')) i++
                    i = (i + 2).coerceAtMost(len)
                } else {
                    return false
                }
            }

            else -> break
        }
    }
    if (i + 6 > len) return false
    if (!text.regionMatches(i, "module", 0, 6, ignoreCase = false)) return false
    val end = i + 6
    return end == len || !text[end].isLetterOrDigit() && text[end] != '_'
}
