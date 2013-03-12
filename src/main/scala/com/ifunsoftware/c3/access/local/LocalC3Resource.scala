package com.ifunsoftware.c3.access.local

import com.ifunsoftware.c3.access.{MetadataKeep, MetadataChange, DataStream, C3Resource}
import org.aphreet.c3.platform.resource.{Resource, ResourceVersion}
import org.aphreet.c3.platform.accesscontrol.UPDATE

class LocalC3Resource(val system: LocalC3System, val resource: ResourceContainer) extends C3Resource with DataConverter {

  def this(system: LocalC3System, address: String) = this(system, new LazyResourceContainer(system, address))

  def this(system: LocalC3System, resource: Resource) = this(system, new LoadedResourceContainer(resource))

  def address = resource.address

  def date = resource.createDate

  def tracksVersions = resource.isVersioned

  def metadata = resource.metadata.asMap.toMap

  def systemMetadata = resource.systemMetadata.asMap.toMap

  def versions = resource.versions.map(new LocalC3Version(_)).toList

  protected def updateInternal(meta: MetadataChange, data: Option[DataStream]) {
    system.retrieveAccessTokens(UPDATE).checkAccess(resource)

    data.foreach(stream => resource.addVersion(ResourceVersion(stream)))

    resource.metadata ++= meta.updated

    meta.removed.foreach(resource.metadata.remove(_))

    system.update(resource)
  }

  def update(meta: MetadataChange, data: DataStream) {
    updateInternal(meta, Some(data))
  }

  def update(meta: MetadataChange) {
    updateInternal(meta, None)
  }

  def update(data: DataStream) {
    updateInternal(MetadataKeep, Some(data))
  }

  implicit def convertContainerToResource(container: ResourceContainer): Resource = container.resource
}

trait ResourceContainer {

  def resource: Resource

}

class LazyResourceContainer(system: LocalC3System, address: String) extends ResourceContainer {
  lazy val resource = system.fetchResource(address)
}

class LoadedResourceContainer(val resource: Resource) extends ResourceContainer
