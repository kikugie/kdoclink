import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.symbolProcessorProviders
import dev.kikugie.kdoclink.ksp.KDocSymbolProcessorProvider
import io.kotest.core.spec.style.StringSpec
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

@OptIn(ExperimentalPathApi::class, ExperimentalCompilerApi::class)
class KDocLinkTest : StringSpec({
    "run ksp".config(enabled = false) {
        val files = Path("src/test/kotlin/sample").walk().filter { it.isRegularFile() }
        val properties = buildMap {
            this["kdoclink.annotation"] = "sample.Documentation"
            this["kdoclink.entry.sample"] = "https://www.mydocs.dev/sample_docs"
            this["kdoclink.entry.sample.hello"] = "https://www.mydocs.dev/sample_docs/hello"
            this["kdoclink.entry.sample.hello#title"] = "It says Hello World"
        }

        val compilation = KotlinCompilation().apply {
            sources = files.map { SourceFile.fromPath(it.toFile()) }.toList()
            symbolProcessorProviders = listOf(KDocSymbolProcessorProvider())
            kspArgs.putAll(properties)
        }

        val result = compilation.compile()
        println(result.messages)
    }
})