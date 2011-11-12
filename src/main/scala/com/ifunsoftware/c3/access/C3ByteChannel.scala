package com.ifunsoftware.c3.access

import java.io.BufferedInputStream
import java.nio.ByteBuffer
import org.apache.commons.httpclient.HttpMethodBase
import java.nio.channels.{ReadableByteChannel, Channels}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ByteChannel(val method:HttpMethodBase) extends ReadableByteChannel {

  private var open = true

  private val inChannel = Channels.newChannel(new BufferedInputStream(method.getResponseBodyAsStream))

  override def read(buffer:ByteBuffer):Int = inChannel.read(buffer)

  override def isOpen:Boolean = open

  def length:Long = method.getResponseContentLength

  override def close() {
    try{
      open = false
      inChannel.close()
    }finally {
      method.releaseConnection()
    }
  }
}