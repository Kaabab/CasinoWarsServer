package com.casinowars.server.modules

import com.google.inject.AbstractModule

/**
 * General module sub class with added dispose functionality
 */
abstract class Module : AbstractModule() {
  /**
   * Dispose the module acquired resources
   */
  abstract fun dispose()
}
