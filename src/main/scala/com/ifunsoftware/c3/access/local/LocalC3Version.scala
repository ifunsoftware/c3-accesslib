package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.C3Version
import org.aphreet.c3.platform.resource.ResourceVersion

class LocalC3Version(val version: ResourceVersion) extends C3Version {

  def date = version.date

  def metadata = version.systemMetadata.toMap

  def getData = new LocalC3ByteChannel(version.data)

  def getDataStream = new LocalC3InputStream(version.data)

}
