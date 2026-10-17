import java.net.URI

plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
}

repositories {
	maven {
		name = "Modrinth"
		url = URI("https://api.modrinth.com/maven")
	}
	maven {
		name = "Fuzs Mod Resources"
		url = URI("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
	}
	maven {
		// location of the maven that hosts JEI files since January 2023
		name = "Jared's maven"
		url = URI("https://maven.blamejared.com/")
	}
	maven {
		url = URI("https://cursemaven.com")
	}
	maven {
		name = "GeckoLib"
		url = URI("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
		content {
			includeGroup("software.bernie.geckolib")
		}
	}
}

base {
	archivesName = providers.gradleProperty("archives_base_name")
}

fabricApi {
	configureTests {
		createSourceSet = true
		modId = "food_test"
		enableClientGameTests = true
	}
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.11.4"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
	modImplementation("software.bernie.geckolib:geckolib-fabric-${providers.gradleProperty("minecraft_version").get()}:${
		providers.gradleProperty(
			"geckolib_version"
		).get()
	}")
	modImplementation ("maven.modrinth:kaleidoscope-cookery-refabricated:${providers.gradleProperty("kaleidoscope_cookery_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	modImplementation ("maven.modrinth:kaleidoscope-tavern-refabricated:${providers.gradleProperty("kaleidoscope_tavern_version").get()}-fabric+mc${providers.gradleProperty("minecraft_version").get()}")
	modImplementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${providers.gradleProperty("forge_config_api_version").get()}")
	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	// Punchy
	compileOnly("maven.modrinth:8aoMKplv:kburaezy")
}

tasks.test {
	useJUnitPlatform()
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
