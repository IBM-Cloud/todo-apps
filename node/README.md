# bluemix-todo-apps - node overview

TodoMVC using Backbone, CouchDB/MongoDB and a node back-end, running on Bluemix.

Refer to the README.md file in the parent directory
(eg, `bluemix-todo-apps/README.md`) for general instructions regarding
this application and the database service it requires.

This application supports both Mongo DB and Couch DB ([Cloudant][cloudant_url]) as a backend.

## How it Works

1. Add items to the todo list by typing into the box and pressing `Enter`

2. Mark items as complete by clicking the checkmark to the left of the corresponding item.

3. Delete items by clicking the 'X' to the right of the corresponding item that appears on hover.

4. Edit existing todo items by double-clicking on the corresponding item.

## Running the app on Bluemix

1. Create a Bluemix Account

    [Sign up][bluemix_signup_url] for Bluemix, or use an existing account.

2. Download and install the [Cloud-foundry CLI][cloud_foundry_url] tool

3. If you have not already, [download node.js][download_node_url] and install it on your local machine.

4. To deploy the app, you will need the jbuild tool installed.

	To install `jbuild` on Windows, use the command

    	npm -g install jbuild

	To install `jbuild` on Mac or Linux, use the command

    	sudo npm -g install jbuild

5. Clone the app to your local environment from your terminal using the following command

  ```
  git clone https://github.com/IBM-Bluemix/todo-apps.git
  ```

6. `cd` into the node folder of this newly created directory

7. Now its time to decide what database service you would like to use as your datastore. I will go through them one-by-one here:
	#### Cloudant
	Cloudant is IBM's NoSQL CouchDB solution. The following command will create a Cloudant service called todo-couch-db, bind it to your app, and start your app:
	
		$ jbuild deploycloudant appName
		
	#### Compose.io Mongo
	Compose.io is IBM's providor of a number of fully-managed, open source DB solutions. The following steps walks you through creating a Compose Mongo instance:
	
	* Create a [Compose.io account][compose_signup_url] for a 30-day free trial and select MongoDB as your database
	* Once your Mongo deployment has been provisioned, create a DB called `todoDB`
	* Navigate to the Users tab of your new DB and select Add User. Fill out your own username and password.
	* Navigate to the Admin tab and note the host and port number of your primary replica set.
	* Go to the Bluemix catalog, select the 'MongoDB by Compose' service, and do the following:
		* Leave the app unbound
		* Name the service `todo-compose-mongo-db`
		* Fill out your credentials with the info from the previous steps
		* Click 'Create'	
	* Now run the following command to bind this service to your app and start it up:
	
			$ jbuild deploycompose appName
		
		
	#### MongoDB
	The following command will create a MongoDB service called todo-mongo-db, bind it to your app, and start your app:
	
		$ jbuild deploymongo appName
		
**Note**: If the `jbuild` command exits the first time without deploying your app due to `npm install`, just run it again.
	

## Running the app locally

To run the app locally, you will first need to have a CouchDB server and/or a Mongo DB server running locally. The code assumes that the database is using the default port.

After installing the DB server locally, and installing the app via steps 3, 5, and 6 above, you can run it with the command:

    node server -d cloudant

for Couch DB or

    node server -d mongo

for Mongo DB.

You should see the output like the following written to the console:

    bluemix-todo-apps-node: VCAP_SERVICES environment is not set
    bluemix-todo-apps-node: using database:  http://127.0.0.1:5984/todos
    bluemix-todo-apps-node: server starting: http://localhost:6024

You can use the URL listed on the last line to access the application. It stores the data locally in the database listed on the second line.


## About the app

#### CoffeeScript
This application is written in [CoffeeScript][coffeescript_url] and
compiled into JavaScript.  The CoffeeScript source is available in the
`lib-src` directory, and the compiled JavaScript files are available in the
`lib` directory.

The CoffeeScript files break down as follows:

* `cli.coffee` - Handles the command-line invocation of `node server`; parses the command line and constructs a call to the `server` module.

* `couch-db.coffee` - Handles interaction with the CouchDB database.  A `DB` object is created to handle the interaction, which has a set of [SCRUD methods][scrud_methods_url] (search/create/read/update/delete) to access the data.

