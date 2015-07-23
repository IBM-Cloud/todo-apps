about
================================================================================

The ToDo sample apps are meant to be simple demos of how you can take advantage
of Bluemix and a database service.  In addition it shows how to take advantage
of both the [built-in](https://www.ng.bluemix.net/docs/starters/rt_landing.html) and [community buildpacks](https://github.com/cloudfoundry-community/cf-docs-contrib/wiki/Buildpacks) 
to deploy your app using whatever runtime you choose.

The ToDo app is pretty simple, it allows you to add an persist ToDos that you
need to get done.  As you complete different ToDos you can mark them done and
eventually delete them from the list.  The ToDos are stored in a database.

All implementations support two different database backends, 
[Mongo DB](https://www.mongodb.org/) and [Couch DB](http://couchdb.apache.org/) 
via [Cloudant](https://cloudant.com/).  If you want to use Cloudant see the
section of this README titled "Couch DB and Cloudant".



background
================================================================================

The front-end UI for the ToDo app uses a slightly modified version of the
Backbone sample from [TodoMVC.com](http://todomvc.com/architecture-examples/backbone/).  The main change was
to change the `collections/todo.js` file to not use `localStorage`,
but to instead set the `url` to `apis/todos`,
so that the ToDos are retrieved from the server instead of from localStorage.

The original `collections/todo.js` file is
[here](https://github.com/tastejs/todomvc/blob/gh-pages/architecture-examples/backbone/js/collections/todos.js).

There are various implementation for the back-end of the ToDo app.
We have implemented the back-end currently in
Java, Node.js, Sinatra, Python, and PHP.  If you want to contribute a backend
in another language we enchourage you to do.  See the "adding new implementations"
section.

All of the implementations are similar; they serve up the files in the
`frontend` directory as static web resources - html, css, js files.  And they
also expose an api at the uri `api/todos` to query and modify the ToDos.

Note that this isn't really a realistic ToDo app, at present, since it maintains
a global list of Todos that the entire world shares.  The intent is just to
show using an existing simple front-end application with a simple database
back-end.  A real example would probably include user authentication, storing
ToDo's on a per-user basis, etc.


getting the code
================================================================================

To get the code you can just clone the repo.

    git clone git@github.com:IBM-Bluemix/todo-apps.git

The repository contains a directory for each implementation, and the
`frontend` directory contains the web resources that are used by each
implementation.



running the samples
================================================================================

To run the samples on BlueMix you must have signed up for BlueMix and have 
installed the Cloud Foundry command line tool.  To sign up for BlueMix head to
[bluemix.net](https://console.ng.bluemix.net/?cm_mmc=Display-GitHubReadMe-_-BluemixSampleApp-Todo-_-Node-Compose-_-BM-DevAd) and register.

You can download the Cloud Foundry command line tool by following the steps in the [README file](https://github.com/cloudfoundry/cli).

After you have installed the Cloud Foundry command line tool you need to point it
at BlueMix so it knows where to deploy the applications.  You can do this by running

    cf login -a https://api.ng.bluemix.net

This will prompt you to login with your BlueMix user ID and password which is the
same as your IBM ID and password.  You should only need to do this once, the command
line tool will remember this information.

Most of the projects use build technologies that are specific to the runtime the ToDo
app is written in to deploy the app to BlueMix (Maven, Rake, Paver, etc).  The assumption 
is that these are tools developers who are using these runtimes are familiar with.  
Under the covers they are using the Cloud Foundry command line to deploy the apps.  
The benefit is that you don't need to remember verbose commands (in most cases) and
continue to use tools you are comfortable with.  See the individual runtime folders
(java, node, php, python, sinatra) for more details on how to deploy the various 
versions.


adding new implementations
================================================================================

Feel free to expand upon this project by adding new implementations in your
favorite runtime or framework.  Below is a simple specification you should keep
in mind when adding new implementations.

### REST Endpoints

*  The server implementation should support GET, POST, PUT, and DELETE.


#### GET Request - Gets all ToDos

    GET /api/todos

GET Response
The response should be a JSON array of all ToDos.

    [{"completed":false,"id":"001fbbe7bd708a34624b47526cd6ac89","order":1,"title":"test"},{"completed":false,"id":"4d6153cf4bc3bdaf9c6c7eebf42d67a6","order":2,"title":"1"},{"completed":false,"id":"be3855e004dd5d74c802992c09ea8d28","order":3,"title":"2"},{"completed":false,"id":"e7cb7149098961e5dc182715f1cb0e9d","order":4,"title":"3"},{"completed":false,"id":"9368ccc4629a1c8dfd99a9e741d01c44","order":5,"title":"4"},{"completed":false,"id":"e5752d462b83f13da3d8dced1c15eb43","order":6,"title":"5"}]



#### POST Request - Creates a new ToDo

    POST /api/todos/

POST Body

    {"title":"another","order":7,"completed":false}

POST Response
The response should be a JSON representation of a ToDo with the id field
populated.

{"completed":false,"id":"f76424cc41f1ee8c2682a37069098794","order":7,"title":"another"}



#### PUT Request - Updates a ToDo

    PUT /api/todos/[id]

PUT Body

    {"completed":true,"id":"4d6153cf4bc3bdaf9c6c7eebf42d67a6","order":2,"title":"1"}

PUT Response
The response should be a JSON representation of the updated ToDo.

    {"completed":true,"id":"4d6153cf4bc3bdaf9c6c7eebf42d67a6","order":2,"title":"1"}


#### DELETE Request - Deletes a ToDo

    DELETE /api/todos/[id]

DELETE Response
The response should be a 204.


### Mongo

*  The Mongo implementation should use a collection called "todos".
*  You should create a Mongo DB service with the name "todo-mongo-db" or "todo-compose-mongo-db", depending on which Mongo provider you choose.

#### Setting Up a Mongo DB server Locally

It will most likely be useful to have a local Mongo DB server for testing
when adding new implementations.

See the Mongo DB [install instructions](http://docs.mongodb.org/manual/installation/) 
for your platform to install a local Mongo DB server.

### Couch DB / Cloudant

*  You should create a Cloudant service with the name "todo-couch-db".

*  The name of the Couch DB / Cloudant database to use is "bluemix-todo".

<b>View</b>

The application code should create a view document, if one doesn't already exist,
in the bluemix-todo database with the following JSON.

    {
      views: {
        allTodos: {
          reduce: "_count",
          map: "function(doc){if(doc.title && doc.completed != null){emit(doc.order,{title: doc.title,completed: doc.completed})}}"
        }
      }
    }


#### Setting Up CouchDB Server Locally

It will most likely be useful to have a local Couch DB server for testing
when adding new implementations.

Relax, this is very easy!

Head over to the official home of CouchDB - <http://couchdb.apache.org/> - and
click the red "DOWNLOAD" link.  Follow the instructions to download a verison
of CouchDB for your platform.

When running the Mac version of CouchDB, you'll have a menu bar tool-button you can
use to start and manage the database.

The implementations will run locally as long as the CouchDB server is running.
By default the Couch DB will be running at http://127.0.0.1:5984.
