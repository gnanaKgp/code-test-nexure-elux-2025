plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.common)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.ktor.test)
    testImplementation(kotlin("test"))
    testImplementation(libs.testcontainer.mongo)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
application { mainClass = "io.nexure.discount.ApplicationKt" }

tasks {
    test {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
        
        // Force Testcontainers to use the standard Docker Desktop pipe and API version
        systemProperty("docker.host", "npipe:////./pipe/docker_engine")
        systemProperty("testcontainers.checks.disable", "true")
        
        // Environment variables for additional reliability
        environment("DOCKER_HOST", "npipe:////./pipe/docker_engine")
        environment("DOCKER_API_VERSION", "1.45")
        environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    }
}
