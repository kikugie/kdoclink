package dev.kikugie.kdoclink.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

class KDocSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        KDocSymbolProcessor(environment.codeGenerator, environment.logger, environment.options)
}

class KDocSymbolProcessor(val generator: CodeGenerator, val logger: KSPLogger, val options: Map<String, String>) : SymbolProcessor {
    val docAnnotationName: String? = options["kdoclink.annotation"] ?: run {
        logger.warn("No documentation annotation 'kdoclink.annotation' provided"); null
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (docAnnotationName == null) return emptyList()

        var symbols = resolver.getSymbolsWithAnnotation(docAnnotationName).filterIsInstance<KSDeclaration>()
        if (symbols.any { it.annotations.any { it.isDoc && !it.isValidDoc } })
            return symbols.toList()

        symbols = resolver.getSymbolsWithAnnotation(docAnnotationName).filterIsInstance<KSDeclaration>()
        val injections = symbols.mapNotNull { dec ->
            if (dec.containingFile == null || "build/generated" in dec.containingFile!!.filePath)
                return@mapNotNull null

            val docs = dec.annotations.filter { it.isDoc }
                .mapNotNull { it.docInfo }
                .toList()
            if (docs.isEmpty()) null
            else KDocPoints(dec, docs)
        }

        for ((file, value) in injections.groupBy { it.host.containingFile!! })
            inject(file, value)
        return emptyList()
    }

    fun inject(file: KSFile, content: List<KDocPoints>) {
        val contents = Path(file.filePath).runCatching<Path, String>(Path::readText).getOrElse {
            logger.error("Failed to process ${file.filePath}: $it"); return
        }
        val modified = KDocTransformer(contents, content).transform()
        generator.createNewFile(Dependencies(true, file), file.packageName.asString(), file.fileName.removeSuffix(".kt")).writer().use {
            it.write(modified)
        }
    }

    val KSAnnotation.isDoc: Boolean get() = when {
        docAnnotationName!!.endsWith(shortName.asString()).not() -> false
        annotationType.resolve().declaration.qualifiedName?.asString() != docAnnotationName -> false
        else -> true
    }

    val KSAnnotation.isValidDoc: Boolean get() = when {
        arguments.size != 1 || arguments.first().value !is String -> false
            .also { logger.warn("@$docAnnotationName must have a single string argument") }
        else -> true
    }

    val KSAnnotation.docInfo: KDocLink? get() {
        val id = arguments.first().value as String
        val url = options["kdoclink.entry.$id"] ?: return run {
            logger.warn("No documentation entry for '$id' provided"); null
        }
        val title = options["kdoclink.entry.$id#title"] ?: run {
            val parent = (parent as KSDeclaration).simpleName.asString()
            "$parent documentation"
        }
        return KDocLink(url, title)
    }
}