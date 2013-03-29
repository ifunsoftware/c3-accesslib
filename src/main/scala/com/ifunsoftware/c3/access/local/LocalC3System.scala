package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access._
import com.ifunsoftware.c3.access.local.fs.LocalC3FileSystemNode
import com.ifunsoftware.c3.access.C3System.Metadata
import org.aphreet.c3.platform.access.AccessManager
import org.aphreet.c3.platform.resource.{ResourceVersion, Resource}
import org.aphreet.c3.platform.accesscontrol._
import org.aphreet.c3.platform.filesystem.{FSNotFoundException, FSManager}
import scala.Some
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import org.aphreet.c3.platform.domain.{Domain, DomainManager}
import org.aphreet.c3.platform.search.SearchManager
import org.aphreet.c3.platform.query.{QueryConsumer, QueryManager}
import com.ifunsoftware.c3.access.fs.C3FileSystemNode

class LocalC3System(val domain: String, val bundleContext: AnyRef) extends C3System with DataConverter {

  thisC3system =>

  val accessManager = resolveService(classOf[AccessManager])

  val fsManager = resolveService(classOf[FSManager])

  val accessControlManager = resolveService(classOf[AccessControlManager])

  val domainManager = resolveService(classOf[DomainManager])

  val searchManager = resolveService(classOf[SearchManager])

  val queryManager = resolveService(classOf[QueryManager])

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

  def fetchResource(ra: String): Resource = {
    accessManager.getOption(ra) match {
      case Some(resource) => {
        retrieveAccessTokens(READ).checkAccess(resource)
        resource
      }
      case None => throw new C3AccessException("Can't find resource for address " + ra)
    }
  }

  def getData(ra: String): Option[C3ByteChannel] = {
    accessManager.getOption(ra) match {
      case Some(resource) => {

        retrieveAccessTokens(READ).checkAccess(resource)

        Some(new LocalC3ByteChannel(resource.versions.last.data))
      }
      case None => None
    }
  }

  def getResource(ra: String, metadata: List[String]): Option[C3Resource] = {

    accessManager.getOption(ra) match {
      case Some(resource) => Some(new LocalC3Resource(this, resource))
      case None => None
    }
  }

  def addResource(meta: Metadata, data: DataStream): String = {

    val accessTokens = retrieveAccessTokens(CREATE)

    val resource = new Resource
    for((key, value) <- meta){
      resource.metadata(key) = value.get
    }

    resource.addVersion(ResourceVersion(data))

    accessTokens.updateMetadata(resource)

    accessManager.add(resource)
  }

  def getFile(name: String): Option[C3FileSystemNode] = {
    try {
      val internalNode = fsManager.getNode(domainId, name)

      retrieveAccessTokens(READ).checkAccess(internalNode.resource)

      Some(LocalC3FileSystemNode(this, internalNode, name))
    }catch {
      case e: FSNotFoundException => None
      case e: Throwable => throw new C3AccessException("Can't get file with name " + name + " in domain " + domain, e)
    }
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

  def retrieveAccessTokens(action: Action): AccessTokens = {
    accessControlManager.retrieveAccessTokens(LocalAccess, action, accessControlParams)
  }

  def update(resource: Resource) {
    accessManager.update(resource)
  }

  def createDirectory(fullName: String, meta: Metadata) {

    retrieveAccessTokens(CREATE)

    fsManager.createDirectory(domainId, fullName, C3System.metadataToStringMap(meta).toMap)
  }

  def createFile(fullName: String, meta: Metadata, data: DataStream) {

    val accessTokens = retrieveAccessTokens(CREATE)

    val resource = new Resource

    for((key, value) <- meta){
      resource.metadata(key) = value.get
    }

    resource.addVersion(ResourceVersion(data))

    accessTokens.updateMetadata(resource)

    fsManager.createFile(domainId, fullName, resource)
  }

  def move(oldPath: String, newPath: String) {
    fsManager.moveNode(domainId, oldPath, newPath)
  }

  def search(query: String): List[SearchResultEntry] = {
    searchManager.search(domainId, query)
      .map(element => SearchResultEntry(element.address, element.path, element.score, element.fragments.map(
      fragment => SearchResultFragment(fragment.field, fragment.foundStrings.toList)).toList
    )).toList
  }

  def query(meta: Metadata, function: C3Resource => Unit) {
    queryManager.executeQuery(fields = C3System.metadataToStringMap(meta).toMap, systemFields = Map(Domain.MD_FIELD -> domainId), consumer = new QueryConsumer {
      def close() {}

      def consume(resource: Resource): Boolean = {
        val c3resource = new LocalC3Resource(thisC3system, resource)
        function(c3resource)
        true
      }
    })
  }

  def resolveService[T](clazz: Class[T]): T = {

    LocalC3System.log.info("Resolving service {}", clazz.getCanonicalName)

    val context = bundleContext.asInstanceOf[BundleContext]

    val reference = context.getServiceReference(clazz.getCanonicalName)

    context.getService(reference).asInstanceOf[T]
  }
}

object LocalC3System {

  val log = LoggerFactory.getLogger(classOf[LocalC3System])
}
