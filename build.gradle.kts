plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

base {
    archivesName = project.property("archives_base_name") as String
    group = project.property("maven_group") as String
    version = project.property("mod_version") as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version") as String}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version") as String}")

    // Meteor
    implementation("meteordevelopment:meteor-client:${project.property("meteor_version") as String}-SNAPSHOT")
    compileOnly("meteordevelopment:baritone:${project.property("minecraft_version") as String}-SNAPSHOT")
}

loom {
    accessWidenerPath = file("src/main/resources/blackout.accesswidener")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version")
        )

        inputs.properties(propertyMap)

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    withType<JavaCompile> {
        options.release = 25
        options.encoding = "UTF-8"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}
