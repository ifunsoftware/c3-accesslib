package com.ifunsoftware.c3.access.integration

import com.ifunsoftware.c3.access.C3SystemFactory
import java.io.FileOutputStream
import org.junit.{Ignore, Test}

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
}