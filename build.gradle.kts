import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.4.21"
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "me.window"
version = "1.5.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("net.luckperms:api:5.5")
    implementation("xyz.xenondevs.invui:invui:2.5.1")
    implementation("xyz.xenondevs.invui:invui-kotlin:2.5.1")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

var targetJavaVersion = 25
val javaVersion = JavaVersion.toVersion(targetJavaVersion)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin { // Extension for easy setup
    jvmToolchain(targetJavaVersion) // Target version of generated JVM bytecode. See 7️⃣
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveClassifier.set("shadowless")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    relocate("org.bstats", "cc.jaysi.bstats")
}