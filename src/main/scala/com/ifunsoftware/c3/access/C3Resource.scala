package com.ifunsoftware.c3.access

import java.util.Date

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

class MetadataChange(val updated: Map[String, String], val removed: List[String]){

  def this(updated: Map[String, String]) = this(updated, Nil)

  def this(removed: List[String]) = this(Map(), removed)

}

object MetadataKeep extends MetadataChange(Map(), Nil)

case class MetadataRemove(override val removed: List[String]) extends MetadataChange(Map(), removed)

case class MetadataUpdate(override val updated: Map[String, String]) extends MetadataChange(updated, Nil)
