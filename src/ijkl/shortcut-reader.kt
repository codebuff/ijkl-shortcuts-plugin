package ijkl

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.ui.KeyStrokeAdapter
import org.w3c.dom.Node
import java.io.InputStream
import javax.swing.KeyStroke
import javax.xml.parsers.DocumentBuilderFactory

fun InputStream.readShortcutsData(): List<ShortcutData> {
    return use { readShortcutsDataFrom(this) }
}

private fun readShortcutsDataFrom(inputStream: InputStream): List<ShortcutData> {
    fun Node.getAttribute(name: String): String? =
        attributes.getNamedItem(name)?.nodeValue

    fun Node.children(): List<Node> {
        val result = ArrayList<Node>()
        0.until(childNodes.length).forEach { i ->
            result.add(childNodes.item(i))
        }
        return result
    }

    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document = builder.parse(inputStream)
    val keymapTag = document.children().find { it.nodeName == "keymap" } ?: error("")

    return keymapTag.children()
        .filter { it.nodeName == "action" }
        .map {
            ShortcutData(
                it.getAttribute("id") ?: "",
                it.children()
                    .filter { it.nodeName == "keyboard-shortcut" }
                    .map { it.getAttribute("first-keystroke")?.toKeyboardShortcut() }
                    .filterNotNull()
            )
        }
}

fun String?.toKeyboardShortcut(): Shortcut? {
    if (this == null || isBlank()) return null

    val firstKeystroke: KeyStroke?
    val secondKeystroke: KeyStroke?
    if (contains(",")) {
        val i = indexOf(",")
        firstKeystroke = KeyStrokeAdapter.getKeyStroke(substring(0, i).trim())
        secondKeystroke = KeyStrokeAdapter.getKeyStroke(substring(i + 1).trim())
    } else {
        firstKeystroke = KeyStrokeAdapter.getKeyStroke(this)
        secondKeystroke = null
    }
    if (firstKeystroke == null) error("Invalid keystroke '$this'")

    return KeyboardShortcut(firstKeystroke, secondKeystroke)
}