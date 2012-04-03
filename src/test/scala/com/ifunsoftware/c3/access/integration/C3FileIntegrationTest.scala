package com.ifunsoftware.c3.access.integration

import org.junit.Assert._
import com.ifunsoftware.c3.access.fs.C3Directory
import com.ifunsoftware.c3.access.{C3AccessException, DataStream, C3SystemFactory}
import org.junit.{Ignore, Test}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

@Ignore
class C3FileIntegrationTest {

  @Test
  def testFileSystemCRUD(){

    val C3_SYSTEM_ADDRESS = "http://c3.aphreet.org:7373"

    val C3_DOMAIN = "aphreet"

    val C3_KEY = "e14ebc01610f9273fbe12e118d662f37"

    val system = new C3SystemFactory().createSystem(C3_SYSTEM_ADDRESS, C3_DOMAIN, C3_KEY)

    val node = system.getFile("/")

    val dir = node.asInstanceOf[C3Directory]

    val directoryName = "inttest-" + System.currentTimeMillis()

    dir.createDirectory(directoryName)

    val testDir = dir.children.filter(_.name == directoryName).head.asInstanceOf[C3Directory]

    for(child <- testDir.children){
      println(child.name)
    }

    testDir.createDirectory("MyDirectory")
    testDir.createFile("HelloWorld124.txt", Map("md0" -> "value0"), DataStream("Hello, World!"))

    assertEquals(List("MyDirectory", "HelloWorld124.txt"), testDir.children.map(_.name).toList)

    for(child <- testDir.children){
      system.deleteResource(child.address)
    }

    testDir.markDirty()

    assertTrue(testDir.children.isEmpty)

    for(child <- testDir.children){
      println(child.name)
    }

    system.deleteResource(testDir.address)

    try{
      system.getFile(testDir.fullname)
      fail("Expected C3AccessException")
    }catch {
      case e:C3AccessException => {
        assertEquals(404, e.code)
        assertEquals("File not found", e.message)
      }
      case e => fail("Expected C3AccessException")
    }
  }
}