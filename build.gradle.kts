plugins {
    idea
    `java-library`
    `maven-publish`
    id("net.kyori.indra") version "3.1.3"
    id("net.kyori.indra.git") version "3.1.3"
    id("net.kyori.indra.publishing") version "3.1.3"
    id("io.freefair.lombok") version "8.4"
}

indra {
    github("GeyserMC", "MCProtocolLib")

    mitLicense()
    publishReleasesTo("opencollab-release-repo", "https://repo.opencollab.dev/maven-releases/")
    publishSnapshotsTo("opencollab-snapshot-repo", "https://repo.opencollab.dev/maven-snapshots/")

    configurePublications {
        pom {
            name = "MCProtocolLib"
            url = "https://github.com/GeyserMC/MCProtocolLib/"
            organization {
                name = "GeyserMC"
                url = "https://github.com/GeyserMC"
            }
            developers {
                developer {
                    id = "steveice10"
                    name = "Steveice10"
                    email = "Steveice10@gmail.com"
                }
                developer {
                    id = "GeyserMC"
                    name = "GeyserMC"
                    url = "https://geysermc.org/"
                }
            }
        }

        versionMapping {
            usage(Usage.JAVA_API) { fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) }
            usage(Usage.JAVA_RUNTIME) { fromResolutionResult() }
        }
    }

    javaVersions {
        target(16)
        strictVersions()
        testWith(16)
        minimumToolchain(16)
    }
}

val repoName = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
publishing {
    repositories {
        maven("https://repo.opencollab.dev/${repoName}/") {
            credentials.username = System.getenv("OPENCOLLAB_USERNAME")
            credentials.password = System.getenv("OPENCOLLAB_PASSWORD")
            name = "opencollab"
        }
    }
}

dependencies {
    // Minecraft related libraries
    api(libs.opennbt)
    api(libs.mcauthlib)

    // Kyori adventure
    api(libs.bundles.adventure)

    // Math utilities
    api(libs.bundles.math)

    // Stripped down fastutil
    api(libs.bundles.fastutil)

    // Netty
    api(libs.bundles.netty)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Test dependencies
    testImplementation(libs.junit.jupiter)
}

group = "com.github.steveice10"
version = "1.20.2-1-SNAPSHOT"
description = "MCProtocolLib is a simple library for communicating with Minecraft clients and servers."

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-serial,-processing")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    title = "MCProtocolLib Javadocs"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

