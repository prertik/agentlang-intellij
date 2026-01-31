package ai.agentlang.idea

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.plugins.textmate.TextMateBackedFileType
import javax.swing.Icon

object AgentlangFileType : LanguageFileType(AgentlangLanguage), TextMateBackedFileType {
    override fun getName(): String = "Agentlang"

    override fun getDescription(): String = "Agentlang language file"

    override fun getDefaultExtension(): String = "al"

    override fun getIcon(): Icon = AgentlangIcons.FILE
}
