plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    api(platform(libs.spring.ai.bom))
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.starter.validation)
    api(libs.jackson.databind)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
