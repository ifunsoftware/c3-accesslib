package com.ifunsoftware.c3.access.fs

import com.ifunsoftware.c3.access.{C3Resource, DataStream}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
trait C3FileSystemNode {

  def name:String

  def metadata:Map[String, String]

}

trait C3File extends C3FileSystemNode with C3Resource {

}

trait C3Directory extends C3FileSystemNode {

  def children:List[C3FileSystemNode]
  
  def createDirectory(name:String):C3Directory

  def createFile(name:String, meta:Map[String, String], data:DataStream)
  
}