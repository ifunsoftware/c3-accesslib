package com.ifunsoftware.c3.access.impl

import com.ifunsoftware.c3.access.C3ByteChannel
import org.apache.commons.httpclient.HttpMethodBase
import java.nio.channels.Channels
import java.io.BufferedInputStream
import java.nio.ByteBuffer

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ByteChannelImpl(val method:HttpMethodBase) extends C3ByteChannel {

  private var open = true

  private val inChannel = Channels.newChannel(new BufferedInputStream(method.getResponseBodyAsStream))

  override def read(buffer:ByteBuffer):Int = inChannel.read(buffer)

  override def isOpen:Boolean = open

  override def length:Long = method.getResponseContentLength

  override def close() {
    try{
      open = false
      inChannel.close()
    }finally {
      method.releaseConnection()
    }
  }
}