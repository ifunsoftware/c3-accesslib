package com.ifunsoftware.c3.access.impl

import com.ifunsoftware.c3.access.C3ByteChannel
import org.apache.commons.httpclient.HttpMethodBase
import java.nio.channels.Channels
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ByteChannelImpl(val method: HttpMethodBase) extends C3ByteChannel {

  private val log = C3ByteChannelImpl.log

  private var open = true

  private val inChannel = Channels.newChannel(new BufferedInputStream(method.getResponseBodyAsStream))

  {
    log.trace("Channel created {}", this)
  }

  override def read(buffer: ByteBuffer): Int = inChannel.read(buffer)

  override def isOpen: Boolean = open

  override def length: Long = method.getResponseContentLength

  override def close() {
    try {
      open = false
      inChannel.close()
    } finally {
      log.debug("Channel trace {}", this)
      method.releaseConnection()
    }
  }

  override def readContentAsString: String = {
    val buffer = ByteBuffer.allocate(length.toInt)
    read(buffer)
    val content = new String(buffer.array(), "UTF-8")
    close()
    content
  }

  override def finalize(){
    if (open){
      try{
        log.warn("Finalizing opened ByteChannel. This looks like a bug")
        close()
      }catch{
        case e: Throwable =>
      }
    }
  }
}

object C3ByteChannelImpl {

  val log = LoggerFactory.getLogger(classOf[C3ByteChannelImpl])

}