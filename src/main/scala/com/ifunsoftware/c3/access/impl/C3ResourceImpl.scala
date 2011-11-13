package com.ifunsoftware.c3.access.impl

import java.util.Date
import xml.NodeSeq
import org.joda.time.format.ISODateTimeFormat
import collection.mutable.ArrayBuffer
import com.ifunsoftware.c3.access.{DataStream, C3AccessException, C3Version, C3Resource}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ResourceImpl(val system:C3SystemImpl, val xml:NodeSeq) extends C3Resource{

  private var _address:String = null

  private var _date:Date = null

  private var _tracksVersions:Boolean = false

  private var _metadata:Map[String, String] = null

  private var _systemMetadata:Map[String, String] = null

  private var _versions:List[C3Version] = null

  {
    updateFromXmlDescription(xml)
  }

  override def address:String = _address

  override def date:Date = _date

  override def tracksVersions:Boolean = _tracksVersions

  override def metadata:Map[String, String] = _metadata

  override def systemMetadata:Map[String,  String] = _systemMetadata

  override def versions:List[C3Version] = _versions


  override def update(meta:Map[String, String], data:DataStream) {
    system.updateResource(address, meta, data)
  }

  override def update(meta:Map[String, String]){
    update(meta, null)
  }

  override def update(data:DataStream) {
    update(Map(), data)
  }


  override def toString:String = {

    val builder = new StringBuilder

    builder.append("C3ResourceImpl[address=").append(address)
      .append(", date=").append(date.getTime)
      .append(", trackVersions=").append(tracksVersions)
      .append(", meta=").append(metadata)
      .append(", sysmeta=").append(systemMetadata)
      .append(", versions=").append(versions)
      .append("]")

    builder.toString()
  }

  private def updateFromXmlDescription(xmlDescription:NodeSeq) {
    try{
      val resourceTag = (xmlDescription \ "resource") (0)

      _address = (resourceTag \ "@address").text

      _date = ISODateTimeFormat.dateTime().parseDateTime((resourceTag \ "@createDate").text).toDate

      _tracksVersions = (resourceTag \ "@trackVersions").text.toBoolean

      val metadataTag = (resourceTag \ "metadata") (0)

      _metadata = parseMetadata(metadataTag)

      val sysMetaTag = (resourceTag \ "systemMetadata") (0)

      _systemMetadata = parseMetadata(sysMetaTag)

      val versionsTag = (resourceTag \ "versions")(0)

      var array = ArrayBuffer[C3Version]()

      var number = 1

      for(versionTag <- versionsTag \ "version"){

        val versionDate = ISODateTimeFormat.dateTime().parseDateTime((versionTag \ "@date").text).toDate

        val versionMeta = parseMetadata((versionTag \ "systemMetadata") (0))

        val versionNumber = number

        array += (new C3VersionImpl(system, this, versionDate, versionMeta, versionNumber))

        number = number + 1
      }

      _versions = array.toList
    }catch{
      case e => throw new C3AccessException("Failed to parse resource xml", e)
    }
  }

  private def parseMetadata(metadataTag:NodeSeq):Map[String,  String] = {
    (metadataTag \ "element").map(e => ((e \ "@key").text, (e \ "value")(0).text)).toMap
  }
}