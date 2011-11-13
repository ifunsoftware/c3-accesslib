package com.ifunsoftware.c3.access.integration

import java.io.FileOutputStream
import org.junit.{Ignore, Test}
import com.ifunsoftware.c3.access.{DataStream, C3SystemFactory}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
@Ignore
class C3IntegrationTest {

  @Test
  def testResourceGet() {

    val system = new C3SystemFactory().createSystem("http://c3.aphreet.org:7373")

    val resource = system.getResource("3EY9anan-2HKa-lGOz-ubI1mSO5-5S5NJXgv-fbb6")

    println(resource)

    val version = resource.versions.head

    val dataChannel = version.getData

    val fileChannel = new FileOutputStream("file.out").getChannel

    try{
      fileChannel.transferFrom(dataChannel, 0, dataChannel.length)
    }finally {
      dataChannel.close()
      fileChannel.close()
    }
  }

  @Test
  def testResourceUpdate() {

    val system = new C3SystemFactory().createSystem("http://c3.aphreet.org:7373")

    val resource = system.getResource("3EY9anan-2HKa-lGOz-ubI1mSO5-5S5NJXgv-fbb6")

    resource.update(Map("c3.int.test" -> "value4", "c3.int.test2" -> "value5"), DataStream("Preved!".getBytes("UTF-8")))

    val resource2 = system.getResource("3EY9anan-2HKa-lGOz-ubI1mSO5-5S5NJXgv-fbb6")

    println(resource2.metadata)

    val version = resource2.versions.tail.head

    val dataChannel = version.getData

    val fileChannel = new FileOutputStream("file.out").getChannel

    try{
      fileChannel.transferFrom(dataChannel, 0, dataChannel.length)
    }finally {
      dataChannel.close()
      fileChannel.close()
    }

  }
}