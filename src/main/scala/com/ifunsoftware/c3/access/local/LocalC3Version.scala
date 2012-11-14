package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.{C3InputStream, C3Version}
import org.aphreet.c3.platform.resource.ResourceVersion

class LocalC3Version(val version:ResourceVersion) extends C3Version{

  def date = version.date

  def metadata = version.systemMetadata.toMap

  def getData = new LocalC3ByteChannel(version.data)

  def getDataStream = new C3InputStream {

    val is = version.data.inputStream

    def length = version.data.length

    def read() = is.read()

    override def close(){
      is.close()
      super.close()
    }
  }
}
