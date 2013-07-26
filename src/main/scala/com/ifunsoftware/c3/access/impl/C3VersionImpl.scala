package com.ifunsoftware.c3.access.impl

import java.util.Date
import com.ifunsoftware.c3.access.{C3AccessException, C3InputStream, C3ByteChannel, C3Version}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3VersionImpl(val system: C3SystemImpl,
                    val resource: C3ResourceImpl,
                    val _date: Date,
                    val length: Long,
                    val hash: String,
                    val _number: Int) extends C3Version {

  def date: Date = _date

  def getData: C3ByteChannel = system.getDataInternal(resource.address, _number)
    .getOrElse(throw new C3AccessException("No data for specified version"))

  def getDataStream: C3InputStream = system.getDataAsStreamInternal(resource.address, _number)
    .getOrElse(throw new C3AccessException("No data for specified version"))

  override def toString: String = {
    val builder = new StringBuilder

    builder.append("C3VersionImpl[number=").append(_number)
      .append(", date=").append(date.getTime)
      .append(", length=").append(length)
      .append(", hash=").append(hash)
      .append("]")

    builder.toString()
  }
}