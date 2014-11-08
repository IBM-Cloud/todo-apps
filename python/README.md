## About
A backend for the ToDo app written in Python.  ToDos can be stored in 
either a Mongo DB or Couch DB using Cloudant.

## Prerequisites
Before running this app or deploying it to BlueMix you need to have [
Python](https://www.python.org/), [PIP](http://www.pip-installer.org/), [Paver](http://paver.github.io/paver/), 
and the [CloudFoundry Command Line](https://github.com/cloudfoundry/cli) 
installed.  If you are a Python developer you probably already have most of these installed.

## Deploying To BlueMix

The Paver file takes care of everything you need to do to deploy to BlueMix, including 
creating and binding to the services.  Before you deploy the app please make sure you 
have all the prerequisites from the section above installed and working, they are used by 
the Paver tasks.

### Login To BlueMix From The Command Line

The Paver file contains a task that will do this for you, just run

    $ paver cf_login

You will be prompted to enter your BlueMix user name and password.

You can also do this by using the Cloud Foundry command line tool directly.

    $ cf login -a https://api.ng.bluemix.net

### Deploying To BlueMix

There are two tasks that will deploy this app to BlueMix, one uses a Mongo DB service 
and the other uses a Cloudant service.

To deploy the app so it uses Mongo DB run

    $ paver deploy_mongo_todo

This tasks will prompt you for a name to use for your application.  The name must be unique, if it is
not unique the deploy will fail.  If it does fail run the task again and enter a different name.

To deploy the app so it uses Cloudant run

    $ paver deploy_cloudant_todo


### Additional Information

This app has been tested using the Python buildpack [here](https://github.com/joshuamckenty/heroku-buildpack-python).
There are other Python buildpacks that you may use but this app has not been tested with them
so use them at your own risk.  This buildpack requires you have a file called requirements.txt in
the root of your project listing all the dependencies that need to be installed.  See the requiremnts.txt
file in the root of this project for an example.
