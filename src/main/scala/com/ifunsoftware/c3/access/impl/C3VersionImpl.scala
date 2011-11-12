package com.ifunsoftware.c3.access.impl

import java.util.Date
import com.ifunsoftware.c3.access.{C3ByteChannel, C3Version}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3VersionImpl(val system:C3SystemImpl,
                    val resource:C3ResourceImpl,
                    val _date:Date,
                    val _metadata:Map[String, String],
                    val _number:Int) extends C3Version{

  def date:Date = _date

  def metadata:Map[String, String] = _metadata

  def getData:C3ByteChannel = system.getDataInternal(resource.address, _number)

  override def toString:String = {
    val builder = new StringBuilder

    builder.append("C3VersionImpl[number=").append(_number)
      .append(", date=").append(date.getTime)
      .append(", meta=").append(metadata)
      .append("]")

    builder.toString()
  }
}