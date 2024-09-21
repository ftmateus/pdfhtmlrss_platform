import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.16"
	id("io.spring.dependency-management") version "1.1.3"
//	id("pdfhtmlrss_platform.xmlrss")
	//id("org.graalvm.buildtools.native") version "0.9.27"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "pt.unl.fct.di"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_11
}

tasks.register<Wrapper>("wrapper") {
	gradleVersion = "5.6.4"
}
tasks.register("prepareKotlinBuildScriptModel"){}


configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	flatDir {
		dirs("libs")
	}
}

dependencies {
//	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation(project(":xmlrss"))
	implementation("org.jsoup:jsoup:1.18.1")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.mariadb.jdbc:mariadb-java-client")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	//TODO change to testImplementation
	implementation("com.h2database:h2:1.3.148")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	compileOnly("org.projectlombok:lombok")
	//developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
	implementation("org.apache.pdfbox:pdfbox-tools:2.0.25")
	implementation("net.sf.cssbox:pdf2dom:2.0.1")
	implementation("com.itextpdf:itextpdf:5.5.10")
	implementation("com.itextpdf:itext7-core:7.1.15")
	implementation("com.itextpdf.tool:xmlworker:5.5.10")
	implementation("org.apache.poi:poi-ooxml:3.15")
	implementation("org.apache.poi:poi-scratchpad:3.15")
	implementation("org.apache.santuario:xmlsec:2.0.8")
	implementation("org.bouncycastle:bcprov-jdk18on:1.76")
	implementation("redis.clients:jedis:5.1.5")
	compileOnly("eu.europa.ec.joinup.sd-dss:dss-pades:6.0")
//	implementation("com.e-iceblue:spire.pdf:10.6.2")

	implementation("org.jsoup:jsoup:1.16.2")
	implementation("org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.3.1")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}