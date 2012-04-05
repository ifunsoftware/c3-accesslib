package com.ifunsoftware.c3.access.impl

import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.slf4j.LoggerFactory
import com.ifunsoftware.c3.access._
import org.apache.commons.httpclient.methods.multipart.{MultipartRequestEntity, StringPart, Part}
import xml.{NodeSeq, XML}
import com.ifunsoftware.c3.access.fs.C3FileSystemNode
import com.ifunsoftware.c3.access.fs.impl.{C3FileImpl, C3DirectoryImpl}
import java.io.InputStreamReader
import java.nio.CharBuffer
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient._
import java.net.URLEncoder

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3SystemImpl(val host:String,  val domain:String,  val key:String) extends C3System{

  val log = LoggerFactory.getLogger(getClass)

  val resourceRequestUri = "/rest/resource/"

  val fileRequestUri = "/rest/fs"

  val searchRequestUri = "/rest/search/"

  val httpClient = new HttpClient


  def getMetadataForName(name:String):NodeSeq = getMetadataInternal(fileRequestUri + name)


  def getMetadataForAddress(ra:String, extendedMeta:List[String] = List()):NodeSeq = getMetadataInternal(resourceRequestUri + ra, extendedMeta)

  protected def getMetadataInternal(relativeUrl:String, extendedMeta:List[String] = List()):NodeSeq = {

    val method = createGetMethod(relativeUrl, true)
    
    if(!extendedMeta.isEmpty){
      val header = new Header("x-c3-extmeta", extendedMeta.reduceLeft(_ + "," + _))
      log.debug("Header value for extended meta {}", header.getValue)
      method.addRequestHeader(header)
    }

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => {
          XML.load(method.getResponseBodyAsStream)
        }
        case _ => handleError(status, method); null
      }
    })
  }

  override def getResource(ra:String, metadata:List[String] = List()):C3Resource = new C3ResourceImpl(this, ra, getMetadataForAddress(ra, metadata))


  override def getFile(fullname:String):C3FileSystemNode = {
    val metadata = getMetadataForName(fullname)

    val resource = new C3ResourceImpl(this, null, metadata)

    val isDir = resource.systemMetadata.getOrElse("c3.fs.nodetype", "") == "directory"

    val name = resource.systemMetadata.get("c3.fs.nodename") match {
      case Some(value) => value
      case None => {
        if(fullname == "/") "/"
        else throw new C3AccessException("File " + fullname + " does not contain 'c3.fs.nodename' system metadata")
      }
    }

    if(isDir){
      new C3DirectoryImpl(this, resource.address, metadata, name, fullname)
    }else{
      new C3FileImpl(this, resource.address, metadata, name, fullname)
    }
  }

  def deleteFile(name:String){
    val method = createDeleteMethod(fileRequestUri + name)

    executeMethod(method, status => {
      status match{
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  override def addResource(meta:Map[String, String], data:DataStream):String = {
    addDataInternal(resourceRequestUri, meta, data)
  }

  def addFile(fullname:String, meta:Map[String, String], data:DataStream):String = {
    val relativeUrl = fileRequestUri + fullname

    addDataInternal(relativeUrl, meta, data)
  }

  private def addDataInternal(relativeUrl:String, meta:Map[String, String], data:DataStream):String = {

    val method = createPostMethod(relativeUrl)

    method.setRequestEntity(new MultipartRequestEntity(createPartsArray(meta, data), method.getParams))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_CREATED => {

          val xml = XML.load(method.getResponseBodyAsStream)

          val uploadedTags = (xml \ "uploaded")

          if (uploadedTags.size > 0){
            ((uploadedTags(0)) \ "@address").text
          }else{
            null
          }
        }
        case _ => handleError(status, method); null
      }
    })
  }

  def addDirectory(fullname:String){
    val method = createPostMethod(fileRequestUri + fullname)

    method.addRequestHeader(new Header("x-c3-nodetype", "directory"))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_CREATED => Unit
        case _ => handleError(status, method); null
      }
    })
  }

  override def search(query:String):List[SearchResultEntry] = {
    val method= createGetMethod(searchRequestUri + URLEncoder.encode(query, "UTF-8"))

    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => {
          SearchResultEntryParser.parse(XML.load(method.getResponseBodyAsStream))
        }
        case _ => handleError(status, method); List()
      }
    })

  }

  def updateResource(address:String, meta:Map[String, String], data:DataStream):Int = {

    val method = createPutMethod(resourceRequestUri + address)

    method.setRequestEntity(new MultipartRequestEntity(createPartsArray(meta, data), method.getParams))

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

  override def deleteResource(address:String) {
    val method = createDeleteMethod(resourceRequestUri + address)

    executeMethod(method, status => {
      status match{
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  override def getData(ra:String):C3ByteChannel = getDataInternal(ra, 0)

  def getDataInternal(address:String, version:Int):C3ByteChannel = {

    val relativeUrl = if(version > 0){
      resourceRequestUri + address + "/" + version
    }else{
      resourceRequestUri + address
    }

    val method = createGetMethod(relativeUrl)

    val status = httpClient.executeMethod(method)
    status match {
      case HttpStatus.SC_OK => new C3ByteChannelImpl(method)
      case _ =>
        try{
          method.releaseConnection()
        }catch{
          case e => //do nothing here
        }
        handleError(status, method); null
    }
  }
  
  def getDataAsStreamInternal(address:String, version:Int):C3InputStream = {
    val relativeUrl = if(version > 0){
      resourceRequestUri + address + "/" + version
    }else{
      resourceRequestUri + address
    }

    val method = createGetMethod(relativeUrl)

    val status = httpClient.executeMethod(method)
    status match {
      case HttpStatus.SC_OK => new C3InputStreamImpl(method)
      case _ =>
        try{
          method.releaseConnection()
        }catch{
          case e => //do nothing here
        }
        handleError(status, method); null
    }
  }

  def moveFile(path:String, newPath:String) {

    val method = createPutMethod(fileRequestUri + path)

    method.addRequestHeader(new Header("x-c3-op", "move"))
    method.setRequestEntity(new StringRequestEntity(newPath, "text/plain", "UTF-8"))


    executeMethod(method, status => {
      status match {
        case HttpStatus.SC_OK => Unit
        case _ => handleError(status, method)
      }
    })
  }

  protected def createPartsArray(meta:Map[String, String], data:DataStream):Array[Part] = {

    var parts:List[Part] = meta.map(e => {
      val part = new StringPart(e._1, e._2, "UTF-16")
      part.setCharSet("UTF-8")
      part
    }).toList

    if(data != null){
      parts = data.createFilePart :: parts
    }

    parts.toArray
  }

  protected def handleError(status:Int, method:HttpMethodBase){

    val contentType = method.getResponseHeader("Content-Type").getValue

    if(contentType.startsWith("application/xml")){
      val xml = XML.load(method.getResponseBodyAsStream)

      val errorTag = (xml \ "error")(0)

      val message = ((errorTag \ "message")(0)).text

      val exception = ((errorTag \ "exception")(0)).text

      if(exception.length() > 0){
        log.error("Failed to execute request, status " + status +", stacktrace:\n" + exception)
      }

      throw new C3AccessException(message, status)
    }else{

      val reader = new InputStreamReader(method.getResponseBodyAsStream, "UTF-8")

      val BUFFER_SIZE = 2048

      val buffer = CharBuffer.allocate(BUFFER_SIZE)

      reader.read(buffer)

      log.error("Failed to execute request to {}, server output: {}", method.getURI.toString, buffer.toString)

      reader.close()

      throw new C3AccessException(("Filed to execute method, http status is " + status).asInstanceOf[String], status)
    }
  }

  private def createGetMethod(relativeUrl:String, metadata:Boolean = false):HttpMethodBase = {
    val method =
      if(metadata)
        new GetMethod(host + relativeUrl + "?metadata")
      else
        new GetMethod(host + relativeUrl)

    addAuthHeaders(method, relativeUrl)

    method
  }

  private def createPostMethod(relativeUrl:String):PostMethod = {
    val method = new PostMethod(host + relativeUrl)
    addAuthHeaders(method, relativeUrl)
    method
  }

  private def createPutMethod(relativeUrl:String):PutMethod = {
    val method = new PutMethod(host + relativeUrl)
    addAuthHeaders(method, relativeUrl)
    method
  }

  private def createDeleteMethod(relativeUrl:String):HttpMethodBase = {

    val method = new DeleteMethod(host + relativeUrl)

    addAuthHeaders(method, relativeUrl)

    method
  }

  /**
   * @param relativeUrl url without hostname, i.e. /rest/resource/<address>
   */
  private def addAuthHeaders(method:HttpMethodBase, relativeUrl:String) {
    if(domain != "anonymous"){

      val dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z")

      val dateString = dateFormat.format(new Date())

      val hashBase = relativeUrl + dateString + domain

      val hash = hmac(key, hashBase)

      log.debug("Calculated hash {} for input parameters: {} ", hash,  Array(relativeUrl, domain, dateString))

      val header = new Header("x-c3-sign", hash)
      method.addRequestHeader(header)

      val domainHeader = new Header("x-c3-domain", domain)
      method.addRequestHeader(domainHeader)

      val dateHeader = new Header("x-c3-date", dateString)
      method.addRequestHeader(dateHeader)
    }
  }

  private def hmac(key:String, input:String):String = {

    val mac = Mac.getInstance("HmacSHA256")

    val secret = new SecretKeySpec(key.getBytes, "HmacSHA256")
    mac.init(secret)

    val digest = mac.doFinal(input.getBytes("UTF-8"));

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

  private def executeMethod[T](method:HttpMethodBase, f:Int => T):T = {

    try{
      val status = httpClient.executeMethod(method)
      f(status)
    }finally{
      method.releaseConnection()
    }
  }
}