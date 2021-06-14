import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  application
  eclipse
  idea
  kotlin("jvm") version BuildVersions.kotlin
  kotlin("plugin.serialization") version BuildVersions.kotlin
  id("com.github.johnrengelman.shadow") version BuildVersions.shadowMan
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()


group = BuildConstants.groupId
version = BuildConstants.targetVersion

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

repositories {
  mavenCentral()
}

val mainVerticleName = BuildConstants.mainVerticleName
val launcherClassName = BuildConstants.launcherClassName

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform(BuildDependencies.vertxDepChain))
  implementation(BuildDependencies.vertxCore)
  implementation(BuildDependencies.vertxPostGre)
  implementation(BuildDependencies.vertxWebOpenApi)
  implementation(BuildDependencies.vertxWeb)

  implementation(BuildDependencies.guice)

  implementation(BuildDependencies.apacheCommons)

  // jackson
  implementation(BuildDependencies.jacksonCore)
  implementation(BuildDependencies.jacksonAnnotations)
  implementation(BuildDependencies.jacksonDataBind)
  implementation(BuildDependencies.jacksonDataFormat)
  implementation(BuildDependencies.jacksonKotlin)


  testImplementation(BuildDependencies.mockito)
  {
    exclude("junit", "junit")
  }
  testImplementation(BuildDependencies.vertxJUnit5)
  {
    exclude("junit", "junit")
  }
  testImplementation(BuildDependencies.junitJupiter) {
    exclude("junit", "junit")
  }
  testImplementation(BuildDependencies.kotlinMockito)
  {
    exclude("junit", "junit")
  }
  testImplementation(kotlin(BuildDependencies.kotlinJunit))

}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}