* `mongo-db.coffee` - Handles interaction with the Mongo database.  A `DB` object is created to handle the interaction, which has a set of [SCRUD methods][scrud_methods_url] (search/create/read/update/delete) to access the data.

* `server.coffee` - Handles the HTTP server

* `tx.coffee` - Provides a transaction object which turns HTTP requests into Database operations.

* `utils.coffee` - Provides some utility functions used in all the modules.

#### npm Packages

The application makes use of the following packages from npm:

* [express][express_url] - A framework to build web server applications

* [nano][nano_url] - A library to interface with CouchDB

* [nopt][nopt_url] - A library to parse command-line arguments

* [ports][ports_url] - A library to consistently manage name http ports

* [q][q_url] - A promises library

* [underscore][underscore_url] - A utility belt library with lots of handy functions

* [monk][monk_url] - A library to interface with Mongo DB

#### Promises

This application makes heavy use of Q promises to handle async calls.
Promises are explained in depth on
[Q's project page][q_github_url] and
[an introduction to promises][promises_intro_url] is
available at the <http://promisejs.org> site.


## Hacking

If you want to modify the source to play with it, you'll also want to have the `jbuild` program installed.

To install `jbuild` on Windows, use the command

    npm -g install jbuild

To install `jbuild` on Mac or Linux, use the command

    sudo npm -g install jbuild

The `jbuild` command runs tasks defined in the `jbuild.coffee` file.  The task you will most likely use is `watch`, which you can run with the command:

    jbuild watch

When you run this command, the application will be built from source, the server started, and tests run.  When you subsequently edit and then save one of the source files, the application will be re-built, the server re-started, and the tests re-run.  For ever.  Use Ctrl-C to exit the `jbuild watch` loop.

You can run those build, server, and test tasks separately.  Run `jbuild` with no arguments to see what tasks are available, along with a short description of them.

### Troubleshooting

To troubleshoot your Bluemix app the main useful source of information is the logs. To see them, run:

  ```
  $ cf logs <application-name> --recent
  ```

### License

[Apache License, Version 2.0][apache_license_url]

### Privacy Notice

The TodoMVC node sample web application includes code to track deployments to Bluemix and other Cloud Foundry platforms. The following information is sent to a [Deployment Tracker][deploy_track_url] service on each deployment:

* Application Name (`application_name`)
* Space ID (`space_id`)
* Application Version (`application_version`)
* Application URIs (`application_uris`)

This data is collected from the `VCAP_APPLICATION` environment variable in IBM Bluemix and other Cloud Foundry platforms. This data is used by IBM to track metrics around deployments of sample applications to IBM Bluemix. Only deployments of sample applications that include code to ping the Deployment Tracker service will be tracked.

### Disabling Deployment Tracking

Deployment tracking can be disabled by removing `require("cf-deployment-tracker-client").track();` from the beginning of the `server.js` file at the root of this repo.

[compose_url]: https://www.compose.io/
[cloudant_url]: https://cloudant.com/
[nodejs_install_url]: https://nodejs.org/
[bluemix_signup_url]: https://console.ng.bluemix.net/?cm_mmc=Display-GitHubReadMe-_-BluemixSampleApp-Todo-_-Node-Compose-_-BM-DevAd
[cloud_foundry_url]: https://github.com/cloudfoundry/cli
[download_node_url]: https://nodejs.org/download/
[compose_signup_url]: https://app.compose.io/signup/svelte
[coffeescript_url]: http://coffeescript.org/
[express_url]: https://npmjs.org/package/express
[nano_url]: https://npmjs.org/package/nano
[nopt_url]: https://npmjs.org/package/nopt
[ports_url]: https://npmjs.org/package/ports
[q_url]: https://npmjs.org/package/q
[underscore_url]: https://npmjs.org/package/underscore
[monk_url]: https://www.npmjs.org/package/monk
[q_github_url]: https://github.com/kriskowal/q
[promises_intro_url]: http://www.promisejs.org/intro/
[scrud_methods_url]: http://en.wikipedia.org/wiki/Create,_read,_update_and_delete
[apache_license_url]: http://www.apache.org/licenses/LICENSE-2.0.html
[deploy_track_url]: https://github.com/cloudant-labs/deployment-tracker
