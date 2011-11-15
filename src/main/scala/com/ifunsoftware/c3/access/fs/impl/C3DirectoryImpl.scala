package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.impl.C3SystemImpl
import com.ifunsoftware.c3.access.DataStream
import collection.mutable.ArrayBuffer
import xml.{XML, NodeSeq}
import java.nio.channels.Channels
import com.ifunsoftware.c3.access.fs.{C3FileSystemNode, C3Directory}
import org.slf4j.LoggerFactory

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3DirectoryImpl(override val system:C3SystemImpl,
                 override val address:String,
                 override val xml:NodeSeq,
                 override val _name:String,
                 override val _fullname:String,
                 val directoryXml:NodeSeq) extends C3FileSystemNodeImpl(system, address, xml, _name, _fullname)
                                           with C3Directory
{
  private val log = C3DirectoryImpl.log

  private var directoryLoaded = false

  private var _children = List[C3FileSystemNode]()

  {
    if(directoryXml != null){
      updateFieldsFromDirectoryXml(directoryXml)
    }
  }

  def this(system:C3SystemImpl, address:String, _name:String, _fullname:String) =
    this(system, address, _name, _fullname)

  def this(system:C3SystemImpl, address:String, _name:String, _fullname:String) = this(system, address, null, _name, _fullname, null)

  override def children:List[C3FileSystemNode] = preloadDir{_children}

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


  def updateFieldsFromDirectoryXml(description:NodeSeq){

    val directoryTag = (description \ "directory")(0)

    val nodesTag = (directoryTag \ "nodes")(0)

    val array = new ArrayBuffer[C3FileSystemNode]

    for(nodeTag <- nodesTag \ "node"){
      val childName = (nodeTag \ "@name").text
      val childAddress = (nodeTag \ "@address").text
      val isFile = (nodeTag \ "@leaf").text == "true"

      val childFullName = fullname + "/" + childName

      val child:C3FileSystemNode = if(isFile){
        new C3FileImpl(system, childAddress, childName, childFullName)
      }else{
        new C3DirectoryImpl(system, childAddress, childName, childFullName)
      }

      array += child
    }

    _children = array.sortWith((first, second) => {
      if(first.getClass == second.getClass){
        first.name.compareTo(second.name) < 0
      }else{
        first.isInstanceOf[C3Directory]
      }
    }).toList

    directoryLoaded = true
  }

  protected def preloadDir[T](value: => T):T = {
    if(!directoryLoaded){

      log.debug("Directory {} is not loaded yet, loading...", address)

      val channel = system.getData(address)

      try{
        val xml = XML.load(Channels.newReader(channel, "UTF-8"))
        updateFieldsFromDirectoryXml(xml)
      }finally{
        channel.close()
      }

    }
    value
  }

  override def toString:String = {
    val builder = new StringBuilder

    builder.append("[C3DirectoryImpl name=").append(_name)
      .append(", fullname=").append(_fullname)
      .append(", resource=").append(super.toString)
      .append("]")

    builder.toString()
  }
}

object C3DirectoryImpl{

  val log = LoggerFactory.getLogger(classOf[C3DirectoryImpl])

}