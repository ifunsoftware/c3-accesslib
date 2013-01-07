package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.C3ByteChannel
import java.nio.ByteBuffer
import org.aphreet.c3.platform.resource.DataStream
import java.nio.channels.Channels

class LocalC3ByteChannel(val dataStream: DataStream) extends C3ByteChannel {

  lazy val channel = Channels.newChannel(dataStream.inputStream)

  def read(dst: ByteBuffer) = {
    channel.read(dst)
  }

  def length = dataStream.length

  def readContentAsString = dataStream.stringValue

  def isOpen = channel.isOpen

  def close() {
    channel.close()
  }
}
