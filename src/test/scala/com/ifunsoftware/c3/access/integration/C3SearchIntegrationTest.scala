package com.ifunsoftware.c3.access.integration

import com.ifunsoftware.c3.access.C3SystemFactory
import org.junit.Test

class C3SearchIntegrationTest {

  @Test
  def testSearch(){

    val system = new C3SystemFactory().createSystem(C3_HOST,
      C3_DOMAIN, C3_KEY, 100, HTTP_PROXY_HOST, HTTP_PROXY_PORT)

    println(system.search("Hello"))

  }
}
