package com.ifunsoftware.c3.access.impl

import java.text.SimpleDateFormat
import java.util.{Locale, Date}
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.slf4j.LoggerFactory
import com.ifunsoftware.c3.access._
import org.apache.commons.httpclient.methods.multipart.{MultipartRequestEntity, StringPart, Part}
import xml._
import com.ifunsoftware.c3.access.C3System.Metadata
import com.ifunsoftware.c3.access.fs.C3FileSystemNode
import com.ifunsoftware.c3.access.fs.impl.{C3FileImpl, C3DirectoryImpl}
import java.io.{BufferedInputStream, InputStreamReader}
import java.nio.CharBuffer
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient._
import java.net.{URL, URLEncoder}
import params.HttpConnectionManagerParams
import io.Source
import scala.Some
import com.ifunsoftware.c3.access.SearchResultEntry
import scala.xml.pull._
import org.apache.commons.codec.binary.Base64

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3SystemImpl(val host: String,
                   val domain: String,
                   val key: String,
                   val maxConnections: Int = 100,
                   val proxyHost: String = null,
                   val proxyPort: Int = 0) extends C3System {

  val log = LoggerFactory.getLogger(getClass)

  val resourceRequestUri = "/rest/resource/"

  val fileRequestUri = "/rest/fs"

  val searchRequestUri = "/rest/search/"

  val httpClient = createHttpClient

  private def createHttpClient: HttpClient = {

    val hostConfiguration = getHostConfiguration

    val connectionParams = new HttpConnectionManagerParams()
    connectionParams.setMaxConnectionsPerHost(hostConfiguration, maxConnections)
    connectionParams.setMaxTotalConnections(maxConnections)

    val connectionManager = new MultiThreadedHttpConnectionManager
    connectionManager.setParams(connectionParams)

    val client = new HttpClient(connectionManager)
    client.setHostConfiguration(hostConfiguration)

    client
  }

  private def getHostConfiguration: HostConfiguration = {
    val url = new URL(host)

    val port = if (url.getPort == -1) {
      url.getDefaultPort
    } else {
      url.getPort
    }

    val hostname = url.getHost
    val protocol = url.getProtocol

    val hostConfiguration = new HostConfiguration()
    hostConfiguration.setHost(hostname, port, protocol)

    if (proxyHost != null) {
      hostConfiguration.setProxy(proxyHost, proxyPort)
    }

    hostConfiguration
  }


  def getMetadataForName(name: String): NodeSeq = getMetadataInternal(fileRequestUri + name)


  def getMetadataForAddress(ra: String, extendedMeta: List[String] = List()): NodeSeq = getMetadataInternal(resourceRequestUri + ra, extendedMeta)

  protected def getMetadataInternal(relativeUrl: String, extendedMeta: List[String] = List()): NodeSeq = {

    log.debug("Loading metadata for url '{}' including extended keys: '{}'", relativeUrl, extendedMeta)

    val method = createGetMethod(relativeUrl, metadata = true)

    if (!extendedMeta.isEmpty) {
      val header = new Header("x-c3-extmeta", extendedMeta.reduceLeft(_ + "," + _))
      log.debug("Header value for extended meta '{}'", header.getValue)
      method.addRequestHeader(header)
    }

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => {
          val xml = XML.load(method.getResponseBodyAsStream)
          log.trace("Got XML repsonse for url '{}': {}", relativeUrl, xml)
          xml
        }
        case _ => handleError(status, method); null
      }
    })
  }

  override def getResource(ra: String, metadata: List[String] = List()): C3Resource = new C3ResourceImpl(this, ra, getMetadataForAddress(ra, metadata))

  override def getFile(fullname: String): C3FileSystemNode = {

    log.debug("Loading file '{}'", fullname)

    val metadata = getMetadataForName(encodeFilePath(fullname))

    val resource = new C3ResourceImpl(this, null, metadata)

    val isDir = resource.systemMetadata.getOrElse("c3.fs.nodetype", "") == "directory"

    val name = resource.systemMetadata.get("c3.fs.nodename") match {
      case Some(value) => value
      case None => {
        if (fullname == "/") "/"
        else throw new C3AccessException("File " + fullname + " does not contain 'c3.fs.nodename' system metadata")
      }
    }

    if (isDir) {
      new C3DirectoryImpl(this, resource.address, metadata, name, fullname)
    } else {
      new C3FileImpl(this, resource.address, metadata, name, fullname)
    }
  }

  def deleteFile(name: String) {

    log.debug("Deleting file '{}'", name)

    val method = createDeleteMethod(fileRequestUri + encodeFilePath(name))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  override def addResource(meta: Metadata, data: DataStream): String = {
    addDataInternal(resourceRequestUri, meta, data)
  }

  def addFile(fullname: String, meta: Metadata, data: DataStream): String = {
    val relativeUrl = fileRequestUri + encodeFilePath(fullname)

    addDataInternal(relativeUrl, meta, data)
  }

  private def addDataInternal(relativeUrl: String, meta: Metadata, data: DataStream): String = {

    log.debug("Creating object '{}' with metadata '{}'", relativeUrl, meta)

    val method = createPostMethod(relativeUrl)

    method.setRequestEntity(new MultipartRequestEntity(createPartsArray(meta, data), method.getParams))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_CREATED => {

          val xml = XML.load(method.getResponseBodyAsStream)

          val uploadedTags = (xml \ "uploaded")

          if (uploadedTags.size > 0) {
            ((uploadedTags(0)) \ "@address").text
          } else {
            null
          }
        }
        case _ => handleError(status, method); null
      }
    })
  }

  def addDirectory(fullname: String, meta: Metadata) {

    log.debug("Creating directory '{}' with metadata '{}'", fullname, meta)

    val method = createPostMethod(fileRequestUri + encodeFilePath(fullname))

    method.addRequestHeader(new Header("x-c3-nodetype", "directory"))

    meta foreach { case (k,v) => method.addRequestHeader(
      new Header("x-c3-metadata", k + ":" + new String(Base64.encodeBase64(v.get.getBytes("UTF-8")), "UTF-8")))
    }

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_CREATED => Unit
        case _ => handleError(status, method); null
      }
    })
  }

  override def search(query: String): List[SearchResultEntry] = {

    log.debug("Running search query '{}'", query)

    val method = createGetMethod(searchRequestUri + URLEncoder.encode(query, "UTF-8"))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => {

          val xml = XML.load(method.getResponseBodyAsStream)

          log.trace("Got xml response for query '{}': {} ", query, xml)

          val results = SearchResultEntryParser.parse(xml)
          log.debug("Found '{}' resources ", results.length)
          results
        }
        case _ => handleError(status, method); List()
      }
    })

  }

  def updateResource(address: String, meta: Metadata, removeMeta: List[String], data: Option[DataStream]): Int = {

    log.debug("Updating resource '{}'", address)
    log.trace("Adding metadata: '{}', removing metadata: '{}'", meta, removeMeta)

    val method = createPutMethod(resourceRequestUri + address)

    for((key, value) <- meta){
      method.addRequestHeader("x-c3-metadata", key + ":" +
        new String(Base64.encodeBase64(value.get.getBytes("UTF-8")), "UTF-8"))
    }

    for(key <- removeMeta){
      method.addRequestHeader("x-c3-metadata-delete", key)
    }

    data.foreach{ data => method.setRequestEntity(data.createRequestEntity)}

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => {

          val xml = XML.load(method.getResponseBodyAsStream)

          ((xml \\ "uploaded")(0) \ "@version" text).toInt
        }
        case _ => handleError(status, method); Int.MinValue
      }
    })
  }

  override def deleteResource(address: String) {

    log.debug("Deleting resource '{}'", address)

    val method = createDeleteMethod(resourceRequestUri + address)

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  override
  def getData(ra: String): C3ByteChannel =
    getData(ra, embedData = false, embedChildMetaData = Set())

  def getData(ra: String, embedData: Boolean = false, embedChildMetaData: Set[String] = Set()): C3ByteChannel =
    getDataInternal(ra, 0, embedData, embedChildMetaData)

  def getDataInternal(address: String, version: Int, embedData: Boolean = false, embedChildMetaData: Set[String] = Set()): C3ByteChannel = {

    log.debug("Loading data '{}' of version '{}'", address, version)
    log.trace("Load children data: '{}', children metadata: '{}'", embedData, embedChildMetaData)

    val relativeUrl = if (version > 0) {
      resourceRequestUri + address + "/" + version
    } else {
      resourceRequestUri + address
    }

    val method = createGetMethod(relativeUrl, embedData = embedData, embedChildMetaData = embedChildMetaData)

    val status = httpClient.executeMethod(method)
    status match {
      case HttpStatus.SC_OK => new C3ByteChannelImpl(method)
      case _ =>
        try {
          method.releaseConnection()
        } catch {
          case e: Throwable => //do nothing here
        }
        handleError(status, method)
        null
    }
  }

  def getDataAsStreamInternal(address: String, version: Int): C3InputStream = {
    log.debug("Loading data '{}' of version '{}'", address, version)

    val relativeUrl = if (version > 0) {
      resourceRequestUri + address + "/" + version
    } else {
      resourceRequestUri + address
    }

    val method = createGetMethod(relativeUrl)

    val status = httpClient.executeMethod(method)
    status match {
      case HttpStatus.SC_OK => new C3InputStreamImpl(method)
      case _ =>
        try {
          method.releaseConnection()
        } catch {
          case e: Throwable => //do nothing here
        }
        handleError(status, method)
        null
    }
  }

  def moveFile(path: String, newPath: String) {

    log.debug("Moving file from '{}' to '{}' ", path, newPath)

    val method = createPutMethod(fileRequestUri + encodeFilePath(path))

    method.addRequestHeader(new Header("x-c3-op", "move"))
    method.setRequestEntity(new StringRequestEntity(newPath, "text/plain", "UTF-8"))


    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  protected def createPartsArray(meta: Metadata, data: DataStream): Array[Part] = {

    var parts: List[Part] = meta.map(e => {
      val part = new StringPart(e._1, e._2.get, "UTF-16")
      part.setCharSet("UTF-8")
      part
    }).toList

    if (data != null) {
      parts = data.createFilePart :: parts
    }

    parts.toArray
  }

  protected def handleError(status: Int, method: HttpMethodBase) {

    val contentType = method.getResponseHeader("Content-Type").getValue

    if (contentType.startsWith("application/xml")) {
      val xml = XML.load(method.getResponseBodyAsStream)

      val errorTag = (xml \ "error")(0)

      val message = ((errorTag \ "message")(0)).text

      val exception = ((errorTag \ "exception")(0)).text

      if (exception.length() > 0) {
        log.error("Failed to execute request, status " + status + ", stacktrace:\n" + exception)
      }

      log.debug("Failed to execute request: http status code: {} message: '{}'", status, message)

      throw new C3AccessException(message, status)
    } else {

      val reader = new InputStreamReader(method.getResponseBodyAsStream, "UTF-8")

      val BUFFER_SIZE = 2048

      val buffer = CharBuffer.allocate(BUFFER_SIZE)

      reader.read(buffer)

      log.error("Failed to execute request to {}, server output: {}", method.getURI.toString, buffer.toString)

      reader.close()

      throw new C3AccessException(("Filed to execute method, http status is " + status), status)
    }
  }

  private def createGetMethod(relativeUrl: String,
                              metadata: Boolean = false,
                              embedData: Boolean = false,
                              embedChildMetaData: Set[String] = Set()): HttpMethodBase = {
    val method =
      if (metadata)
        new GetMethod(host + relativeUrl + "?metadata")
      else
        new GetMethod(host + relativeUrl)

    if (embedData) {
      val embedDataHeader = new Header("x-c3-data", embedData.toString)
      method.addRequestHeader(embedDataHeader)
    }
    if (!embedChildMetaData.isEmpty) {
      val embedChildMetaDataHeader = new Header("x-c3-meta", embedChildMetaData.mkString(","))
      method.addRequestHeader(embedChildMetaDataHeader)
    }

    addAuthHeaders(method, relativeUrl)

    method
  }

  def query(meta: Metadata, function: C3Resource => Unit) {

    val method = new GetMethod(host + "/rest/query")

    method.setQueryString(meta.map(e => new NameValuePair(e._1, e._2.get)).toArray)

    addAuthHeaders(method, "/rest/query")

    executeMethod(method, status =>
      status match {
        case HttpStatus.SC_OK => {
          val source = Source.fromInputStream(new BufferedInputStream(method.getResponseBodyAsStream))
          val er = new XMLEventReader(source)

          def processResourceXml(xml: Node){
            val resourceTag = (xml \ "resource")(0)
            val _address = (resourceTag \ "@address").text
            val resource = new C3ResourceImpl(this, _address, xml)
            function(resource)
          }

          try {
            var resourceData: StringBuilder = new StringBuilder

            while (er.hasNext) {
              er.next() match {
                case EvElemStart(_, "resources", _, _) => // Do nothing
                case EvElemEnd(_, "resources") => // Do nothing
                case x @ EvElemEnd(_, "resource") =>
                  resourceData append backToXml(x)
                  val resourceNode = XML.loadString(wrapResourceXmlData(resourceData.result()))
                  processResourceXml(resourceNode)
                  resourceData = new StringBuilder
                case x @ EvElemStart(_, label, _, _) =>
                  resourceData append backToXml(x)
                case x @ EvElemEnd(_, label) =>
                  resourceData append backToXml(x)
                case EvText(text) if resourceData != null =>
                  resourceData append text
                case EvEntityRef(entity) => // TODO
                case _ => // ignore everything else
              }
            }
          } finally {
            source.close()
          }
        }
        case _ => throw new C3AccessException("Failed to execute query, status is " + status, status)
      }
    )
  }

  private def createPostMethod(relativeUrl: String): PostMethod = {
    val method = new PostMethod(host + relativeUrl)
    addAuthHeaders(method, relativeUrl)
    method
  }

  private def createPutMethod(relativeUrl: String): PutMethod = {
    val method = new PutMethod(host + relativeUrl)
    addAuthHeaders(method, relativeUrl)
    method
  }

  private def createDeleteMethod(relativeUrl: String): HttpMethodBase = {

    val method = new DeleteMethod(host + relativeUrl)

    addAuthHeaders(method, relativeUrl)

    method
  }

  def makeCleanUrl(url: String): String = {
    url.replaceAll("/+", "/")
  }

  /**
   * @param relativeUrl url without hostname, i.e. /rest/resource/<address>
   */
  private def addAuthHeaders(method: HttpMethodBase, relativeUrl: String) {
    if (domain != "anonymous") {

      val cleanUrl = makeCleanUrl(relativeUrl)

      val dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)

      val dateString = dateFormat.format(new Date())

      val hashBase = cleanUrl + dateString + domain

      val hash = hmac(key, hashBase)

      log.debug("Calculated hash {} for input parameters: {} ", hash, Array(cleanUrl, domain, dateString))

      val header = new Header("x-c3-sign", hash)
      method.addRequestHeader(header)


      val domainHeader = new Header("x-c3-domain", domain)
      method.addRequestHeader(domainHeader)

      val dateHeader = new Header("x-c3-date", dateString)
      method.addRequestHeader(dateHeader)
    }
  }

  private def hmac(key: String, input: String): String = {

    val mac = Mac.getInstance("HmacSHA256")

    val secret = new SecretKeySpec(key.getBytes, "HmacSHA256")
    mac.init(secret)

    val digest = mac.doFinal(input.getBytes("UTF-8"))

    val hexString = new StringBuilder

    for (b <- digest) {
      if ((0xFF & b) < 0x10) {
        hexString.append("0").append(Integer.toHexString((0xFF & b)))
      } else {
        hexString.append(Integer.toHexString((0xFF & b)))
      }
    }

    hexString.toString()
  }

  private def executeMethod[T](method: HttpMethodBase, f: Int => T): T = {

    try {
      val status = httpClient.executeMethod(method)
      f(status)
    } finally {
      method.releaseConnection()
    }
  }

  // xml streaming helpers
  private def backToXml(ev: XMLEvent) = {
    ev match {
      case EvElemStart(pre, label, attrs, scope) => {
        "<" + label + attrsToString(attrs) + ">"
      }
      case EvElemEnd(pre, label) => {
        "</" + label + ">"
      }
      case _ => ""
    }
  }

  private def attrsToString(attrs: MetaData) = {
    attrs.length match {
      case 0 => ""
      case _ => attrs.map( (m:MetaData) => " " + m.key + "='" + m.value +"'" ).reduceLeft(_+_)
    }
  }

  private def wrapResourceXmlData(data: String) = {
    // That's ugly a bit. TODO refactor
    """<?xml version="1.0" encoding="UTF-8"?>
       <p:response xmlns:p="http://c3.aphreet.org/rest/1.0" xsi:schemaLocation="http://c3.aphreet.org/rest/1.0 http://c3-system.googlecode.com/files/rest.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
       <info version="1.0" status="OK"/>
    """ + data +
    "</p:response>"
  }

  private def encodeFilePath(path: String): String = {
    path.split("/").map(URLEncoder.encode(_, "UTF-8")).mkString("/")
  }
}