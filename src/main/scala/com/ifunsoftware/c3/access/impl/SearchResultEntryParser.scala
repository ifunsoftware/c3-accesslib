package com.ifunsoftware.c3.access.impl

import xml.NodeSeq
import com.ifunsoftware.c3.access.{SearchResultFragment, SearchResultEntry}
import collection.mutable.ArrayBuffer

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

object SearchResultEntryParser {

  def parse(xml: NodeSeq): List[SearchResultEntry] = {
    (xml \\ "entry").map(parseEntry(_)).toList
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
