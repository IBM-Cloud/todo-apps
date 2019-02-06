## About
A backend for the ToDo app written written in Java.  ToDos can be stored in
either a Mongo DB or Couch DB using Cloudant.  


## Prerequisites

The project uses  Maven to manage dependencies, build, test, and deploy.
If you don't have Maven installed, you can do so by following this
[guide](http://maven.apache.org/download.cgi).

## Building
To build the project you just need to run Maven.  Change directories to the
bluemix-todo-app folder within the Java folder and run Maven.

    $ cd java/bluemix-todo-app
    $ mvn

## Running The App Locally
You can run the app locally for development purposes.  When you do run the app locally,
ToDos are stored in memory so when you restart the server the ToDos will be lost.  
(It does not require nor use a database locally to run.)  To run the app locally execute the
following Maven command in the root of the project.

    $ mvn -P run

Then open your favorite browser and navigate to http://localhost:8080.

## Swagger doc
You can access the swaggerdoc of the APIs at http://localhost:8080/api/swagger.json or http://localhost:8080/api/swagger.yaml

To use the swagger doc for swagger-codegen, change the `host` `your_host:your_port` and the `schemes` to point to your server

## Deploying The App To Bluemix
You can deploy to Bluemix using the cf command line interface, or the cloudfoundry maven plugin.
### Deploy using cf cli

    cf push YOUR-APP-NAME -p target/bluemix-todo-app.war -b java_buildpack

In a couple of minutes, your application should be deployed to YOUR-APP-NAME.mybluemix.net. However the ToDos will be stored in memory, and therefore will be lost if the application restarts, is updated or scaled. To create a cloudant database, bind it to your app and then restage it:

    cf create-service cloudantNoSQLDB Shared todo-couch-db
    cf bind-service YOUR-APP-NAME todo-couch-db
    cf restage YOUR-APP-NAME


### Deploy using maven
We use the [Cloud Foundry Maven Plugin](https://github.com/cloudfoundry/cf-java-client/tree/master/cloudfoundry-maven-plugin)
to deploy the app to Bluemix.  Before using Maven to deploy the app you just
need to do a small amount of configuration so Maven knows your Bluemix credentials.  To do this
open the settings.xml file in ~/.m2.  Within the servers element add the code below
inserting your credentials.

    <server>
      <id>Bluemix</id>
      <username>user@email.com</username>
      <password>bluemixpassword</password>
    </server>

If you do not see a settings.xml file in ~/.m2 than you will have to create one.
To do this just copy the settings.xml file from <maven install dir>/conf into ~/.m2.

Now that Maven knows your credentials you can deploy the app.  The app can be
deployed using either a Mongo DB backend or a backend based on Couch DB such as
[Cloudant](https://cloudant.com/).

#### Deploying The App With Mongo DB Backend

To do this you just need to run a Maven command specifying the URL and the
organization you want to use.

    mvn -P mongo-deploy -Dapp-url=bluemix-todo-java-mongo.mybluemix.net -Dorg=organization

The app-url parameter must be unique, so choose a URL that is meaningful to you.  The org
parameter represents the organization in Bluemix you want to deploy the app too.  By default
everyone has an organization name that is their user ID.  This command will deploy the app
to the space called dev by default, you can use the space parameter to deploy it to a different
space.  For example

    mvn -P mongo-deploy -Dapp-url=bluemix-todo-java-mongo.mybluemix.net -Dorg=organization -Dspace=myspace

#### Deploying The App With Cloudant Backend

To do this you just need to run a Maven command specifying the URL, organization, Cloudant
credentials, and Cloudant URL.

    $ mvn -P cloudant-deploy -Dapp-url=bluemix-todo-java-cloudant.mybluemix.net -Dorg=organization

Just like with the Mongo DB version you need to make sure the app-url is unique.  In addition
by default this command will use the space called dev.  You can specify a different space if
you would like using the space parameter.

    $ mvn -P cloudant-deploy -Dapp-url=bluemix-todo-java-cloudant.mybluemix.net -Dorg=organization -Dspace=myspace

### Deploying Application to IBM Kubernetes Service (IKS) using Docker
To deploy this application to IKS using docker, follow below steps:
#### Prerequisites
1. Install Kubernetes tool `kubectl` using : https://kubernetes.io/docs/tasks/tools/install-kubectl/

2. Install `Docker CE` using : https://hub.docker.com/search/?type=edition&offering=community

3. Create a `Docker ID` : https://hub.docker.com

4. Download `VirtualBox` : https://www.virtualbox.org/wiki/Downloads

5. (Optional) If you want to run locally, `minikube` for MacOS : https://kubernetes.io/docs/tasks/tools/install-minikube/

#### Getting Started and Configuration
1. Create a Kubernetes Cluster in Bluemix/IBM Cloud

    a). First let's create an account on Bluemix/IBM Cloud (For Free) : https://www.ibm.com/cloud/

    b). After login to your Bluemix/IBM Cloud account, click on the `Catalog` on top menu bar, select `Containers` from the left navigation menu and click on the Kubernetes Service and click on `create`.
    
    c). In the Create a Kubernates Cluster page, set the `Cluster type` as `Free`, enter your `Cluster Name`,  and select the location from the dropdown list (Or leave it for default, i used `Dallas`), then click on `create cluster`. Your will see your Kubernetes Cluster is now in `Deploying` status. When the `deployment` is done, you can see your Kubernetes cluster status is updated as `Deployed`(It takes 8-15 minutes).

