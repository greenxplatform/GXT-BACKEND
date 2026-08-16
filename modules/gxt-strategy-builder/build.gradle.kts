plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    api(project(":modules:gxt-common"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(platform(libs.spring.ai.bom))
    implementation(libs.spring.ai.anthropic)
    implementation(libs.spring.ai.openai)
    implementation(libs.jackson.databind)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
