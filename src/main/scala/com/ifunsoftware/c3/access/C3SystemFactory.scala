package com.ifunsoftware.c3.access

import impl.C3SystemImpl
import local.LocalC3System

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3SystemFactory {

  def createSystem(host: String, domain: String, key: String, maxConnections: Int, proxyHost: String, proxyPort: Int): C3System = {
    new C3SystemImpl(host, domain, key, maxConnections, proxyHost, proxyPort)
  }

  def createSystem(host: String, domain: String, key: String): C3System = {
    new C3SystemImpl(host, domain, key)
  }

  def createSystem(host: String): C3System = {
    new C3SystemImpl(host, "anonymous", null)
  }

  def createLocalSystem(domain: String, bundleContext: AnyRef): C3System = {
    new LocalC3System(domain, bundleContext)
  }
}