plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
}

group = "dev.kikugie"
version = property("version") as String
base.archivesName.set("kdoclink")

repositories {
    mavenCentral()
}

dependencies {
    fun plugin(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"

    implementation(libs.ksp)
    implementation(libs.plugins.kotlin.ksp.get().let { plugin(it.pluginId, it.version.requiredVersion) })
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("reflect"))
    testImplementation(libs.bundles.test)
}

kotlin {
    jvmToolchain(16)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("kdoclink") {
            id = "dev.kikugie.kdoclink"
            implementationClass = "dev.kikugie.kdoclink.gradle.KDocLinkPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "kikugieMaven"
            url = uri("https://maven.kikugie.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create("basic", BasicAuthentication::class)
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "dev.kikugie"
            artifactId = "kdoclink"
            version = project.version.toString()
            from(components["java"])
        }
    }
}