package com.ifunsoftware.c3.access.local.fs

import com.ifunsoftware.c3.access.fs.{C3File, C3Directory, C3FileSystemNode}
import org.aphreet.c3.platform.filesystem.{NodeRef, Directory, Node}
import com.ifunsoftware.c3.access.local._
import com.ifunsoftware.c3.access.DataStream

class LocalC3FileSystemNode(override val system: LocalC3System,
                            val resourceContainer: ResourceContainer,
                            val name: String,
                            var fullPath: String,
                            val isDirectory: Boolean
                             )
  extends LocalC3Resource(system, resourceContainer) with C3FileSystemNode with C3File with C3Directory {

  def this(system: LocalC3System, node: Node, fullPath: String) = this(system,
    new LoadedResourceContainer(node.resource),
    node.name,
    fullPath,
    node.isDirectory)

  def this(system: LocalC3System, nodeRef: NodeRef, fullPath: String) = this(system,
    new LazyResourceContainer(system, nodeRef.address),
    nodeRef.name,
    fullPath,
    !nodeRef.leaf)

  var internalFSNode = new InternalFSNodeWrapper(system, resource, fullPath)

  override def asFile = if (!isDirectory)
    this.asInstanceOf[C3File]
  else
    super.asFile

  override def asDirectory = if (isDirectory)
    this.asInstanceOf[C3Directory]
  else
    super.asDirectory

  def fullname = fullPath

  def children(embedChildrenData: Boolean, embedChildMetaData: Set[String]) =
    internalFSNode.children

  def getChild(name: String, embedChildData: Boolean, embedChildMetaData: Set[String]) =
    internalFSNode.getChild(name)

  def createDirectory(name: String) {
    system.createDirectory(fullname + "/" + name)
    markDirty()
  }

  def createFile(name: String, meta: Map[String, String], data: DataStream) {
    system.createFile(fullname + "/" + name, meta, data)
    markDirty()
  }

  def markDirty() {
    internalFSNode = new InternalFSNodeWrapper(system, new LazyResourceContainer(system, resource.address), fullname)
  }

  def move(path: String) {
    system.move(fullPath, path)
    fullPath = path
    markDirty()
  }
}

class InternalFSNodeWrapper(system: LocalC3System, resourceContainer: ResourceContainer, fullPath: String) {

  lazy val internalFSNode = Node.fromResource(resourceContainer.resource)

  lazy val children = internalFSNode.asInstanceOf[Directory].getChildren
    .map(nodeRef => new LocalC3FileSystemNode(system, nodeRef, fullPath + "/" + nodeRef.name)).toList


  def getChild(name: String) = internalFSNode.asInstanceOf[Directory].getChild(name) match {
    case Some(ref) => Some(new LocalC3FileSystemNode(system, ref, fullPath + "/" + ref.name))
    case None => None
  }
}

