plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	
	// Database
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.google.cloud.sql:postgres-socket-factory:1.25.2")
	runtimeOnly("com.google.cloud.sql:cloud-sql-connector-jdbc-sqlserver:1.25.2")

	
	// HTTP Client for Circle API
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	
	// Coroutines support
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	
	// UUID support
	implementation("com.fasterxml.uuid:java-uuid-generator:4.3.0")
	
	// Test dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2") // For testing
	testImplementation("io.mockk:mockk:1.13.8") // Mocking library for Kotlin
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
