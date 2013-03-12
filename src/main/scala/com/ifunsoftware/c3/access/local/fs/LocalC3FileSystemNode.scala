package com.ifunsoftware.c3.access.local.fs

import com.ifunsoftware.c3.access.fs.{C3File, C3Directory, C3FileSystemNode}
import org.aphreet.c3.platform.filesystem.{NodeRef, Directory, Node}
import com.ifunsoftware.c3.access.local._
import com.ifunsoftware.c3.access.DataStream

class LocalC3FileSystemNode(override val system: LocalC3System,
                            val resourceContainer: ResourceContainer,
                            val name: String,
                            var fullPath: String,
                            val isDirectory: Boolean)
  extends LocalC3Resource(system, resourceContainer) with C3FileSystemNode {

  def fullname = fullPath

  def move(path: String) {
    system.move(fullPath, path)
    fullPath = path
  }
}


object LocalC3FileSystemNode{

  def apply(system: LocalC3System, node: Node, fullPath: String): LocalC3FileSystemNode = {
    if (node.isDirectory){
      new LocalC3Directory(system, new LoadedResourceContainer(node.resource), node.name, fullPath)
    }else{
      new LocalC3File(system, new LoadedResourceContainer(node.resource), node.name, fullPath)
    }
  }

  def apply(system: LocalC3System, nodeRef: NodeRef, fullPath: String): LocalC3FileSystemNode = {
    if (!nodeRef.leaf){
      new LocalC3Directory(system, new LazyResourceContainer(system, nodeRef.address), nodeRef.name, fullPath)
    }else{
      new LocalC3File(system, new LazyResourceContainer(system, nodeRef.address), nodeRef.name, fullPath)
    }
  }

}

