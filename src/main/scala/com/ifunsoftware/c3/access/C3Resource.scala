package com.ifunsoftware.c3.access

import java.io.InputStream
import java.util.Date

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3Resource {

  def address:String

  def date:Date

  def tracksVersions:Boolean

  def metadata:Map[String, String]

  def systemMetadata:Map[String,  String]

  def versions:List[C3Version]


  def setMetadataValue(key:String,  value:String)

  def update(stream:InputStream)

  def commit()
}