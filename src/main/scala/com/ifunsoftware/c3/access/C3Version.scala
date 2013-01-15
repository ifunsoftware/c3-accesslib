package com.ifunsoftware.c3.access

import java.util.Date

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3Version {

  def date: Date

  def length: Long

  def hash: String

  def getData: C3ByteChannel

  def getDataStream: C3InputStream
}