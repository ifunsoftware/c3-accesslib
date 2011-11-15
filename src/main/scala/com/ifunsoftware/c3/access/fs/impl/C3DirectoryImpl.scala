package com.ifunsoftware.c3.access.fs.impl

import xml.NodeSeq
import com.ifunsoftware.c3.access.impl.C3SystemImpl
import com.ifunsoftware.c3.access.fs.{C3FileSystemNode, C3Directory}
import com.ifunsoftware.c3.access.DataStream
/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3DirectoryImpl(override val system:C3SystemImpl,
                 override val address:String,
                 override val _name:String,
                 override val _fullname:String,
                 val directoryXml:NodeSeq) extends C3FileSystemNodeImpl(system, address, _name, _fullname)
                                           with C3Directory
{
  override def children:List[C3FileSystemNode] = null

  override def createDirectory(name:String):C3Directory = null

  override def createFile(name:String, meta:Map[String, String], data:DataStream){
    
  }

  override def update(meta:Map[String, String], data:DataStream){
    throw new UnsupportedOperationException
  }

  override def update(meta:Map[String, String]){
    throw new UnsupportedOperationException
  }

  override def update(data:DataStream){
    throw new UnsupportedOperationException
  }

  /*
  def updateFromDirectoryXml(description:NodeSeq){

    val directoryTag = (description / "directory")(0)

    val nodesTag = (directoryTag / "nodes")(0)

    val arrayBuffer = new ArrayBuffer[C3FileSystemNode]

    for(nodeTag <- nodesTag / "node"){
      
    }
  }

*/
}