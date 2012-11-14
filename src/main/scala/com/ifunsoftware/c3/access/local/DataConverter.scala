package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.impl.{FileDataStream, ByteArrayDataStream}
import org.aphreet.c3.platform.resource.{DataStream => InternalDataStream}
import com.ifunsoftware.c3.access.DataStream

/**
 * Created with IntelliJ IDEA.
 * User: malygm
 * Date: 11/14/12
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
trait DataConverter {

  implicit def streamToInternalStream(datastream:DataStream):InternalDataStream = {
    datastream match {
      case ByteArrayDataStream(bytes) => InternalDataStream.create(bytes)
      case FileDataStream(file) => InternalDataStream.create(file)
      case _ => throw new IllegalArgumentException("Unsupported stream type")
    }
  }
}
