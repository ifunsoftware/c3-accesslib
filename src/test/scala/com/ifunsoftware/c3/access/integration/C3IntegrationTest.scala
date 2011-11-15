package com.ifunsoftware.c3.access.integration

import org.junit.Test
import org.junit.Assert._
import java.io.BufferedReader
import java.nio.channels.{ReadableByteChannel, Channels}
import com.ifunsoftware.c3.access.{C3AccessException, DataStream, C3SystemFactory}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
//@Ignore
class C3IntegrationTest {

  val C3_SYSTEM_ADDRESS = "http://localhost:8080"
  
  @Test
  def testResourceCRUD() {

    val address = checkResourceAdd

    checkResourceUpdate(address)

    checkResourceDelete(address)
  }

  def checkResourceAdd:String = {

    val expectedContent = "Preveddd!\njhkjh"

    val meta = Map("my.meta" -> "value0")

    val system = new C3SystemFactory().createSystem(C3_SYSTEM_ADDRESS)

    val address = system.addResource(meta, DataStream(expectedContent))

    val resource = system.getResource(address)

    checkMetadataContains(meta, resource.metadata)
    
    val dataChannel = system.getData(address)

    checkContentMatch(expectedContent, dataChannel)

    address
  }

  def checkResourceUpdate(address:String) {

    val expectedContent = "Preved!"

    val newMeta = Map("c3.int.test" -> "value4", "c3.int.test2" -> "value5")

    val system = new C3SystemFactory().createSystem(C3_SYSTEM_ADDRESS)

    val resource = system.getResource(address)

    resource.update(newMeta, DataStream(expectedContent))

    val resource2 = system.getResource(address)

    checkMetadataContains(newMeta, resource.metadata)

    val version = resource2.versions.tail.head

    val dataChannel = version.getData

    checkContentMatch(expectedContent, dataChannel)
  }

  def checkResourceDelete(address:String) {

    val system = new C3SystemFactory().createSystem(C3_SYSTEM_ADDRESS)

    system.deleteResource(address)

    val resource = system.getResource(address)
    
    assertEquals("C3ResourceImpl[address=" + address + "]", resource.toString)

    try{
      resource.metadata
      fail("Resource can't be loaded after delete")
    }catch{
      case e:C3AccessException => {
        assertEquals(404, e.code)
        assertEquals("Resource not found", e.message)
      }
      case e => fail("Expected C3AccessException")
    }
  }

  def checkContentMatch(expected:String, actual:ReadableByteChannel) {
    val reader = Channels.newReader(actual, "UTF-8")

    val bufferedReader = new BufferedReader(reader)

    try{
      val builder = new StringBuilder

      var line = bufferedReader.readLine

      while(line != null){
        builder.append(line)
        line = bufferedReader.readLine
        if(line != null){
          builder.append("\n")
        }
      }

      val content = builder.toString()

      assertEquals(expected, content)

    }finally {
      bufferedReader.close()
    }
  }

  def checkMetadataContains(expected:Map[String, String], actual:Map[String, String]){

    for((key, value) <- expected){
      assertEquals(value, actual.getOrElse(key, ""))
    }
  }
}