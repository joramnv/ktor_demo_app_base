import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val arrow_version: String by project
val dapr_version: String by project
val grpcVersion: String by project
val grpcKotlinVersion: String by project
val protobufVersion: String by project

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.13"
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
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    api("io.ktor:ktor-server-netty:$ktor_version")
    api("ch.qos.logback:logback-classic:$logback_version")
    api("io.ktor:ktor-server-core:$ktor_version")
    api("io.ktor:ktor-gson:$ktor_version")
    api("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
    api("io.dapr:dapr-sdk:$dapr_version")
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    api("io.ktor:ktor-server-tests:$ktor_version")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
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
    sourceCompatibility = JavaVersion.VERSION_1_7
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
