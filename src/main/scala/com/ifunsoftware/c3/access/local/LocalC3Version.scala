package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.C3Version
import org.aphreet.c3.platform.resource.ResourceVersion

class LocalC3Version(val version: ResourceVersion) extends C3Version {

  def date = version.date

  def length = version.systemMetadata.asMap.getOrElse("c3.data.length", "0").toLong

  def hash = version.systemMetadata.asMap.getOrElse("c3.data.hash", "")

  def getData = new LocalC3ByteChannel(version.data)

  def getDataStream = new LocalC3InputStream(version.data)

}
