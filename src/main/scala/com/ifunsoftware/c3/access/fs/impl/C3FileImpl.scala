package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.impl.C3SystemImpl
import com.ifunsoftware.c3.access.fs.C3File
import xml.NodeSeq

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3FileImpl(override val system:C3SystemImpl,
                 override val address:String,
                 override val xml:NodeSeq,
                 override val _name:String,
                 override val _fullname:String) extends C3FileSystemNodeImpl(system, address, xml, _name, _fullname) with C3File
{

  def this(system:C3SystemImpl, address:String, _name:String, _fullname:String) =
    this(system, address, null, _name, _fullname)
  
  override def toString:String = {
    val builder = new StringBuilder

    builder.append("[C3FileImpl name=").append(_name)
      .append(", fullname=").append(_fullname)
      .append(", resource=").append(super.toString)
      .append("]")

    builder.toString()
  }
}