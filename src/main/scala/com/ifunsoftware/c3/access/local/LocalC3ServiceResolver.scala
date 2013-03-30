package com.ifunsoftware.c3.access.local

import org.osgi.framework.BundleContext

trait C3ServiceLocator {

  def resolveService[T](clazz: Class[T]): T

}

class OSGiServiceLocator(val bundleContext: AnyRef) extends C3ServiceLocator{

  def resolveService[T](clazz: Class[T]): T = {

    LocalC3System.log.info("Resolving service {}", clazz.getCanonicalName)

    val context = bundleContext.asInstanceOf[BundleContext]

    val reference = context.getServiceReference(clazz.getCanonicalName)

    context.getService(reference).asInstanceOf[T]
  }
}