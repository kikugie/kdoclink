package dev.kikugie.kdoclink.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

abstract class KDocLinkExtension(private val project: Project) {
    private val ksp: KspExtension = project.extensions.getByType()

    var annotation: String
        get() = checkNotNull(ksp.arguments["kdoclink.annotation"]) { "kdoclink.annotation is not set" }
        set(value) = annotation(value)

    operator fun set(id: String, url: String) = entry(id, url)
    operator fun set(id: String, entry: Pair<String, String>) = entry(id, entry.first, entry.second)

    fun annotation(name: String) = ksp.arg("kdoclink.annotation", name)
    fun entry(id: String, url: String) = entry(id, url, null)
    fun entry(id: String, url: String, title: String?) {
        ksp.arg("kdoclink.entry.${id}", url)
        if (title != null) ksp.arg("kdoclink.entry.${id}#title", title)
    }
}