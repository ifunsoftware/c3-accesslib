package com.ifunsoftware.c3.access

import impl.{ByteArrayDataStream, FileDataStream}
import org.apache.commons.httpclient.methods.multipart.FilePart
import java.io.File
import org.apache.commons.httpclient.methods.RequestEntity

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait DataStream {

  def createFilePart: FilePart

  def createRequestEntity: RequestEntity
}

object DataStream {

  def apply(file: File): DataStream = new FileDataStream(file, "application/octet-stream")

  def apply(file: File, contentType: String) = new FileDataStream(file, contentType)

  def apply(array: Array[Byte]): DataStream = new ByteArrayDataStream(array)

  def apply(string: String): DataStream = new ByteArrayDataStream(string.getBytes("UTF-8"))
}