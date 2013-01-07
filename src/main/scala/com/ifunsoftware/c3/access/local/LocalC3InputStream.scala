package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.C3InputStream
import org.aphreet.c3.platform.resource.DataStream

class LocalC3InputStream(val ds: DataStream) extends C3InputStream {

  private lazy val internalInputStream = ds.inputStream

  override def read() = internalInputStream.read()

  override def read(b: Array[Byte]): Int = internalInputStream.read(b)

  override def read(b: Array[Byte], off: Int, len: Int) = internalInputStream.read(b, off, len)

  override def skip(n: Long) = internalInputStream.skip(n)

  override def available() = internalInputStream.available()

  override def mark(readlimit: Int) {
    internalInputStream.mark(readlimit)
  }

  override def reset() {
    internalInputStream.reset()
  }

  override def markSupported() = internalInputStream.markSupported()

  def length = ds.length

  override def close() {
    internalInputStream.close()

  }

}
