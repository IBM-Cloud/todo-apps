## About
A backend for the ToDo app written using Ruby and Sinatra.  ToDos can be stored 
in either a Mongo DB or a Couch DB using Cloudant.

## Prerequisites
Before running this app or deploying it to Bluemix you need to have 
[Ruby](https://www.ruby-lang.org), [Gem](http://rubygems.org/), [Sinatra](http://www.sinatrarb.com/), 
[Bundler](http://bundler.io/), and the [CloudFoundry Command Line](https://github.com/cloudfoundry/cli) 
installed.  If you are a Ruby developer you probably already have most of these installed.

## Deploying To Bluemix

The Rake file takes care of everything you need to do to deploy to Bluemix, including 
creating and binding to the services.  Before you deploy the app please make sure you have 
all the prerequisites from the section above installed and working, they are used by the 
Rake tasks.

### Login To Bluemix From The Command Line

The Rake file contains a task that will do this for you, just run

    $ rake cfLogin

You will be prompted to enter your Bluemix user name and password.

You can also login using the Cloud Foundry command line tool directly.

    $ cf login -a https://api.ng.bluemix.net

### Deploying To Bluemix

There are two tasks that will deploy this app to Bluemix, one uses a Mongo DB service and 
the other uses a Cloudant service.

To deploy the app so it uses Mongo DB run

    $ rake deployMongoToDo

During the deploy you will be prompted to enter a name for the application.  This name must be unique.
If it is not unique the deploy will fail.  If it fails just run the task again using a different name.

To deploy the app so it uses Cloudant run

    $ rake deployCloudantToDo


### Additional Information

Bluemix requires that your Sinatra project have two files present in the project root, Gemfile
and config.ru.  Gemfile lists all the Gems your project depends on and that need to be installed
when the project is deployed to Bluemix.  The config.ru file tells Bluemix how to start your 
app.  This project has two different config.ru files located in the deploy folder, one for 
Couch DB / Cloudant and the other for Mongo DB.  These files are copied and renamed to the root
of the project when the rake tasks are run.  For more information on these requirements see
the [Cloud Foundry documentation](http://docs.cloudfoundry.org/devguide/deploy-apps/ruby-tips.html).