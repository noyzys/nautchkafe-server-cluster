plugins {
    kotlin("jvm") version "2.1.20"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.nautchkafe.server.cluster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") 
    maven("https://repo.agones.dev/snapshots") 
    maven("https://oss.sonatype.org/content/repositories/snapshots/") // fabric8
}

dependencies {
    // Velocity Proxy
    implementation("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    // Agones SDK
    implementation("dev.agones:agones-sdk-java:1.35.0")

    // Kubernetes Client (fabric8)
    implementation("io.fabric8:kubernetes-client:6.9.2")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // FP concepts
    implementation("io.vavr:vavr:0.10.4")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    shadowJar {
        archiveBaseName.set("server-daemon")
        archiveVersion.set("1.0.0")
        archiveClassifier.set("")
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}