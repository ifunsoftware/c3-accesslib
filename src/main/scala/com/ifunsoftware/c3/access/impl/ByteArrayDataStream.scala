package com.ifunsoftware.c3.access.impl

import java.io.{ByteArrayInputStream, InputStream}
import com.ifunsoftware.c3.access.DataStream
import org.apache.commons.httpclient.methods.multipart.{FilePart, PartSource}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

case class ByteArrayDataStream(bytes: Array[Byte]) extends DataStream {

  override def createFilePart: FilePart = new FilePart("data", new ByteArrayPartSource(bytes))
}

class ByteArrayPartSource(val bytes: Array[Byte]) extends PartSource {

  override def createInputStream: InputStream = {
    new ByteArrayInputStream(bytes)
  }

  override def getFileName: String = "array"

  override def getLength: Long = bytes.length
}