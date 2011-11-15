package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.impl.C3SystemImpl
import com.ifunsoftware.c3.access.fs.C3File

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3FileImpl(override val system:C3SystemImpl,
                 override val address:String,
                 override val _name:String,
                 override val _fullname:String) extends C3FileSystemNodeImpl(system, address, _name, _fullname) with C3File
{

}