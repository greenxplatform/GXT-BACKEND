plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    api(project(":modules:gxt-common"))
    api(project(":modules:gxt-backtest"))
    api(project(":modules:gxt-market-data"))
    implementation(libs.spring.boot.starter.web)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
