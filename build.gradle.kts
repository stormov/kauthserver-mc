plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

group = "my.stormov.kauthserver"
version = "1.0.0"
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.security.crypto)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.jackson.kotlin)
    implementation(libs.caffeine)
    runtimeOnly(libs.postgresql)

    // implementation(libs.spring.boot.starter.data.redis)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.test { useJUnitPlatform() }