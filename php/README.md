## About
A backend for the ToDo app written using PHP.  ToDos can be stored in either 
a Mongo DB or a Couch DB using Cloudant.

## Prerequisites
Before running this app or deploying it to BlueMix you need to have 
[PHP](http://us1.php.net/downloads.php), and the 
[CloudFoundry Command Line](https://github.com/cloudfoundry/cli) installed.

## Deploying To BlueMix

The bash script included in the project takes care of everything you need to do 
to deploy to BlueMix, including creating and binding to the services.  Before you 
deploy the app please make sure you have all the prerequisites from the section above 
installed and working.

### Login To BlueMix From The Command Line
Before deploying to BlueMix you need to make sure you are logged in.  To do this run

    cf login -a https://api.ng.bluemix.net

### Deploying To BlueMix

There are two tasks that will deploy this app to BlueMix, one uses a Mongo DB 
service and the other uses a Cloudant service.

To deploy the app so it uses Mongo DB run

    $ ./deploy.sh mongo

During the deploy you will be prompted to enter an application name.  This name must be unique, if
not the deployment will fail.  If it fails run the bash script again choosing a different name.

To deploy the app so it uses Cloudant run

    $ ./deploy.sh cloudant


## Additional Information

This app currently uses the PHP buildpack from [CloudFoundry](https://github.com/dmikusa-pivotal/cf-php-build-pack).
By default it is run unsing an Apache server however you can also use NGINX.  To do this open the 
options.json file in the .bp-config directory and change the WEB_SERVER property from httpd to nginx.

You may try other PHP buildpacks but the app has only been tested using the CloudFoundry buildpack 
so it may not work.