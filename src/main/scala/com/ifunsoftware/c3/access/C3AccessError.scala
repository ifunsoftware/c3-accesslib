package com.ifunsoftware.c3.access

import org.aphreet.c3.platform.exception.ResourceNotFoundException
import org.aphreet.c3.platform.filesystem.{FSWrongRequestException, FSNotFoundException}
import org.aphreet.c3.platform.auth.exception.AuthFailedException
import org.aphreet.c3.platform.accesscontrol.AccessControlException
import org.aphreet.c3.platform.domain.DomainException
import org.apache.commons.httpclient.HttpStatus
import org.aphreet.c3.platform.search.SearchQueryException

/**
 * Author: Mikhail Malygin
 * Date:   9/3/13
 * Time:   3:44 PM
 */
object C3AccessError{

  def handlingExceptions[T](a: => T):  T = {
    try{
      a
    }catch{
      case e: Throwable => handleException(e)
    }
  }

  def handleException[T](exception: Throwable): T = {
    exception match {
      case e: C3AccessException => throw e
      case e: ResourceNotFoundException => throw new C3NotFoundException(e.getMessage, e)
      case e: FSNotFoundException => throw new C3NotFoundException(e.getMessage, e)
      case e: AuthFailedException => throw new C3PermissionException(e.getMessage, e)
      case e: AccessControlException => throw new C3PermissionException(e.getMessage, e)
      case e: FSWrongRequestException => throw new C3IncorrectRequestException(e.getMessage, e)
      case e: SearchQueryException => throw new C3IncorrectRequestException(e.getMessage, e)
      case e: Throwable => throw new C3UnknownErrorException(e.getMessage, e)
    }
  }

}
