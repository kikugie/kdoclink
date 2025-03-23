## KDoc Link
A Gradle + KSP plugin that provides an organised way to link external documentation
in KDoc comments. This is useful to avoid outdated documentation links in the codebase.

## Setup
`settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
    }
}
```

`build.gradle.kts`:
```kotlin
plugins {
    kotlin("jvm") version "2.1.10"
    id("com.google.devtools.ks") version "2.1.10-1.0.31"
    id("dev.kikugie.kdoclink") version "0.1.0"
}

repositories {
    mavenCentral()
    maven("https://maven.kikugie.dev/releases")
}

kdoclink {
    // ...
}
```

## Configuration
KDoc Link requires an annotation with a single `String` argument to provide documentation keys:
```kotlin
package com.example

@Repeatable
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class MyDocumentation(val id: String)
```

Next, register the annotation reference in the plugin extension:
```kotlin
kdoclink {
    // in Groovy use annotation("com.example.MyDocumentation")
    annotation = "com.example.MyDocumentation"
}
```

## Processing
In the `kdoclink` extension you can register the inserted links and placeholder text.
```kotlin
kdoclink {
    // in Groovy use annotation("com.example.MyDocumentation")
    annotation = "com.example.MyDocumentation"
    
    /* in Groovy use:
       entry("sample", "https://docs.myproject.com/sample")
       entry("sample.hello", "https://docs.myproject.com/sample_hello", "How to say hello")
     */
    this["sample"] = "https://docs.myproject.com/sample"
    this["sample.hello"] = "https://docs.myproject.com/sample_hello" to "How to say hello"
}
```

With this example:
```kotlin
/**
 * This class is used as an example.
 * It's very good at its work!
 */
@MyDocumentation("sample")
interface Sample {
    @MyDocumentation("sample.hello")
    fun hello()
}
```

The following file will be created in generated project sources:
```kotlin
/**
 * This class is used as an example.
 * It's very good at its work!
 * @see <a href="https://docs.myproject.com/sample">Sample documentation</a>
 */
@MyDocumentation("sample")
interface Sample {
    /**
     * @see <a href="https://docs.myproject.com/sample_hello">How to say hello</a>
     */
    @MyDocumentation("sample.hello")
    fun hello()
}
```
The generated class will replace the one in your source code during project compilations,
and will be included in all `.jar` files.