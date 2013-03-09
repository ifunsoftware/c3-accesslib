package com.ifunsoftware.c3.access.integration

import org.junit.{Ignore, Test}
import com.ifunsoftware.c3.access.C3SystemFactory

@Ignore
class C3SearchIntegrationTest {

  @Test
  def testSearch(){

    val system = new C3SystemFactory().createSystem("http://node0.c3.ifunsoftware.com",
      "576caf7e-380c-48ea-a005-9dd392bbe426", "947e83d99dc01eb0b86acbd994e249b7")

    println(system.search("Hello"))

  }
}