2. Run `Kubernetes CLI`

    a). Download and install a few `CLI tools` and the `IBM Kubernetes Service` plug-in.
    
    `curl -sL https://ibm.biz/idt-installer | bash`
     
    b). TO gain access to your `cluster`, `Log in` to your `IBM Cloud` account.
     
    `ibmcloud login -a https://api.ng.bluemix.net`
            
      If you have a `federated ID`, use ibmcloud login `--sso` to log in to the IBM Cloud CLI
            
    c). Target the IBM Cloud Container Service `region` in which you want to work.

    `ibmcloud cs region-set us-south`

    d). Get the command to set the environment variable and download the Kubernetes configuration files.
    
    `ibmcloud cs cluster-config <your Bluemix/IBM cloud Kubernetes cluster name>`

    For example: `ibmcloud cs cluster-config mycluster1`

    e). Set the `KUBECONFIG` environment variable. Copy the output from the previous command and paste it in your terminal. The command output should look similar to the following.

    `export KUBECONFIG=/Users/$USER/.bluemix/plugins/container-service/clusters/mycluster1/kube-config-hou02-mycluster1.yml`

    f). Verify that you can connect to your `cluster` by listing your worker nodes.

    `kubectl get nodes`
       
3. Now, Assuming that you already build the project using `mvn`, `login` to the docker by using the command:

      `docker login`

4. Change directory to the bluemix-todo-app folder within the Java folder and run the following commands:

      `$ cd java/bluemix-todo-app`
        
      `docker build -t <DOCKER-IMAGE-NAME> .`
        
   It will create docker image named `<DOCKER-IMAGE-NAME>`. You can check it by command `docker images`.
   
##### Run the application locally using docker
1. To run the application locally using docker, run the following command:

    `docker run -it --rm -p 8088:8080 --name bluemix-todo-app <DOCKER-IMAGE-NAME>`
    
    here, docker port 8088 is mapped to the locally port 8080
    
2. check for the application at `http://localhost:8088/bluemix-todo-app/`

##### Deploy and run the application using IKS and docker
1. Login to IBM Cloud using commands :
    
    `bx login -a https://api.ng.bluemix.net --sso`
    
    `bx cr login`
    
    `bx cr namespace-add <NAMESPACE_NAME>`
    
    `bx cs region-set us-south`
    
2. In order to push the new image to IBM Cloud repository you need to `tag` the local images into an IBM specific format.To do this use the commands:

    `docker images`
    Notedown these `<DOCKER-IMAGE-ID>`, `<DOCKER-IMAGE-NAME>`, `<DOCKER-IMAGE-TAG>` to use them in next step.
    
    `docker tag <DOCKER-IMAGE-ID> registry.ng.bluemix.net/<NAMEPACE_NAME>/<DOCKER-IMAGE-NAME>:<DOCKER-IMAGE-TAG>`  
    
    `docker push registry.ng.bluemix.net/<NAMEPACE_NAME>/<DOCKER-IMAGE-NAME>`
    
3. Deploy containerized application to the Kubernetes cluster in Bluemix using Kubernetes, to do this use the following two kubectl commands:

    `kubectl run <YOUR_DEPLOYMENT-NAME> --image=registry.ng.bluemix.net/<NAMEPACE_NAME>/<DOCKER-IMAGE-NAME>:<DOCKER-IMAGE-TAG> --port=8080`
    
    `kubectl expose deployment <YOUR_DEPLOYMENT-NAME> --type=NodePort`
    
    You can see the deployed docker container with name `<YOUR_DEPLOYMENT-NAME>` is up and running in IKS dashboard.

4. To access the `bluemix-todo-app` application:
    
   a. In the IKS dashboard, For NodeIP, Go to `Nodes`(left-side bar) --> `details` --> `addresses`, notedown `Extenal IP` 
    
        For Example: 
        Under Addresses:
            InternalIP: 10.76.141.207
            ExternalIP: 184.172.233.230
            Hostname: 10.76.141.207
            Notedown, `ExternalIP` i.e, `184.172.233.230`
    
   b. For Node port, go to services --> Internal endpoints of "webserver", use second port number 
        
        For Example:
        Under services -->  <YOUR_DEPLOYMENT-NAME>
            <YOUR_DEPLOYMENT-NAME>:8080 TCP
            <YOUR_DEPLOYMENT-NAME>:30154 TCP
            Notedown, second IP i.e, 30154
    
   c. Finally, access application using `184.172.233.230:30154/bluemix-todo-app/` i.e, `<Node_External_IP>`:`<Node_Port>/bluemix-todo-app/`
    
    

### Additional Information

This project is leveraging the [spring-cloud library](https://github.com/spring-projects/spring-cloud)
to help retrieve service information from the VCAP_SERVICES environment variable.  See the ToDoStoreFactory
class for an example of how you can use this library to more easily use the information within VCAP_SERVICES.
