package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access._
import com.ifunsoftware.c3.access.local.fs.LocalC3FileSystemNode
import org.aphreet.c3.platform.access.AccessManager
import org.aphreet.c3.platform.resource.{ResourceVersion, Resource}
import org.aphreet.c3.platform.accesscontrol._
import org.aphreet.c3.platform.filesystem.FSManager
import scala.Some
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import org.aphreet.c3.platform.domain.DomainManager

class LocalC3System(val domain: String, val bundleContext:AnyRef) extends C3System with DataConverter{

  val accessManager = resolveService(classOf[AccessManager])

  val fsManager = resolveService(classOf[FSManager])

  val accessControlManager: AccessControlManager = resolveService(classOf[AccessControlManager])

  val domainManager: DomainManager = resolveService(classOf[DomainManager])

  val domainId = {
    domainManager.domainById(domain) match {
      case Some(domainInstance) => domainInstance.id
      case None => domainManager.domainList.filter(_.name == domain).headOption match {
        case Some(domainInstance) => domainInstance.id
        case None => throw new C3AccessException("Can't find domain: " + domain)
      }
    }
  }

  val accessControlParams = Map("domain" -> domainId)

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
    val internalNode = fsManager.getNode(domainId, name)

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
    val node = fsManager.getNode(domainId, name)

    retrieveAccessTokens(DELETE).checkAccess(node.resource)

    fsManager.deleteNode(domainId, name)
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

    fsManager.createDirectory(domainId, fullName)
  }

  def createFile(fullName:String, meta: Map[String, String], data: DataStream){

    val accessTokens = retrieveAccessTokens(CREATE)

    val resource = new Resource
    resource.metadata ++= meta

    resource.addVersion(ResourceVersion(data))

    accessTokens.updateMetadata(resource)

    fsManager.createFile(domainId ,fullName, resource)
  }

  def move(oldPath: String, newPath: String){
    fsManager.moveNode(domainId, oldPath, newPath)
  }

  def resolveService[T](clazz:Class[T]):T = {

    LocalC3System.log.info("Resolving service {}", clazz.getCanonicalName)

    val context = bundleContext.asInstanceOf[BundleContext]

    val reference = context.getServiceReference(clazz.getCanonicalName)

    context.getService(reference).asInstanceOf[T]
  }
}

object LocalC3System{

  val log = LoggerFactory.getLogger(classOf[LocalC3System])
}
