package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.{C3AccessException, C3ByteChannel, DataStream, C3System}
import org.aphreet.c3.platform.access.AccessManager
import org.aphreet.c3.platform.resource.{ResourceVersion, Resource}
import org.aphreet.c3.platform.accesscontrol._
import scala.Some
import org.aphreet.c3.platform.filesystem.FSManager

class LocalC3System(val domain: String) extends C3System with DataConverter{

  var accessManager: AccessManager = _

  var fsManager: FSManager = _

  var accessControlManager: AccessControlManager = _

  val accessControlParams = Map("domain" -> domain)

  def getData(ra: String): C3ByteChannel = {
    accessManager.getOption(ra) match {
      case Some(resource) => {

        retrieveAccessTokens(READ).checkAccess(resource)

        new LocalC3ByteChannel(resource.versions.last.data)
      }
      case None => throw new C3AccessException("Resource " + ra + " is not found")
    }
  }

  def getResource(ra: String, metadata: List[String]) {

    accessManager.getOption(ra) match {
      case Some(resource) => {
        retrieveAccessTokens(READ).checkAccess(resource)
        new LocalC3Resource(this, resource)
      }
      case None => throw new C3AccessException("Can't find resource for address " + ra)
    }
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
    fsManager.getNode(domain, name)
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

  def search(query: String) = null

  def retrieveAccessTokens(action:Action):AccessTokens = {
    accessControlManager.retrieveAccessTokens(LocalAccess, action, accessControlParams)
  }

  def update(resource:Resource) {
    accessManager.update(resource)
  }
}
