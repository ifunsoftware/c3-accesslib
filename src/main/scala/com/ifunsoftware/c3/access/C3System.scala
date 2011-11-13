package com.ifunsoftware.c3.access

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

trait C3System {

  def getData(ra:String):C3ByteChannel

  def getResource(ra:String):C3Resource

  def addResource(meta:Map[String, String], data:DataStream):String

  def deleteResource(ra:String)
}