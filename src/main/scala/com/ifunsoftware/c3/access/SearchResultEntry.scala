package com.ifunsoftware.c3.access

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

case class SearchResultEntry(address: String,
                             path: String,
                             score: Float,
                             fragments: List[SearchResultFragment])

case class SearchResultFragment(name: String,
                                strings: List[String])
