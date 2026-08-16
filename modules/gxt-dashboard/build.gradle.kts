plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    api(project(":modules:gxt-common"))
    implementation(libs.spring.boot.starter.web)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
