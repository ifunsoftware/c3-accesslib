package com.ifunsoftware.c3.access.fs

import com.ifunsoftware.c3.access.{C3Resource, DataStream}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
trait C3FileSystemNode extends C3Resource {

  def name: String

  def fullname: String

  def move(path: String)

  def isDirectory: Boolean

  def asDirectory: C3Directory = throw new IllegalArgumentException("This instance is not a directory")

  def asFile: C3File = throw new IllegalArgumentException("This instance is not a file")
}

trait C3File extends C3FileSystemNode {

}

trait C3Directory extends C3FileSystemNode {

  def children(embedChildrenData: Boolean = false, embedChildMetaData: Set[String] = Set()): List[C3FileSystemNode]

  def createDirectory(dirName: String, meta: Map[String, String])

  def createFile(name: String, meta: Map[String, String], data: DataStream)

  def markDirty()

  def getChild(name: String, embedChildData: Boolean = false, embedChildMetaData: Set[String] = Set()): Option[C3FileSystemNode]

}