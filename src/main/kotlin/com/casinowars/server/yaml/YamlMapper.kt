package com.casinowars.server.yaml

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Kotlin enabled Yaml mapper
 */
class YamlMapper {
  private val mapper: ObjectMapper = configureMapper()

  private fun configureMapper(): ObjectMapper {

    val mapper = ObjectMapper(
      YAMLFactory()
        .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
        .configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    )
      .registerKotlinModule()
    mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
    return mapper
  }

  internal inline fun <reified T> read(content: String): T {
    return mapper.readValue(content)
  }

}
