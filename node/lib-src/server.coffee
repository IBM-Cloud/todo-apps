# Licensed under the Apache License. See footer for details.

# name of the todo service in cloud foundry
TODO_COUCH_SERVICE = "todo-couch-db"
TODO_MONGOLAB_SERVICE = "todo-mongo-db"
TODO_COMPOSEMONGO_SERVICE = "todo-compose-mongo-db"

# local couchdb server
TODO_COUCH_LOCAL   = "http://127.0.0.1:5984"

# local mongo server
TODO_MONGO_LOCAL   = "mongodb://localhost:27017/db"

#-------------------------------------------------------------------------------
URL  = require "url"
http = require "http"

Q       = require "q"
_       = require "underscore"
ports   = require "ports"
express = require "express"
cfEnv   = require "cfenv"

couchDB = require "./couch-db"
mongoDB = require "./mongo-db"
tx      = require "./tx"
utils   = require "./utils"

# global reference to our db object
todoDB = null

# get core data from Cloud Foundry environment
appEnv = cfEnv.getAppEnv
    name: utils.PROGRAM

#-------------------------------------------------------------------------------
# diagnostic message when server exits for any reason
#-------------------------------------------------------------------------------
process.on "exit", (status) ->
    utils.log "process exiting with status #{status}"

#-------------------------------------------------------------------------------
# start the server, returning a promise of the server;
# the promise is resolved when the server starts
#-------------------------------------------------------------------------------
exports.start = (options) ->
    utils.verbose true if options.verbose

    # sometimes you need to dump your ENV vars
    # utils.vlog "process.env #{utils.JL process.env}"

    if options.db == "cloudant"
        utils.log "Using Couch DB"
        # get the url to the couch database
        couchURL = getCouchURL()
        utils.log "using database:  #{couchURL}"

        # initialize the database, async
        couchDB.init couchURL

        # if db init fails, exit
        .fail (err) ->
            utils.log "error initializing database:"
            utils.logError err

        # if db init succeeds, start the server
        .then (todoDB_) ->

            # store the db in a global
            todoDB = todoDB_

            server = new Server options
            server.start()

        # handle exceptions
        .done()
    else if options.db == "mongo"
        utils.log "Using MongoLab DB"
        # get the url to the Mongo database
        mongoURL = getMongoLabURL()
        utils.log "using database:  #{mongoURL}"

        # initialize the database, async
        todoDB = mongoDB.init mongoURL
        server = new Server options
        server.start()
    else if options.db == "compose"
        utils.log "Using Compose Mongo DB"
        # get the url to the Mongo database
        mongoURL = getComposeMongoURL()
        utils.log "using database:  #{mongoURL}"

        # initialize the database, async
        todoDB = mongoDB.init mongoURL
        server = new Server options
        server.start()

#-------------------------------------------------------------------------------
# the url to the CouchDB instance
#-------------------------------------------------------------------------------
getCouchURL = ->
    url = appEnv.getServiceURL TODO_COUCH_SERVICE,
        pathname: "database"
        auth:     ["username", "password"]

    url = url || TODO_COUCH_LOCAL

    length = url.length - 1
    endsInSlash  = url.indexOf('/', length)
    if endsInSlash == -1
        url = url + '/'

    url = url + 'bluemix-todo'

    return url

#-------------------------------------------------------------------------------
# the url to the MongoLab DB instance
#-------------------------------------------------------------------------------
getMongoLabURL = ->
    url = appEnv.getServiceURL TODO_MONGOLAB_SERVICE

    url = url || TODO_MONGO_LOCAL

    return url

#-------------------------------------------------------------------------------
# the url to the Compose Mongo DB instance
#-------------------------------------------------------------------------------
getComposeMongoURL = ->
    mongoCreds = appEnv.getServiceCreds TODO_COMPOSEMONGO_SERVICE
    if mongoCreds
        composeDbName = "todoDB"
        url = "mongodb://" +
               mongoCreds.user + ":" +
               mongoCreds.password + "@" +
               mongoCreds.uri + ":" +
               mongoCreds.port + "/" +
               composeDbName;

    url = url || TODO_MONGO_LOCAL

    return url

#-------------------------------------------------------------------------------
# class that manages the server
#-------------------------------------------------------------------------------
class Server

    #---------------------------------------------------------------------------
    constructor: (options={}) ->
        options.port    ?= appEnv.port
        options.verbose ?= false

        {@port, @verbose} = options

    #---------------------------------------------------------------------------
    # start the server, returning a promise to itself when started
    #---------------------------------------------------------------------------
    start: ->
        deferred = Q.defer()

        app = express()

        # serve up our html/css/js for the browser
        app.use express.static "www"

        # parse JSON bodies in requests
        app.use express.json()

        # create a transaction object
        app.use (req, res, next) ->
            req.tx = tx.tx req, res, todoDB
            next()

        # invoke the appropriate transaction
        app.get    "/api/todos",     (req, res) => req.tx.search()
        app.post   "/api/todos",     (req, res) => req.tx.create()
        app.get    "/api/todos/:id", (req, res) => req.tx.read()
        app.put    "/api/todos/:id", (req, res) => req.tx.update()
        app.delete "/api/todos/:id", (req, res) => req.tx.delete()

        # start the server, resolving the promise when started
        app.listen @port, appEnv.bind, =>
            utils.log "server starting: #{appEnv.url}"

            deferred.resolve @

        return deferred.promise

#-------------------------------------------------------------------------------
# Copyright IBM Corp. 2014
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#-------------------------------------------------------------------------------
