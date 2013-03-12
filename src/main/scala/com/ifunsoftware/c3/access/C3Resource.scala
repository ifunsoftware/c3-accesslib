package com.ifunsoftware.c3.access

import impl.MetadataHelper
import java.util.Date
import scala.collection.Map


/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3Resource {

  def address: String

  def date: Date

  def tracksVersions: Boolean

  def metadata: Map[String, String]

  def systemMetadata: Map[String, String]

  def versions: List[C3Version]

  def update(meta: MetadataChange, data: DataStream)

  def update(meta: MetadataChange)

  def update(data: DataStream)
}

trait MetadataValue{

  def get: String

  def getCollection: TraversableOnce[String]

}

case class StringMetadataValue(value: String) extends MetadataValue{
  def get = value

  def getCollection = MetadataHelper.parseSequence(value)
}

case class LongMetadataValue(value: Long) extends MetadataValue{
  def get = value.toString

  def getCollection = Some(value.toString)
}

case class CollectionMetadataValue(value: TraversableOnce[String]) extends MetadataValue{

  def get = "[" + value.map(_.replaceAll(",", "\\,")).mkString(",") + "]"

  def getCollection = value
}

class MetadataChange(val updated: Map[String, MetadataValue], val removed: List[String])

object MetadataKeep extends MetadataChange(Map(), Nil)

case class MetadataRemove(override val removed: List[String]) extends MetadataChange(Map(), removed)

case class MetadataUpdate(override val updated: Map[String, MetadataValue]) extends MetadataChange(updated, Nil)
