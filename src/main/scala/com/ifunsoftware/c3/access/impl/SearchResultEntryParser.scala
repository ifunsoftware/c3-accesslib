package com.ifunsoftware.c3.access.impl

import xml.NodeSeq
import com.ifunsoftware.c3.access.{SearchResultFragment, SearchResultEntry}
import org.slf4j.LoggerFactory

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

object SearchResultEntryParser {

  val log = LoggerFactory.getLogger(getClass)

  def parse(xml: NodeSeq): List[SearchResultEntry] = {
    if(log.isDebugEnabled){
      log.debug("Got response for parsed search query: '{}'", (xml \\ "query")(0).text)
    }

    val results = (xml \ "searchResults")(0)

    (results \ "entry").map(parseEntry(_)).toList
  }

  private def parseEntry(entry: NodeSeq): SearchResultEntry = {
    val address = (entry \ "@address").text
    val score = (entry \ "@score").text.toFloat
    val path = (entry \ "@path").text

    val fragments = (entry \\ "fragment").map(parseFragment(_)).toList

    SearchResultEntry(address, path, score, fragments)
  }

  private def parseFragment(fragment: NodeSeq): SearchResultFragment = {
    val name = (fragment \ "@field").text

    val strings = (fragment \\ "string").map(e => e.text).toList

    SearchResultFragment(name, strings)
  }
}
