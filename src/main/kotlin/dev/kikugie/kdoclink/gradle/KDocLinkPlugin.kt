package dev.kikugie.kdoclink.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.accessors.runtime.extensionOf
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

open class KDocLinkPlugin : Plugin<Project> {
    companion object {
        const val GROUP = "dev.kikugie"
        const val NAME = "kdoclink"
        const val VERSION = "0.1.0"

        val Project.sourceSets: SourceSetContainer?
            get() = project.findProperty("sourceSets") as? SourceSetContainer

        fun plugin(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"
    }

    override fun apply(target: Project) = with(target) {
        dependencies.apply {
            add("ksp", plugin("$GROUP.$NAME", VERSION))
        }

        tasks.all {
            if (this is Jar) duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        val genDir = layout.buildDirectory.dir("generated/ksp").get().asFile.toPath()
        val projDir = layout.projectDirectory.asFile.resolve("src").toPath()
        sourceSets?.all {
            (extensionOf(this, "kotlin") as SourceDirectorySet).configure(projDir, genDir)
        }

        extensions.create<KDocLinkExtension>("kdoclink", project)
        Unit
    }

    private fun SourceDirectorySet.configure(proj: Path, gen: Path) = exclude {
        val path = proj.relativize(it.file.toPath())
        val generated = gen.resolve(path)
        generated.exists() && generated.isRegularFile()
    }
}