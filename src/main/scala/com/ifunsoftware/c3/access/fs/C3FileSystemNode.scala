package com.ifunsoftware.c3.access.fs

import com.ifunsoftware.c3.access.{C3Resource, DataStream}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
trait C3FileSystemNode extends C3Resource{

  def name:String

  def fullname:String
}

trait C3File extends C3FileSystemNode{

}

trait C3Directory extends C3FileSystemNode {

  def children:List[C3FileSystemNode]
  
  def createDirectory(name:String):C3Directory

  def createFile(name:String, meta:Map[String, String], data:DataStream)
  
}