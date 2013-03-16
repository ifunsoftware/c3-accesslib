package com.ifunsoftware.c3.access

import fs.C3FileSystemNode
import scala.collection.Map

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3System {

  import C3System._

  def getData(ra: String): C3ByteChannel

  def getResource(ra: String, metadata: List[String] = List()): C3Resource

  def addResource(meta: Metadata, data: DataStream): String

  def getFile(name: String): C3FileSystemNode

  def deleteResource(ra: String)

  def deleteFile(name: String)

  def search(query: String): List[SearchResultEntry]

  def query(meta: Metadata, function: C3Resource => Unit)
}

object C3System{

  type Metadata = Map[String, MetadataValue]

  implicit def stringMapToMetadata(map: Map[String, String]): Metadata = {
    map.map(e => (e._1, StringMetadataValue(e._2)))
  }

  implicit def stringImmutableMapToMetadata(map: scala.collection.immutable.Map[String, String]): Metadata = {
    map.map(e => (e._1, StringMetadataValue(e._2)))
  }

  implicit def metadataValueToString(value: MetadataValue): String = value.get

  implicit def metadataValueToCollection(value: MetadataValue): TraversableOnce[String] = value.getCollection

  implicit def stringToMetadataValue(value: String): MetadataValue = StringMetadataValue(value)

  implicit def longToMetadataValue(value: Long): MetadataValue = LongMetadataValue(value)

  implicit def collectionToMetadataValue(value: TraversableOnce[String]) = CollectionMetadataValue(value)

  implicit def metadataToStringMap(map: Metadata): Map[String, String] = {
    map.map(e => (e._1, e._2.get))
  }

}