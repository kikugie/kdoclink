package dev.kikugie.kdoclink.ksp

import com.google.devtools.ksp.symbol.FileLocation

class KDocTransformer(val content: String, val components: List<KDocPoints>) {
    val positions = components.associateBy { (it.host.annotations.first().location as FileLocation).lineNumber }

    fun transform() = buildString(content.length) {
        content.forEachIndexedLine { index, line ->
            val (host, infos) = positions[index] ?: run { appendLine(line); return@forEachIndexedLine }
            val indent = line.takeWhile { it.isWhitespace() }
            val kdoc = constructNewDoc(host.docString, infos, indent)

            if (host.docString == null) appendLine(kdoc)
            else replaceOldDoc(kdoc)
            appendLine(line)
        }
    }

    private fun constructNewDoc(existing: String?, infos: List<KDocLink>, indent: String): String = buildString {
        appendLine("$indent/**")
        if (existing != null) for (it in existing.trimStart().lines())
            appendLine("$indent * $it")
        for (it in infos)
            appendLine("$indent * @see <a href=\"${it.url.replace("\"", "\\\"")}\">${it.title}</a>")
        append("$indent */")
    }

    private fun StringBuilder.replaceOldDoc(new: String) {
        val start = lastIndexOf("/**")
        val end = indexOf("*/", start) + 2
        replace(start, end, new.trimStart())
    }

    private inline fun String.forEachIndexedLine(action: (Int, String) -> Unit) {
        var line = 1
        for (it in lineSequence()) action(line++, it)
    }
}