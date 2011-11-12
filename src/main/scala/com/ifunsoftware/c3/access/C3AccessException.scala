package com.ifunsoftware.c3.access

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3AccessException(val message:String, val cause:Throwable) extends RuntimeException(message, cause){

  def this(message:String) = this(message, null)

}