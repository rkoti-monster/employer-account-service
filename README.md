# Employer Account Service
Employer account service aims to provide a single interface to maintaining e-commerce account information of our employers. 
It provides a restful interface to get, add, delete, update accounts. It also interacts with Zuora and pushes account information to it so that it can be synched to other backend systems like Salesforce etc.

## Build
- `sbt compile` ~ local build
- `sbt docker:publishLocal` - build an image of this service and host it locally (This requires a local docker instance)
 
## Run Locally
- `docker-compose up` 
   - Launches the account service and mysql in docker containers. You can access http://localhost:8083/accounts to verify its up and running. Assumes that the docker images for the account serivce is available in the local docker instance.
   
- `docker-compose -f mysql-service.yml up` 
   - Launches only the backend mysql instance that can be used by the local account service launched using sbt or from within IntelliJ

- `sbt run`
   - Launches a local account service instance on port 8083. This assumes a MySQL DB is running on the local machine

- `kubectl create configmap mysql-initdb-config --from-file=src/it/resources/init.sql`;`kubectl apply -f src/it/resources/k8s-resources-mysql.yml`;`kubectl apply -f src/it/resources/k8s-resources-eas.yml`;
   - Launches a cluster with account service and mysql instance. To access this instance, you need to know the external IP of the Kubernetes cluster which in the case of minikube can be found by the command `minikube ip` and the port to be used will be the NodePort configured for the service which is `30100`.

## Test
- `sbt test`
   - Runs unit tests
   
- `sbt it:test`
    - Runs the integration tests using Minikube kubernetes cluster. It assumes you already launched minikube cluster with 
       `minikube start --driver=hyperkit`
    - Make sure the images are available in the docker instance of minikube. This can be done using
       `eval $(minikube docker-env)`
       `sbt docker:publishLocal`
## Linting
- `sbt ci:compile`

## Formatting
- `sbt scalafmtSbtCheck scalafmtCheckAll` 
    - To check the format.  
    - If it reports errors, then run `sbt scalafmtSbt scalafmtAll` to fix the issues
            
## TODO
- CI pipeline integration
- AWS RDS Integration
- Remove duplication of start-up SQL for mysql instance in file and config-map