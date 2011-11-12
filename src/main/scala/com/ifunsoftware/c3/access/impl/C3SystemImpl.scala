package com.ifunsoftware.c3.access.impl

import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpStatus, HttpClient, Header, HttpMethodBase}
import xml.XML
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.channels.ReadableByteChannel
import com.ifunsoftware.c3.access.{C3ByteChannel, C3AccessException, C3Resource, C3System}

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

    addAuthHeaders(getMethod, url + ra)

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
      getMethod.releaseConnection();
    }
  }

  override def addResource(resource:C3Resource):String = {
    null
  }

  override def updateResource(resource:C3Resource) {

  }

  override def deleteResource(ra:String) {

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
        new C3ByteChannel(getMethod)
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
  def addAuthHeaders(method:HttpMethodBase, relativeUrl:String){
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

  def hmac(key:String, input:String):String = {

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

    hexString.toString
  }
}