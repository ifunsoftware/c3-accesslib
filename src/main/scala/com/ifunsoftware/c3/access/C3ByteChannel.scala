package com.ifunsoftware.c3.access

import java.nio.channels.ReadableByteChannel

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3ByteChannel extends ReadableByteChannel {

  def length:Long
  
}