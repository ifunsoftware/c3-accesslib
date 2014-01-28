package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.impl.{FileDataStream, ByteArrayDataStream}
import org.aphreet.c3.platform.resource.{DataStream => InternalDataStream}
import com.ifunsoftware.c3.access.DataStream
import scala.language.implicitConversions

trait DataConverter {

  implicit def streamToInternalStream(datastream: DataStream): InternalDataStream = {
    datastream match {
      case ByteArrayDataStream(bytes) => InternalDataStream.create(bytes)
      case FileDataStream(file, contentType) => InternalDataStream.create(file)
      case _ => throw new IllegalArgumentException("Unsupported stream type")
    }
  }
}
