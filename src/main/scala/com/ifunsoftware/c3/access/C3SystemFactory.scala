package com.ifunsoftware.c3.access

import impl.C3SystemImpl

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3SystemFactory {

  def createSystem(host:String, domain:String, key:String):C3System = {
    new C3SystemImpl(host, domain, key)
  }

  def createSystem(host:String):C3System = {
    new C3SystemImpl(host, "anonymous", null)
  }
}