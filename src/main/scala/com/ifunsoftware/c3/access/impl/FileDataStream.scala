package com.ifunsoftware.c3.access.impl

import com.ifunsoftware.c3.access.DataStream
import java.io.File
import org.apache.commons.httpclient.methods.multipart.{FilePartSource, FilePart}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class FileDataStream(val file:File) extends DataStream {

  override def createFilePart:FilePart = new FilePart("data", new FilePartSource(file))
  
}