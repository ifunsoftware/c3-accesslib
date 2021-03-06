package com.ifunsoftware.c3.access.impl

import java.util.Date
import xml.NodeSeq
import org.joda.time.format.ISODateTimeFormat
import collection.mutable.ArrayBuffer
import com.ifunsoftware.c3.access._
import org.slf4j.LoggerFactory
import scala.collection.Map

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ResourceImpl(val system: C3SystemImpl, var _address: String, val xml: NodeSeq) extends C3Resource {

  private val log = C3ResourceImpl.log

  private var _date: Date = null

  private var _tracksVersions: Boolean = false

  protected var _metadata: Map[String, String] = null

  private var _systemMetadata: Map[String, String] = null

  protected var _versions: List[C3Version] = null

  protected var loaded = false

  {
    if (xml != null) {
      updateFieldsFromXmlDescription(xml)
    }
  }

  def this(system: C3SystemImpl, address: String) = this(system, address, null)

  override def address: String = _address

  override def date: Date = preload {
    _date
  }

  override def tracksVersions: Boolean = preload {
    _tracksVersions
  }

  override def metadata: Map[String, String] = preload {
    _metadata
  }

  override def systemMetadata: Map[String, String] = preload {
    _systemMetadata
  }

  override def versions: List[C3Version] = preload {
    _versions
  }

  protected def updateInternal(meta: MetadataChange, data: Option[DataStream]) {
    system.updateResource(address, meta.updated, meta.removed, data)
    loaded = false
  }

  override def update(meta: MetadataChange, data: DataStream) {
    updateInternal(meta, Some(data))
  }

  override def update(meta: MetadataChange) {
    updateInternal(meta, None)
  }

  override def update(data: DataStream) {
    updateInternal(MetadataKeep, Some(data))
  }

  override def toString: String = {

    val builder = new StringBuilder

    if (loaded) {
      builder.append("C3ResourceImpl[address=").append(address)
        .append(", date=").append(date.getTime)
        .append(", trackVersions=").append(tracksVersions)
        .append(", meta=").append(metadata)
        .append(", sysmeta=").append(systemMetadata)
        .append(", versions=").append(versions)
        .append("]")
    } else {
      builder.append("C3ResourceImpl[address=").append(address).append("]")
    }

    builder.toString()
  }

  private def updateFieldsFromXmlDescription(xmlDescription: NodeSeq) {
    try {
      val resourceTag = (xmlDescription \ "resource")(0)

      _address = (resourceTag \ "@address").text

      _date = ISODateTimeFormat.dateTime().parseDateTime((resourceTag \ "@createDate").text).toDate

      _tracksVersions = (resourceTag \ "@trackVersions").text.toBoolean

      val metadataTag = (resourceTag \ "metadata")(0)

      _metadata = parseMetadata(metadataTag)

      val sysMetaTag = (resourceTag \ "systemMetadata")(0)

      _systemMetadata = parseMetadata(sysMetaTag)

      val versionsTag = (resourceTag \ "versions")(0)

      var array = ArrayBuffer[C3Version]()

      var number = 1

      for (versionTag <- versionsTag \ "version") {

        val versionDate = ISODateTimeFormat.dateTime().parseDateTime((versionTag \ "@date").text).toDate

        val dataLength = (versionTag \ "@length").text.toLong

        val dataHash = (versionTag \ "@hash").text

        val versionNumber = number

        array += new C3VersionImpl(system, this, versionDate, dataLength, dataHash, versionNumber)

        number = number + 1
      }

      _versions = array.toList

      loaded = true
    } catch {
      case e: Throwable => throw new C3UnknownErrorException("Failed to parse resource definition", e)
    }
  }

  protected def preload[T](value: => T): T = {
    if (!loaded) {

      log.debug("Resource {} is not loaded yet, loading...", address)

      updateFieldsFromXmlDescription(system.getMetadataForAddress(address))
    }
    value
  }

  private def parseMetadata(metadataTag: NodeSeq): Map[String, String] = {
    (metadataTag \ "element").map(e => ((e \ "@key").text, (e \ "value")(0).text)).toMap
  }
}

object C3ResourceImpl {

  val log = LoggerFactory.getLogger(classOf[C3ResourceImpl])

}