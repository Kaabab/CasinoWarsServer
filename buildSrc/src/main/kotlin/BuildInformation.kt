/**
 * Group all dependency versions
 */
object BuildVersions {
  const val kotlin = "1.4.0"
  const val shadowMan = "7.0.0"
  const val junitJupiter = "5.7.0"
  const val vertx = "4.1.0"
  const val guice = "4.0"
  const val jackson = "2.12.1"
  const val apacheCommons = "3.12.0"
  const val mockito = "3.11.1"
  const val mockitoKotlin = "3.2.0"
}

/**
 * Defines build configuration constants
 */
object BuildConstants {
  const val groupId = "com.casinowars"
  const val targetVersion = "1.0.0-SNAPSHOT"

  const val mainVerticleName = "guice:com.casinowars.server.MainVerticle"
  const val launcherClassName = "com.casinowars.server.Launcher"
}

/**
 * List dependencies
 */
object BuildDependencies {
  const val vertxCore = "io.vertx:vertx-core:${BuildVersions.vertx}"
  const val vertxDepChain = "io.vertx:vertx-stack-depchain:${BuildVersions.vertx}"
  const val vertxPostGre = "io.vertx:vertx-pg-client:${BuildVersions.vertx}"
  const val vertxWeb = "io.vertx:vertx-web:${BuildVersions.vertx}"
  const val vertxWebOpenApi = "io.vertx:vertx-web-openapi:${BuildVersions.vertx}"

  const val vertxJUnit5 = "io.vertx:vertx-junit5"
  const val mockito = "org.mockito:mockito-core:${BuildVersions.mockito}"
  const val kotlinJunit = "test-junit5"
  const val kotlinMockito = "org.mockito.kotlin:mockito-kotlin:${BuildVersions.mockitoKotlin}"
  const val junitJupiter = "org.junit.jupiter:junit-jupiter:${BuildVersions.junitJupiter}"

  const val jacksonAnnotations = "com.fasterxml.jackson.core:jackson-annotations:${BuildVersions.jackson}"
  const val jacksonCore = "com.fasterxml.jackson.core:jackson-core:${BuildVersions.jackson}"
  const val jacksonDataBind = "com.fasterxml.jackson.core:jackson-databind:${BuildVersions.jackson}"
  const val jacksonDataFormat = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${BuildVersions.jackson}"
  const val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${BuildVersions.jackson}"


  const val guice = "com.google.inject:guice:${BuildVersions.guice}"
  const val apacheCommons = "org.apache.commons:commons-lang3:${BuildVersions.apacheCommons}"
}
