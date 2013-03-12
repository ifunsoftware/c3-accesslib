package com.ifunsoftware.c3.access

import fs.C3FileSystemNode
import scala.collection.Map

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3System {

  type Metadata = Map[String, MetadataValue]

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

  implicit def stringMapToMetadataValueMap(map: Map[String, String]): Map[String, MetadataValue] = {
    map.map(e => (e._1, StringMetadataValue(e._2))).toMap
  }

  implicit def stringImmutableMapToMetadataValueMap(map: scala.collection.immutable.Map[String, String]): Map[String, MetadataValue] = {
    map.map(e => (e._1, StringMetadataValue(e._2))).toMap
  }

  implicit def metadataValueToString(value: MetadataValue): String = value.get

  implicit def metadataValueToCollection(value: MetadataValue): TraversableOnce[String] = value.getCollection

  implicit def stringToMetadataValue(value: String): MetadataValue = StringMetadataValue(value)

  implicit def longToMetadataValue(value: Long): MetadataValue = LongMetadataValue(value)

  implicit def collectionToMetadataValue(value: TraversableOnce[String]) = CollectionMetadataValue(value)

  implicit def metadataMapToStringMap(map: Map[String, MetadataValue]): Map[String, String] = {
    map.map(e => (e._1, e._2.get)).toMap
  }

  implicit def immutableMetadataMapToStringMap(map: scala.collection.immutable.Map[String, MetadataValue]): Map[String, String] = {
    map.map(e => (e._1, e._2.get)).toMap
  }
}