rootProject.name = "gxt-backend"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":modules:gxt-common",
    ":modules:gxt-identity",
    ":modules:gxt-strategy-builder",
    ":modules:gxt-backtest",
)
