pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.springframework.boot") version "3.2.4"
        id("io.spring.dependency-management") version "1.1.4"
    }
}

rootProject.name = "server"
include("user-service")
include("discovery-service")
include("gateway-service")
include("config-service")
