package com.ifunsoftware.c3.access.local.fs

import com.ifunsoftware.c3.access.local.{ResourceContainer, LocalC3System}
import com.ifunsoftware.c3.access.fs.C3File


class LocalC3File(override val system: LocalC3System,
                  override val resourceContainer: ResourceContainer,
                  override val name: String,
                  fullPath: String) extends LocalC3FileSystemNode(system, resourceContainer, name, fullPath, false)
                                                    with C3File{


  override def asFile: C3File = this

}
