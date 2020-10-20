package io.monster.ecomm.account.test.util

import java.net.URL

import io.fabric8.kubernetes.api.model.{ ConfigMapBuilder, DeletionPropagation }
import io.fabric8.kubernetes.client.DefaultKubernetesClient

import scala.io.{ BufferedSource, Source }

object K8sHelper {
  def getEndpoint(client: DefaultKubernetesClient, serviceName: String, portName: String): (String, Int) = {
    val url = new URL(
      client
        .services()
        .inNamespace("default")
        .withName(serviceName)
        .getURL(portName)
        .replaceFirst("tcp", "http")
    )
    (url.getHost, url.getPort)
  }

  def startTestEnv(
    k8sResourceFile: String,
    maybeClient: Option[DefaultKubernetesClient] = None
  ): DefaultKubernetesClient = {
    val k8sClient = maybeClient.getOrElse(new DefaultKubernetesClient())
    val resources = k8sClient.load(this.getClass.getResourceAsStream(k8sResourceFile)).get()
    k8sClient.resourceList(resources).inNamespace("default").createOrReplace()
    k8sClient
  }

  def createConfigMap(configName: String, sourceFile: String): DefaultKubernetesClient = {
    val k8sClient = new DefaultKubernetesClient()
    lazy val file: BufferedSource = Source.fromResource(sourceFile)
    val fileContent =
      try file.getLines().mkString
      finally file.close
    val configMapResource = k8sClient.configMaps.inNamespace("default").withName(configName)
    configMapResource.createOrReplace(
      new ConfigMapBuilder()
        .withNewMetadata()
        .withName(configName)
        .endMetadata()
        .addToData("init.sql", fileContent)
        .build()
    )
    k8sClient
  }

  def deleteEnv(
    k8sClient: DefaultKubernetesClient,
    service: String,
    maybeConfigMap: Option[String] = None
  ): DefaultKubernetesClient = {
    k8sClient
      .apps()
      .deployments()
      .inNamespace("default")
      .withName(service)
      .withPropagationPolicy(DeletionPropagation.FOREGROUND)
      .delete()

    k8sClient
      .services()
      .inNamespace("default")
      .withName(service)
      .withPropagationPolicy(DeletionPropagation.FOREGROUND)
      .delete()

    maybeConfigMap.foreach(
      k8sClient
        .configMaps()
        .inNamespace("default")
        .withName(_)
        .withPropagationPolicy(DeletionPropagation.FOREGROUND)
        .delete()
    )
    k8sClient
  }
}
