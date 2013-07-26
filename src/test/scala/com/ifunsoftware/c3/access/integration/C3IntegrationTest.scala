package com.ifunsoftware.c3.access.integration

import com.ifunsoftware.c3.access.C3System._
import com.ifunsoftware.c3.access._
import java.io.BufferedReader
import java.nio.channels.{ReadableByteChannel, Channels}
import org.junit.Assert._
import org.junit.Test
import scala.collection.Map

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3IntegrationTest {

  @Test
  def testResourceCRUD() {

    val address = checkResourceAdd

    checkResourceUpdate(address)

    checkResourceDelete(address)
  }

  @Test
  def testQuery() {
    val system = createSystem()

    system.query(Map("content.type" -> "application/x-c3-directory"), (resource) => {
      println(resource.address)
    })
  }

  def createSystem() = new C3SystemFactory().createSystem(C3_HOST, C3_DOMAIN, C3_KEY, 100, HTTP_PROXY_CONFIG)

  //@Test
  def testDelete(){
    val system = createSystem()
    system.deleteResource("ZnZCFfW6-Z5q7-AVqF-u1UPcVkp-5S5NJXgv-fbb6")
  }

  def checkResourceAdd:String = {

    val expectedContent = "Preveddd!\njhkjh"

    val meta = Map("my.meta" -> "value0")

    val system = createSystem()

    val address = system.addResource(meta, DataStream(expectedContent))

    val resource = system.getResource(address)

    assertTrue("Resource should exist", resource.isDefined)

    checkMetadataContains(meta, resource.get.metadata)
    
    val dataChannel = system.getData(address)

    assertTrue("Data channel should exist", dataChannel.isDefined)

    checkContentMatch(expectedContent, dataChannel.get)

    address
  }

  def checkResourceUpdate(address:String) {

    val expectedContent = "Preved!"

    val newMeta = Map("c3.int.test" -> "value4", "c3.int.test2" -> "value5")

    val system = createSystem()

    val resource = system.getResource(address)

    assertTrue("Resource should exist", resource.isDefined)

    resource.get.update(MetadataUpdate(newMeta), DataStream(expectedContent))

    checkMetadataContains(newMeta, resource.get.metadata)

    val version = resource.get.versions.tail.head

    val dataChannel = version.getData

    checkContentMatch(expectedContent, dataChannel)
  }

  def checkResourceDelete(address:String) {

    val system = createSystem()

    system.deleteResource(address)

    try{
      system.getResource(address)
      fail("Resource can't be loaded after delete")
    }catch{
      case e:C3AccessException => {
        assertEquals(404, e.code)
        assertEquals("Resource not found", e.message)
      }
      case e: Throwable => fail("Expected C3AccessException")
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