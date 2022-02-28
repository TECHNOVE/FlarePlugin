plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "co.technove"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")

    implementation("com.github.TECHNOVE:Flare:34637f3f87")
    implementation("com.github.oshi:oshi-core:6.1.2")
}

bukkit {
    main = "co.technove.flareplugin.FlarePlugin"
    apiVersion = "1.16"
    authors = listOf("PaulBGD")
    version = rootProject.version as String

    commands {
        register("flare") {
            description = "Flare profiling command"
            aliases = listOf("profiler", "sampler")
            permission = "flareplugin.command"
            usage = "/flare"
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.16.5")
    }

    shadowJar {
        classifier = ""
    }

}

tasks.create<com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks["shadowJar"] as com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
    prefix = "co.technove.flareplugin.lib"
}
