package com.ifunsoftware.c3.access

import impl.{ByteArrayDataStream, FileDataStream}
import org.apache.commons.httpclient.methods.multipart.FilePart
import java.io.File

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait DataStream {

  def createFilePart:FilePart

}

object DataStream{

  def apply(file:File):DataStream = new FileDataStream(file)

  def apply(array:Array[Byte]):DataStream = new ByteArrayDataStream(array)

  def apply(string:String):DataStream = new ByteArrayDataStream(string.getBytes("UTF-8"))
}