package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.impl.{FileDataStream, ByteArrayDataStream}
import org.aphreet.c3.platform.resource.{DataStream => InternalDataStream}
import com.ifunsoftware.c3.access.DataStream

trait DataConverter {

  implicit def streamToInternalStream(datastream:DataStream):InternalDataStream = {
    datastream match {
      case ByteArrayDataStream(bytes) => InternalDataStream.create(bytes)
      case FileDataStream(file) => InternalDataStream.create(file)
      case _ => throw new IllegalArgumentException("Unsupported stream type")
    }
  }
}