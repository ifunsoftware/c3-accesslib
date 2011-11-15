package com.ifunsoftware.c3.access.impl.fs

import org.junit.Test
import org.junit.Assert._
import xml.XML
import com.ifunsoftware.c3.access.fs.impl.C3DirectoryImpl
import com.ifunsoftware.c3.access.fs.{C3Directory, C3File}

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */
class C3DirectoryImplTestCase {

  @Test
  def testParseDescription(){

    val xmlString = """<?xml version="1.0" encoding="UTF-8"?>
<p:response xmlns:p="http://c3.aphreet.org/rest/1.0" xsi:schemaLocation="http://c3.aphreet.org/rest/1.0 http://c3-system.googlecode.com/files/rest.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <info version="1.0" status="OK"/>
  <directory name="anonymous" address="287f53c2-59c7-4ad5-a228-0abe49359844-f579">
    <nodes>
      <node name="zzz.log" address="69b08bc0-b6f1-40a4-9c6a-a2e549359844-f579" leaf="true"/>
      <node name="directory" address="ae3ecc11-5cd6-4d82-b5ab-120d49359844-73f2" leaf="false"/>
      <node name="directory_3" address="8a777416-9f7a-49ae-8278-4e9449359844-f579" leaf="false"/>
      <node name="java.log" address="69b08bc0-b6f1-40a4-9c6a-a2e549359844-f579" leaf="true"/>
      <node name="test" address="d9cc0adc-e527-4630-8118-735849359844-f579" leaf="false"/>
      <node name="directory_2" address="3b05cd08-8a58-4a8c-8876-865c49359844-f579" leaf="false"/>
    </nodes>
  </directory>
</p:response>"""

    val xml = XML.loadString(xmlString)

    val directory = new C3DirectoryImpl(null, "287f53c2-59c7-4ad5-a228-0abe49359844-f579", null, "anonymous", "/anonymous", xml)

    assertEquals(List("directory", "directory_2", "directory_3", "test", "java.log", "zzz.log"), directory.children.map(_.name).toList)

    assertEquals(List("ae3ecc11-5cd6-4d82-b5ab-120d49359844-73f2",
      "3b05cd08-8a58-4a8c-8876-865c49359844-f579",
      "8a777416-9f7a-49ae-8278-4e9449359844-f579",
      "d9cc0adc-e527-4630-8118-735849359844-f579",
      "69b08bc0-b6f1-40a4-9c6a-a2e549359844-f579",
      "69b08bc0-b6f1-40a4-9c6a-a2e549359844-f579"), directory.children.map(_.address).toList)

    assertEquals(List("/anonymous/directory",
      "/anonymous/directory_2",
      "/anonymous/directory_3",
      "/anonymous/test",
      "/anonymous/java.log",
      "/anonymous/zzz.log"), directory.children.map(_.fullname).toList)

    assertEquals(List("java.log", "zzz.log"), directory.children.filter(_.isInstanceOf[C3File]).map(_.name))

    assertEquals(List("directory", "directory_2", "directory_3", "test"),
      directory.children.filter(_.isInstanceOf[C3Directory]).map(_.name))
  }
}