package com.ifunsoftware.c3.access.integration

import com.ifunsoftware.c3.access.C3System._
import com.ifunsoftware.c3.access.fs.C3Directory
import com.ifunsoftware.c3.access.{C3AccessException, DataStream, C3SystemFactory}
import io.Source
import org.junit.Assert._
import org.junit.Test

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3FileIntegrationTest {

  @Test
  def testFileSystemCRUD(){

    val system = new C3SystemFactory().createSystem(C3_HOST, C3_DOMAIN, C3_KEY)

    val dir: C3Directory = system.getFile("/") match{
      case Some(node) => node.asInstanceOf[C3Directory]
      case None => throw new IllegalStateException("Root directory expected")
    }

    val directoryName = "inttest " + System.currentTimeMillis()

    dir.createDirectory(directoryName, Map())

    val testDir = dir.children().filter(_.name == directoryName).head.asInstanceOf[C3Directory]

    for(child <- testDir.children()){
      println(child.name)
    }

    testDir.createDirectory("MyDirectory", Map("dirmd" -> "dimdvalue0"))
    testDir.createFile("HelloWorld124.txt", Map("md0" -> "value0"), DataStream("Hello, World!"))

    assertEquals(List("MyDirectory", "HelloWorld124.txt"), testDir.children(embedChildrenData = true, embedChildMetaData = Set("md0")).map(_.name).toList)

    val children = testDir.children(embedChildrenData = true, embedChildMetaData = Set("md0"))

    children.filter(!_.isDirectory).map(_.asFile).foreach{ f =>
      val versions = f.versions
      versions.foreach(v => {
        println(Source.fromInputStream(v.getDataStream, "UTF-8").getLines().toList.mkString("\n"))
      })
      val metadata = f.metadata
      metadata.foreach(md => println(md._1 + ": " + md._2))
    }

    for(child <- testDir.children(embedChildrenData = true)){
      system.deleteResource(child.address)
    }

    testDir.markDirty()

    assertTrue(testDir.children(embedChildrenData = true).isEmpty)

    for(child <- testDir.children()){
      println(child.name)
    }

    system.deleteResource(testDir.address)

    try{
      system.getFile(testDir.fullname)
      fail("Expected C3AccessException")
    }catch {
      case e:C3AccessException => {
        assertEquals(404, e.code)
        assertEquals("Resource not found", e.message)
      }
      case e: Throwable => fail("Expected C3AccessException")
    }
  }
}