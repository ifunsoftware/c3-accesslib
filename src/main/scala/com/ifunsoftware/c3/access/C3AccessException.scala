package com.ifunsoftware.c3.access

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3AccessException(val message:String, val cause:Throwable, val code:Int) extends RuntimeException(message, cause){

  def this(message:String, e:Throwable) = this(message, e, 0)

  def this(message:String) = this(message, null, 0)

  def this (message:String, code:Int) = this(message, null, code)
}