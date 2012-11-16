package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access._
import com.ifunsoftware.c3.access.local.fs.LocalC3FileSystemNode
import org.aphreet.c3.platform.access.AccessManager
import org.aphreet.c3.platform.resource.{ResourceVersion, Resource}
import org.aphreet.c3.platform.accesscontrol._
import org.aphreet.c3.platform.filesystem.FSManager
import scala.Some

class LocalC3System(val domain: String) extends C3System with DataConverter{

  var accessManager: AccessManager = _

  var fsManager: FSManager = _

  var accessControlManager: AccessControlManager = _

  val accessControlParams = Map("domain" -> domain)

  def fetchResource(ra: String):Resource = {
    accessManager.getOption(ra) match {
      case Some(resource) => {
        retrieveAccessTokens(READ).checkAccess(resource)
        resource
      }
      case None => throw new C3AccessException("Can't find resource for address " + ra)
    }
  }

  def getData(ra: String): C3ByteChannel = {
    accessManager.getOption(ra) match {
      case Some(resource) => {

        retrieveAccessTokens(READ).checkAccess(resource)

        new LocalC3ByteChannel(resource.versions.last.data)
      }
      case None => throw new C3AccessException("Resource " + ra + " is not found")
    }
  }

  def getResource(ra: String, metadata: List[String]):C3Resource = {
    new LocalC3Resource(this, ra)
  }

  def addResource(meta: Map[String, String], data: DataStream): String = {

    val accessTokens = retrieveAccessTokens(CREATE)

    val resource = new Resource
    resource.metadata ++= meta

    resource.addVersion(ResourceVersion(data))

    accessTokens.updateMetadata(resource)

    accessManager.add(resource)
  }

  def getFile(name: String) = {
    val internalNode = fsManager.getNode(domain, name)

    retrieveAccessTokens(READ).checkAccess(internalNode.resource)

    new LocalC3FileSystemNode(this, internalNode, name)
  }

  def deleteResource(ra: String) {

    val accessTokens = retrieveAccessTokens(DELETE)

    accessManager.getOption(ra) match {
      case Some(resource) => {
        accessTokens.checkAccess(resource)
        accessManager.delete(ra)
      }
      case None => throw new C3AccessException("Can't find resource " + ra)
    }
  }

  def deleteFile(name: String) {
    val node = fsManager.getNode(domain, name)

    retrieveAccessTokens(DELETE).checkAccess(node.resource)

    fsManager.deleteNode(domain, name)
  }

  def search(query: String):List[SearchResultEntry] = null

  def retrieveAccessTokens(action:Action):AccessTokens = {
    accessControlManager.retrieveAccessTokens(LocalAccess, action, accessControlParams)
  }

  def update(resource:Resource) {
    accessManager.update(resource)
  }

  def createDirectory(fullName:String){

    retrieveAccessTokens(CREATE)

    fsManager.createDirectory(domain, fullName)
  }

  def createFile(fullName:String, meta: Map[String, String], data: DataStream){

    val accessTokens = retrieveAccessTokens(CREATE)

    val resource = new Resource
    resource.metadata ++= meta

    resource.addVersion(ResourceVersion(data))

    accessTokens.updateMetadata(resource)

    fsManager.createFile(domain ,fullName, resource)
  }

  def move(oldPath: String, newPath: String){
    fsManager.moveNode(domain, oldPath, newPath)
  }

}
