## About
A backend for the ToDo app written using Ruby and Sinatra.  ToDos can be stored 
in either a Mongo DB or a Couch DB using Cloudant.

## Prerequisites
Before running this app or deploying it to BlueMix you need to have 
[Ruby](https://www.ruby-lang.org), [Gem](http://rubygems.org/), [Sinatra](http://www.sinatrarb.com/), 
[Bundler](http://bundler.io/), and the [CloudFoundry Command Line](http://www.ng.bluemix.net/docs/BuildingWeb.jsp#install-cf) 
installed.  If you are a Ruby developer you probably already have most of these installed.

## Deploying To BlueMix

The Rake file takes care of everything you need to do to deploy to BlueMix, including 
creating and binding to the services.  Before you deploy the app please make sure you have 
all the prerequisites from the section above installed and working, they are used by the 
Rake tasks.

### Login To BlueMix From The Command Line

The Rake file contains a task that will do this for you, just run

    $ rake cfLogin

You will be prompted to enter your BlueMix user name and password.

You can also login using the Cloud Foundry command line tool directly.

    $ cf login -a https://api.ng.bluemix.net

### Deploying To BlueMix

There are two tasks that will deploy this app to BlueMix, one uses a Mongo DB service and 
the other uses a Cloudant service.

To deploy the app so it uses Mongo DB run

    $ rake deployMongoToDo

During the deploy you will be prompted to enter a name for the application.  This name must be unique.
If it is not unique the deploy will fail.  If it fails just run the task again using a different name.

To deploy the app so it uses Cloudant run

    $ rake deployCloudantToDo

Before deploying the Cloudant version make sure you have followed the instructions in the "Couch DB
and Cloudant" section in the top-level [README](../README.md).  When deploying the app so it uses 
Cloudant you will be prompted to enter your Cloudant username and password as well as your Cloudant URL, 
which usually takes the form https://username.cloudant.com.  If you do not have a Cloudant account you 
can sign up for one free at [https://cloudant.com/](https://cloudant.com/).

### Additional Information

BlueMix requires that your Sinatra project have two files present in the project root, Gemfile
and config.ru.  Gemfile lists all the Gems your project depends on and that need to be installed
when the project is deployed to BlueMix.  The config.ru file tells BlueMix how to start your 
app.  This project has two different config.ru files located in the deploy folder, one for 
Couch DB / Cloudant and the other for Mongo DB.  These files are copied and renamed to the root
of the project when the rake tasks are run.  For more information on these requirements see
the [Cloud Foundry documentation](http://docs.cloudfoundry.org/devguide/deploy-apps/ruby-tips.html).