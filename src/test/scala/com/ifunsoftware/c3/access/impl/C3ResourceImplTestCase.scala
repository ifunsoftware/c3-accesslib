package com.ifunsoftware.c3.access.impl

import xml.XML
import java.io.StringReader
import org.junit.Test
import org.junit.Assert._

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3ResourceImplTestCase {

  @Test
  def testResourceParse() {

    val xml = XML.load(new StringReader("""<?xml version="1.0" encoding="UTF-8"?>
<p:response xmlns:p="http://c3.aphreet.org/rest/1.0" xsi:schemaLocation="http://c3.aphreet.org/rest/1.0 http://c3-system.googlecode.com/files/rest.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <info version="1.0" status="OK"/>
  <resource address="be7300eb-c518-40b1-a384-a4dc49359844-f579" createDate="2011-02-12T02:03:06.208+03:00" trackVersions="true">
    <metadata>
      <element key="content.type">
        <value>application/octet-stream</value>
      </element>
      <element key="c3.pool">
        <value>default</value>
      </element>
    </metadata>
    <systemMetadata>
      <element key="indexed">
        <value>1297465386274</value>
      </element>
      <element key="content.type">
        <value>application/octet-stream</value>
      </element>
      <element key="c3.pool">
        <value>default</value>
      </element>
      <element key="c3.user">
        <value>anonymous</value>
      </element>
    </systemMetadata>
    <versions>
      <version date="2011-02-12T02:03:06.209+03:00" length="102400" hash="57afa880c698760302978bb117c0b96b"/>
    </versions>
  </resource>
</p:response>"""))

    val resource = new C3ResourceImpl(null, "be7300eb-c518-40b1-a384-a4dc49359844-f579", xml)

    assertEquals("be7300eb-c518-40b1-a384-a4dc49359844-f579", resource.address)

    assertEquals(1297465386208l, resource.date.getTime)

    assertEquals(true, resource.tracksVersions)

    assertEquals(
      Map("content.type" -> "application/octet-stream",
        "c3.pool" -> "default"),
      resource.metadata)

    assertEquals(
      Map("indexed" -> "1297465386274",
        "content.type" -> "application/octet-stream",
        "c3.pool" -> "default",
        "c3.user" -> "anonymous"),
      resource.systemMetadata
    )

    val version = resource.versions.head

    assertEquals(1297465386209l, version.date.getTime)

    assertEquals(version.length, 102400)
    assertEquals(version.hash, "57afa880c698760302978bb117c0b96b")

    assertEquals(1, version.asInstanceOf[C3VersionImpl]._number)
  }
}