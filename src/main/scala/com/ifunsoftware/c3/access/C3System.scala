package com.ifunsoftware.c3.access

import fs.C3FileSystemNode

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3System {

  type Metadata = Map[String, String]

  def getData(ra: String): C3ByteChannel

  def getResource(ra: String, metadata: List[String] = List()): C3Resource

  def addResource(meta: Metadata, data: DataStream): String

  def getFile(name: String): C3FileSystemNode

  def deleteResource(ra: String)

  def deleteFile(name: String)

  def search(query: String): List[SearchResultEntry]

  def query(meta: Metadata, function: C3Resource => Unit)
}
