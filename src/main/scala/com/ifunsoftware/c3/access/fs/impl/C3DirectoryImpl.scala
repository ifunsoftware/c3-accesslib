package com.ifunsoftware.c3.access.fs.impl

import com.ifunsoftware.c3.access.impl.{C3VersionImpl, C3SystemImpl}
import com.ifunsoftware.c3.access.{C3InputStream, C3ByteChannel, DataStream}
import collection.mutable.ArrayBuffer
import xml.{XML, NodeSeq}
import java.nio.channels.Channels
import org.slf4j.LoggerFactory
import com.ifunsoftware.c3.access.fs.{C3FileSystemNode, C3Directory}
import java.nio.ByteBuffer
import java.io.ByteArrayInputStream
import org.joda.time.format.ISODateTimeFormat

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3DirectoryImpl(override val system: C3SystemImpl,
                      override val address: String,
                      override val xml: NodeSeq,
                      override val _name: String,
                      override val _fullname: String,
                      val directoryXml: NodeSeq) extends C3FileSystemNodeImpl(system, address, xml, _name, _fullname)
with C3Directory {
  private val log = C3DirectoryImpl.log

  private var directoryLoaded = false

  private var _children = List[C3FileSystemNode]()

  {
    if (directoryXml != null) {
      updateFieldsFromDirectoryXml(directoryXml)
    }
  }

  def this(system: C3SystemImpl, address: String, _name: String, _fullname: String) =
    this(system, address, null, _name, _fullname, null)

  def this(system: C3SystemImpl, address: String, xml: NodeSeq, _name: String, _fullname: String) = this(system, address, xml, _name, _fullname, null)

  override def children(embedChildrenData: Boolean = false, embedChildMetaData: Set[String] = Set()): List[C3FileSystemNode] = {
    preloadDir(embedChildrenData, embedChildMetaData) {
      _children
    }
  }

  override def createDirectory(dirName: String) {
    system.addDirectory(createFullPath(fullname, dirName))
    directoryLoaded = false
  }

  def isDirectory: Boolean = true

  override def asDirectory: C3Directory = this

  override def createFile(name: String, meta: Map[String, String], data: DataStream) {
    system.addFile(createFullPath(fullname, name), meta, data)

    directoryLoaded = false
  }

  override def update(meta: Map[String, String], data: DataStream) {
    throw new UnsupportedOperationException
  }

  override def update(meta: Map[String, String]) {
    throw new UnsupportedOperationException
  }

  override def update(data: DataStream) {
    throw new UnsupportedOperationException
  }

  def updateFieldsFromDirectoryXml(description: NodeSeq) {

    val directoryTag = (description \ "directory")(0)

    val nodesTag = (directoryTag \ "nodes")(0)

    val array = new ArrayBuffer[C3FileSystemNode]

    for (nodeTag <- nodesTag \ "node") {
      val childName = (nodeTag \ "@name").text
      val childAddress = (nodeTag \ "@address").text
      val isFile = (nodeTag \ "@leaf").text == "true"

      val childFullName = (fullname match {
        case "/" => ""
        case s => s
      }) + "/" + childName

      val childData = (nodeTag \ "data") match {
        case NodeSeq.Empty => null
        case xml: NodeSeq => xml
      }

      val childMetaData: Map[String, String] = (nodeTag \ "metadata") match {
        case NodeSeq.Empty => Map()
        case xml: NodeSeq => {
          val elements = (xml \ "element").map {
            e =>
              ((e \ "@key").text -> (e \ "value").text)
          }.toMap

          elements
        }
      }

      val child: C3FileSystemNode = if (isFile) {
        if (childData != null) {
          val file: C3FileImpl = new C3FileImpl(system, childAddress, null, childName, childFullName) {
            _metadata = childMetaData
            loaded = true

            val createDate = ISODateTimeFormat.dateTime().parseDateTime((childData \ "@date").text).toDate

            _versions = List(new C3VersionImpl(this.system, this, createDate, 0, null, 0) {

              private val data = C3Base64Decoder.decodeBuffer(childData.text)

              override val length: Long = data.length

              private val inputStream = new ByteArrayInputStream(data)

              override def getData: C3ByteChannel = new C3ByteChannel {
                override def length: Long = data.length

                def readContentAsString: String = new String(data, "UTF-8")

                def isOpen = false

                def close() {}

                def read(p1: ByteBuffer) = 0
              }

              override def getDataStream: C3InputStream = new C3InputStream {
                override def length = data.length

                override def read() = inputStream.read()

                override def read(buf: scala.Array[scala.Byte]) = inputStream.read(buf)
              }
            })
          }
          file
        } else {
          new C3FileImpl(system, childAddress, null, childName, childFullName)
        }
      } else {
        new C3DirectoryImpl(system, childAddress, null, childName, childFullName)
      }

      array += child
    }

    _children = array.sortWith((first, second) => {
      if (first.getClass == second.getClass) {
        first.name.compareTo(second.name) < 0
      } else {
        first.isInstanceOf[C3Directory]
      }
    }).toList

    directoryLoaded = true
  }

  override def getChild(name: String, embedChildData: Boolean = false, embedChildMetaData: Set[String] = Set()): Option[C3FileSystemNode] = {
    children(embedChildData, embedChildMetaData).filter(_.name == name).headOption
  }

  def markDirty() {
    directoryLoaded = false
  }

  protected def preloadDir[T](embedChildrenData: Boolean, embedChildMetaData: Set[String] = Set())(value: => T): T = {
    if (!directoryLoaded) {
      log.debug("Directory {} is not loaded yet, loading...", address)

      val channel = system.getData(address, embedChildrenData, embedChildMetaData)

      val xml = XML.load(Channels.newReader(channel, "UTF-8"))
      updateFieldsFromDirectoryXml(xml)
    }
    value
  }

  override def toString: String = {
    val builder = new StringBuilder

    builder.append("[C3DirectoryImpl name=").append(_name)
      .append(", fullname=").append(_fullname)
      .append(", resource=").append(super.toString)
      .append("]")

    builder.toString()
  }

  private def createFullPath(basePath: String, name: String): String = {
    (basePath match {
      case "/" => ""  // this is workaround for files under root "/" directory: /rest/fs//name
      case path => path
    }) + "/" + name
  }
}

object C3DirectoryImpl {

  val log = LoggerFactory.getLogger(classOf[C3DirectoryImpl])

}