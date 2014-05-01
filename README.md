about
================================================================================

The ToDo sample apps are meant to be simple demos of how you can take advantage
of BlueMix and a database service.  In addition it shows how to take advantage
of both the [built-in](https://www.ng.bluemix.net/docs/RT/Runtimes.jsp) and 
[community buildpacks](https://github.com/cloudfoundry-community/cf-docs-contrib/wiki/Buildpacks) 
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

    git clone git@github.com:CodenameBlueMix/todo-apps.git

The repository contains a directory for each implementation, and the
`frontend` directory contains the web resources that are used by each
implementation.



running the samples
================================================================================

To run the samples on BlueMix you must have signed up for BlueMix and have 
installed the Cloud Foundry command line tool.  To sign up for BlueMix head to
[bluemix.net](http://bluemix.net) and register.

You can download the Cloud Foundry command line tool by following the steps in the
[BlueMix documentation](http://www.ng.bluemix.net/docs/BuildingWeb.jsp#install-cf).

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

couch DB and cloudant
================================================================================

If you want to deploy the Cloudant versions of the various ToDo apps to BlueMix
there is some additional setup you need to do before doing so.  Cloudant is 
cloud based implementation of Couch DB, in other words it is a DBaaS 
(database as a service).  In order to use Cloudant with the ToDo apps you will
need to sign up for a [Cloudant account](https://cloudant.com/).  It is free
as long as your usage is limited, in other words you shouldn't have to worry
about going over the usage limits for the ToDo apps :)

Next you will need to create a database for the ToDo apps to use and create an
API key and password for the apps to authenticate with.  Watch the short video 
below to see how to do that or you can follow the written instructions if you
prefer.


### setting up a CouchDB server running on the cloud

Setting up a CouchDB server on the cloud is also easy, if you don't already
have one running on the cloud.

Head over to the [Cloudant](https://cloudant.com/) web site to sign up for
a free account.  A free account lets you create some small databases, which
is perfect for running this demo - it won't use a lot of space.

Once you've got your Cloudant account, head to the dashboard (should be the
first page you land on after you sign in), and add a new database.

You will be prompted for the name; call it "bluemix-todo".

Once the database is created, all you need to do is add an API key to it.
From the main dashboard, click the lock icon under the Actions column for the
"bluemix-todo" database.  If you don't see that, you may be on the page
for the database itself; click on the orange "Databases" button in the
left-hand navigation to get back to the main dashboard.

Once you've clicked the lock icon, you be taken to the Permissions page of the
database.  We want to generate
a new key, so click that green "Generate API key" on the right side of the
page.

This will provide two values to you, which you will need to remember.
Specifically, a Key and Password.  The key will be listed for you in the
future as a row in the Permissions page, but the Password value you
will never see again.  We'll need these values, so write them down somewhere!
Probably copy/paste it into a file temporarily, as we'll be using it again
in a minute.

You can also see on this page that the new API key only has "Reader"
authority.  Go ahead and give it Writer and Admin authority as well.

That's all!  Now we'll want to create a BlueMix service for this database.

To do that, follow the directions in the README.md files for the
language-specific implementations (eg, java, node, etc).


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
*  You should create a Mongo DB service with the name "todo-mongo-db".

#### Setting Up a Mongo DB server Locally

It will most likely be useful to have a local Mongo DB server for testing
when adding new implementations.

See the Mongo DB [install instructions](http://docs.mongodb.org/manual/installation/) 
for your platform to install a local Mongo DB server.

### Couch DB / Cloudant

*  The application should look for the credentials for the Couch DB or
Cloudant database from a user service called todo-couch-db.  The credentials
should container three properties username, password, and url.  

*  The name of the Couch DB / Cloudant databse to use is "bluemix-todo".

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
