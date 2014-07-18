bluemix-todo-apps - node version
================================================================================

TodoMVC using Backbone, CouchDB and a node back-end, running on BlueMix.

Refer to the README.md file in the parent directory
(eg, `bluemix-todo-apps/README.md`) for general instructions regarding
this application and the database service it requires.

This application supports both Mongo DB and Couch DB (Cloudant) as a backend.



installation
================================================================================

To run the program, you'll need [node.js installed](http://nodejs.org/).
From a command/shell terminal
* `cd` into the parent directory you want to install the project in
* `git clone` the project into a child directory
* `cd` into that child directory
* `cd` into the `node` directory
* `npm install` to install dependencies


For example:

    $ cd Projects
    $ git clone [INSERT GIT REPO HERE]

        ... git output here ...

    $ cd bluemix-todo-apps

    $ cd node

    $ npm install

        ... npm output here ...



running locally
================================================================================

To run the app locally, you will first need to have a CouchDB server and/or a Mongo DB server running
locally.  The code assumes that the database is using the default port.

After installing the DB server locally, and installing the app via the directions
above, you can run it with the command:

    node server -d cloudant

for Couch DB or

    node server -d mongo

for Monogo DB.

You should see the following output written to the console:

    bluemix-todo-apps-node: VCAP_SERVICES environment is not set
    bluemix-todo-apps-node: using database:  http://127.0.0.1:5984/todos
    bluemix-todo-apps-node: server starting: http://localhost:6024

You can use the URL listed on the last line to access the application.  It
stores the data locally in the database listed on the second line.


deploying to BlueMix
================================================================================

To deploy this application to BlueMix you must have the Cloud Foundry command
line installed and have logged into BlueMix using the command line.
  
To use the simple commands below to deploy the application you will also
need jbuild installed.  It can be installed via npm.

To install `jbuild` on Windows, use the command

    npm -g install jbuild

To install `jbuild` on Mac or Linux, use the command

    sudo npm -g install jbuild

To deploy the Mongo DB version to BlueMix run

    $ jbuild deploymongo appName

Replace appName with a unique name for your application.  This command will create a Mongo DB service called todos-mongo-db if one does not already exist.

To deploy the Couch DB / Cloudant version to BlueMix run

    $ jbuild deploycloudant appName

Replace appName with a unique name for your application.  

Other commands you may want to use

* `cf logs bluemix-todo-node`

  shows continuous log information for the app; Ctrl-C to exit

* `cf logs bluemix-todo-node --recent`

  shows recent log information for the app

* `cf stop bluemix-todo-node`

  stop the app if it's running

* `cf start bluemix-todo-node`

  start the app if it's stopped

* `cf app bluemix-todo-node`

  show information about the app

* `cf apps`

  show information about all the apps you have

You can of course also do all this through the
[ACE dashboard](https://ace.ng.bluemix.net/) as well.



about the application
================================================================================

This application is written in [CoffeeScript](http://coffeescript.org/) and
compiled into JavaScript.  The CoffeeScript source is available in the
`lib-src` directory, and the compiled JavaScript files are available in the
`lib` directory.

The application makes use of the following packages from npm:

* [express](https://npmjs.org/package/express)

  a framework to build web server applications

* [nano](https://npmjs.org/package/nano)

  a library to interface with CouchDB

* [nopt](https://npmjs.org/package/nopt)

  a library to parse command-line arguments

* [ports](https://npmjs.org/package/ports)

  a library to consistently manage name http ports

* [q](https://npmjs.org/package/q)

  a promises library

* [underscore](https://npmjs.org/package/underscore)

  a utility belt library with lots of handy functions

* [monk](https://www.npmjs.org/package/monk)
  
  a library to interface with Mongo DB



###promises###

This application makes heavy use of Q promises to handle async calls.
Promises are explained in depth on
[Q's project page](https://github.com/kriskowal/q) and
[an introduction to promises](http://www.promisejs.org/intro/) is
available at the <http://promisejs.org> site.



files
================================================================================

* `lib/*`

  the JavaScript files compiled from the `lib-src` CoffeeScript source

* `lib-src/*`

  the CoffeeScript files - more detail below

* `node_modules/*`

  the pre-req node packages installed when you run `npm install`

* `tests/*`

  some tests that get run as part of the build

* `www/*`

  a copy of the files from the parent `frontend` directory (eg,
  `bluemix-todo-apps/frontend`)

* `README.md`

  This file!

* `jbuild.coffee`

  A file used to build and deploy the application; see the "hacking" topic, below.

* `manifest.yml`

  This file sets the attributes for your application when it's deployed.

* `package.json`

  This file is used by BlueMix to determine that this is a node application,
  so that the node.js buildpack is used to deploy the application.  It's
  a standard file for node.js applications anyway, used to determine the
  pre-req packages your application uses.  Eg, `npm install` uses the
  dependency lists in the file to determine which packages to install from
  npm.

* `server.js`

  A simple program to launch the application.



CoffeeScript files
================================================================================

The following files are the source files for the application, available in
the `lib-src` directory.  Below is a brief description of the files.

* `cli.coffee`

  Handles the command-line invocation of `node server`; parses the command
  line and constructs a call to the `server` module.

* `couch-db.coffee`

  Handles interaction with the CouchDB database.  A `DB` object is created
  to handle the interaction, which has a set of
  [SCRUD methods](http://en.wikipedia.org/wiki/Create,_read,_update_and_delete)
  (search/create/read/update/delete) to access the data.

* `mongo-db.coffee`

  Handles interaction with the Mongo database.  A `DB` object is created
  to handle the interaction, which has a set of
  [SCRUD methods](http://en.wikipedia.org/wiki/Create,_read,_update_and_delete)
  (search/create/read/update/delete) to access the data.

* `server.coffee`

  Handles the HTTP server

* `tx.coffee`

  Provides a transaction object which turns HTTP requests into Database
  operations.

* `utils.coffee`

  Provides some utility functions used in all the modules.

hacking
================================================================================

If you want to modify the source to play with it, you'll also want to have the
`jbuild` program installed.

To install `jbuild` on Windows, use the command

    npm -g install jbuild

To install `jbuild` on Mac or Linux, use the command

    sudo npm -g install jbuild

The `jbuild` command runs tasks defined in the `jbuild.coffee` file.  The
task you will most likely use is `watch`, which you can run with the
command:

    jbuild watch

When you run this command, the application will be built from source, the server
started, and tests run.  When you subsequently edit and then save one of the
source files, the application will be re-built, the server re-started, and the
tests re-run.  For ever.  Use Ctrl-C to exit the `jbuild watch` loop.

You can run those build, server, and test tasks separately.  Run `jbuild`
with no arguments to see what tasks are available, along with a short
description of them.



license
================================================================================

Apache License, Verison 2.0

<http://www.apache.org/licenses/LICENSE-2.0.html>
