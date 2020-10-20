package io.monster.ecomm.account.test.util

object Constants {
  val mysqlServiceName = "mysql"
  val mysqlPortName = "mysql-service-port"
  val mysqlInitConfigMapName = "mysql-initdb-config"
  val mysqlInitFileName = "init.sql"
  val mysqlResourceFileName = "/k8s-resources-mysql.yml"
  val easResourceFileName = "/k8s-resources-eas.yml"
  val easServiceName = "employer-account-service"
  val easPortName = "employer-account-service-port"
}
