import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "plenix"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.liuwj.ktorm:ktorm-core:3.1.0")
    runtimeOnly("org.postgresql:postgresql:42.5.0")
    implementation("org.jetbrains.kotlin:kotlin-script-util:1.7.10")
    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}