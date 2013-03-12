package com.ifunsoftware.c3.access.local.fs

import com.ifunsoftware.c3.access.fs.C3Directory
import com.ifunsoftware.c3.access.local.{LazyResourceContainer, ResourceContainer, LocalC3System}
import com.ifunsoftware.c3.access.{MetadataChange, DataStream}
import org.aphreet.c3.platform.filesystem.{Directory, Node}


class LocalC3Directory(override val system: LocalC3System,
                       override val resourceContainer: ResourceContainer,
                       override val name: String,
                       fullPath: String) extends LocalC3FileSystemNode(system, resourceContainer, name, fullPath, true)
with C3Directory {

  var internalFSNode = new InternalFSNodeWrapper(system, resource, fullPath)

  override def asDirectory: C3Directory = this

  def children(embedChildrenData: Boolean, embedChildMetaData: Set[String]) =
    internalFSNode.children

  def getChild(name: String, embedChildData: Boolean, embedChildMetaData: Set[String]) =
    internalFSNode.getChild(name)

  def createDirectory(dirName: String, meta: Map[String, String]) {
    system.createDirectory(fullname + "/" + dirName, meta)
    markDirty()
  }

  def createFile(fileName: String, meta: Map[String, String], data: DataStream) {
    system.createFile(fullname + "/" + fileName, meta, data)
    markDirty()
  }

  def markDirty() {
    internalFSNode = new InternalFSNodeWrapper(system, new LazyResourceContainer(system, resource.address), fullname)
  }

  override def move(path: String) {
    super.move(path)
    markDirty()
  }

  override def update(meta: MetadataChange, data: DataStream) {
    throw new UnsupportedOperationException
  }

  override def update(data: DataStream) {
    throw new UnsupportedOperationException
  }

  class InternalFSNodeWrapper(system: LocalC3System, resourceContainer: ResourceContainer, fullPath: String) {

    lazy val internalFSNode = Node.fromResource(resourceContainer.resource)

    lazy val children = internalFSNode.asInstanceOf[Directory].children
      .map(nodeRef => LocalC3FileSystemNode(system, nodeRef, fullPath + "/" + nodeRef.name)).toList


    def getChild(name: String) = internalFSNode.asInstanceOf[Directory].getChild(name) match {
      case Some(ref) => Some(LocalC3FileSystemNode(system, ref, fullPath + "/" + ref.name))
      case None => None
    }
  }

}
