package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.fs.C3FileSystemNode
import com.ifunsoftware.c3.access.impl.{C3ResourceImpl, C3SystemImpl}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3FileSystemNodeImpl(override val system:C3SystemImpl,
                           override val address:String,
                           val _name:String,
                           val _fullname:String) extends C3ResourceImpl(system, address)
                                                    with C3FileSystemNode {

  override def name:String = _name

  override def fullname:String = _fullname
}