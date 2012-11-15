package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.{DataStream, C3Resource}
import org.aphreet.c3.platform.resource.{Resource, ResourceVersion}
import org.aphreet.c3.platform.accesscontrol.UPDATE

class LocalC3Resource(val system: LocalC3System, val resource: ResourceContainer) extends C3Resource with DataConverter{

  def this(system: LocalC3System, address: String) = this(system, new LazyResourceContainer(system, address))

  def this(system: LocalC3System, resource: Resource) = this(system, new LoadedResourceContainer(resource))

  def address = resource.address

  def date = resource.createDate

  def tracksVersions = resource.isVersioned

  def metadata = resource.metadata.toMap

  def systemMetadata = resource.systemMetadata.toMap

  def versions = resource.versions.map(new LocalC3Version(_)).toList

  def update(meta: Map[String, String], data: DataStream) {

    system.retrieveAccessTokens(UPDATE).checkAccess(resource)

    if (data != null){
      resource.addVersion(ResourceVersion(data))
    }

    resource.metadata ++= meta

    system.update(resource)
  }

  def update(meta: Map[String, String]) {
    update(meta, null)
  }

  def update(data: DataStream) {
    update(Map(), data)
  }

  implicit def convertContainerToResource(container: ResourceContainer): Resource = container.resource
}

trait ResourceContainer{

  def resource:Resource

}

class LazyResourceContainer(system: LocalC3System, address: String) extends ResourceContainer{
  lazy val resource = system.fetchResource(address)
}

class LoadedResourceContainer(val resource: Resource) extends ResourceContainer
