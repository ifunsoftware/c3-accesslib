package com.ifunsoftware.c3.access.impl

import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.httpclient.{HttpStatus, HttpClient, Header, HttpMethodBase}
import xml.XML
import org.slf4j.LoggerFactory
import com.ifunsoftware.c3.access._
import org.apache.commons.httpclient.methods.multipart.{MultipartRequestEntity, StringPart, Part}
import org.apache.commons.httpclient.methods.{DeleteMethod, PostMethod, PutMethod, GetMethod}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3SystemImpl(val host:String,  val domain:String,  val key:String) extends C3System{

  val log = LoggerFactory.getLogger(getClass)

  val requestUri = "/rest/resource/"

  val url = host + requestUri

  val httpClient = new HttpClient

  override def getData(ra:String):C3ByteChannel = {
    getDataInternal(ra, 0)
  }

  override def getResource(ra:String):C3Resource = {
    val getMethod = new GetMethod(url + ra + "?metadata")

    addAuthHeaders(getMethod, requestUri + ra)

    try{
      val status = httpClient.executeMethod(getMethod)
      status match {
        case HttpStatus.SC_OK => {

          val xml = XML.load(getMethod.getResponseBodyAsStream)

          new C3ResourceImpl(this, xml)
        }
        case _ =>
          if(log.isDebugEnabled){
            log.debug("Response is not ok: {}", getMethod.getResponseBodyAsString(1024))
          }
          throw new C3AccessException("Failed to get resource metadata, error code is " + status )
      }
    }finally{
      getMethod.releaseConnection()
    }
  }

  override def addResource(meta:Map[String, String], data:DataStream):String = {

    val method = new PostMethod(url)

    addAuthHeaders(method, requestUri)

    method.setRequestEntity(new MultipartRequestEntity(createPartsArray(meta, data), method.getParams))

    try{
      val status = httpClient.executeMethod(method)
      status match {
        case HttpStatus.SC_CREATED => {

          val xml = XML.load(method.getResponseBodyAsStream)

          ((xml \\ "uploaded")(0) \ "@address" text)
        }
        case _ =>
          if(log.isDebugEnabled){
            log.debug("Response is not ok: {}", method.getResponseBodyAsString(1024))
          }
          throw new C3AccessException(("Filed to post resource, code " + status).asInstanceOf[String])
      }
    }finally {
      method.releaseConnection()
    }
  }

  def updateResource(address:String, meta:Map[String, String], data:DataStream):Int = {

    val putMethod = new PutMethod(url + address)

    addAuthHeaders(putMethod, requestUri + address)

    val parts = createPartsArray(meta, data)

    putMethod.setRequestEntity(new MultipartRequestEntity(parts, putMethod.getParams))

    try{
      val status = httpClient.executeMethod(putMethod)
      status match {
        case HttpStatus.SC_OK => {

          val xml = XML.load(putMethod.getResponseBodyAsStream)

          ((xml \\ "uploaded")(0) \ "@version" text).toInt
        }
        case _ =>
          if(log.isDebugEnabled){
            log.debug("Response is not ok: {}", putMethod.getResponseBodyAsString(1024))
          }
          throw new C3AccessException(("Filed to post resource, code " + status).asInstanceOf[String])
      }
    }finally {
      putMethod.releaseConnection()
    }

  }

  override def deleteResource(address:String) {
    val deleteMethod = new DeleteMethod(url + address)

    addAuthHeaders(deleteMethod, requestUri + address)

    try{
      val status = httpClient.executeMethod(deleteMethod)
      status match{
        case HttpStatus.SC_OK => null
        case _ =>
          if(log.isDebugEnabled){
            log.debug("Response is not ok: {}", putMethod.getResponseBodyAsString(1024))
          }
          throw new C3AccessException(("Failed to delete resource, code " + status).asInstanceOf[String])
      }
    }
  }

  def getDataInternal(address:String, version:Int):C3ByteChannel = {


    val relativeUrl = if(version > 0){
      url + address + "/" + version
    }else{
      url + address
    }

    val getMethod = new GetMethod(relativeUrl)

    addAuthHeaders(getMethod, relativeUrl)

    val status = httpClient.executeMethod(getMethod)
    status match {
      case HttpStatus.SC_OK => {
        new C3ByteChannelImpl(getMethod)
      }
      case _ =>
        try{
          getMethod.releaseConnection()
        }catch{
          case e => //do nothing here
        }
        throw new C3AccessException("Failed to get resource metadata, error code is " + status )
    }
  }

  /**
   * @param relativeUrl url without hostname, i.e. /rest/resource/<address>
   */
  protected def addAuthHeaders(method:HttpMethodBase, relativeUrl:String){
    if(domain != "anonymous"){

      val dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z")

      val dateString = dateFormat.format(new Date())

      val hashBase = relativeUrl + dateString + domain

      val hash = hmac(key, hashBase)

      log.debug("Calculated hash {} for input parameters url:{} domain:{} date:{} ", Array(hash, relativeUrl, domain, dateString))

      val header = new Header("x-c3-sign", hash)
      method.addRequestHeader(header)

      val domainHeader = new Header("x-c3-domain", domain)
      method.addRequestHeader(domainHeader)

      val dateHeader = new Header("x-c3-date", dateString)
      method.addRequestHeader(dateHeader)
    }
  }

  protected def hmac(key:String, input:String):String = {

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
}