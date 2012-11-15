package com.ifunsoftware.c3.access.impl

import org.apache.commons.httpclient.HttpMethodBase
import com.ifunsoftware.c3.access.C3InputStream


class C3InputStreamImpl(val method:HttpMethodBase) extends C3InputStream{

  private lazy val httpInput = method.getResponseBodyAsStream

  override def read() = httpInput.read()

  override def read(b: Array[Byte]):Int = httpInput.read(b)

  override def read(b: Array[Byte], off: Int, len: Int) = httpInput.read(b, off, len)

  override def skip(n: Long) = httpInput.skip(n)

  override def available() = httpInput.available()

  override def mark(readlimit: Int) {
    httpInput.mark(readlimit)
  }

  override def reset() {
    httpInput.reset()
  }

  override def markSupported() = httpInput.markSupported()

  def length = method.getResponseContentLength
  
  override def close(){
    try{
      httpInput.close()
    }finally{
      method.releaseConnection()
    }
  }
}
