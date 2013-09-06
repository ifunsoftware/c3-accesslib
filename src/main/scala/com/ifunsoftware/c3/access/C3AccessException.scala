package com.ifunsoftware.c3.access

/**
 * Copyright iFunSoftware 2011
 * @author Mikhail Malygin
 */

class C3AccessException(val message: String, val cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
}

class C3NotFoundException(override val message: String, override val cause: Throwable) extends C3AccessException(message, cause){

  def this(message: String) = this(message, null)

}

class C3PermissionException(override val message: String, override val cause: Throwable) extends C3AccessException(message, cause){

  def this(message: String) = this(message, null)

}

class C3IncorrectRequestException(override val message: String, override val cause: Throwable) extends C3AccessException(message, cause){

  def this(message: String) = this(message, null)

}

class C3UnknownErrorException(override val message: String, override val cause: Throwable, val code: Int) extends C3AccessException(message, cause){

  def this(message: String) = this(message, null, 0)

  def this(message: String, code: Int) = this(message, null, code)

  def this(message: String, throwable: Throwable) = this(message, throwable, 0)

}
