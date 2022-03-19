import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val logbackVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project
val arrowVersion: String by project
val daprVersion: String by project
val grpcVersion: String by project
val grpcKotlinVersion: String by project
val protobufVersion: String by project

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.18"
    `idea`
}

group = "com.joram"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

apply(plugin =  "org.gradle.idea")
idea {
    this.module.generatedSourceDirs.add(File("build/generated/source/proto/main/java"))
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("ch.qos.logback:logback-classic:$logbackVersion")
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-gson:$ktorVersion")
    api("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    api("io.dapr:dapr-sdk:$daprVersion")
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    api("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    api("io.ktor:ktor-server-tests:$ktorVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets {
    named("main") {
        proto.srcDir("proto")
    }
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    dependsOn("generateProto")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
