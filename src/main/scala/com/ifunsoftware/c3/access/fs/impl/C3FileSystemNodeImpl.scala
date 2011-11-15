package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.fs.C3FileSystemNode
import com.ifunsoftware.c3.access.impl.{C3ResourceImpl, C3SystemImpl}
import xml.NodeSeq

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
abstract class C3FileSystemNodeImpl(override val system:C3SystemImpl,
                           override val address:String,
                           override val xml:NodeSeq,
                           val _name:String,
                           val _fullname:String) extends C3ResourceImpl(system, address, xml)
                                                    with C3FileSystemNode {

  def this(system:C3SystemImpl, address:String, _name:String, _fullname:String) =
    this(system, address, null, _name, _fullname)

  override def name:String = _name

  override def fullname:String = _fullname

  override def move(path:String) {
    
  }
}