# Licensed under the Apache License. See footer for details.

Q    = require "q"
_    = require "underscore"
nano = require "nano"

utils = require "./utils"

# get better stack traces for promises
Q.longStackSupport = true

# max items we store in the DB
MAX_ITEMS  = 20

# seconds between gc runs to trim items in the DB
GC_SECONDS = 30

#-------------------------------------------------------------------------------
# design documentation for our database
#-------------------------------------------------------------------------------
allTodos_map = (doc) ->
    {title, completed, order} = doc

    return unless title? and completed? and order?

    emit order, {title, completed}

DESIGN_NAME = "todos"
DESIGN_DOC  =
    views:
        allTodos:
             map:    allTodos_map.toString()
             reduce: "_count"

#-------------------------------------------------------------------------------
# initialize the database, given the full URL
#
# returns a promise of the db when it's done initializing
#-------------------------------------------------------------------------------
exports.init = (url) ->

    # create the DB
    db = new DB url

    # we need some component bits from the full db url
    match = url.match /(.*)\/(.*)/
    unless match?
        return Q.reject new Error "url must have a path component: #{url}"

    [ignore, baseUrl, name] = match

    # try creating the database, in case it doesn't exist
    nanoBase = nano baseUrl

    Q.ninvoke nanoBase.db, "create", name

    # ok if it fails, the db already exists
    .fail ->
        null

    # does it have our design?
    .then ->
        db._dbCall "get", "_design/#{DESIGN_NAME}"

    # create the design doc if not already there
    .fail ->
        db._dbCall "insert", DESIGN_DOC, "_design/#{DESIGN_NAME}"

    # set up our garbage collector, return the db object
    .then ->
        setInterval (-> db.gc()), GC_SECONDS * 1000

        return db

#-------------------------------------------------------------------------------
# provides a SCRUD interface to our db - Search/Create/Read/Update/Delete
#-------------------------------------------------------------------------------
class DB

    #---------------------------------------------------------------------------
    constructor: (url) ->
        @nanoDB = nano url

    #---------------------------------------------------------------------------
    # return count of items in the db
    #---------------------------------------------------------------------------
    count: ->
        opts =
            reduce: true

        # get the reduce value on the view
        @_dbCall  "view", DESIGN_NAME, "allTodos", opts

        # return it
        .then (result) ->
            result[0]?.rows[0]?.value || 0

    #---------------------------------------------------------------------------
    # Scrud - return all items in the db
    #---------------------------------------------------------------------------
    search: ->

        # query our view of the items
        opts =
            reduce:       false
            limit:        MAX_ITEMS

        @_dbCall  "view", DESIGN_NAME, "allTodos", opts

        # on success, convert to sanitized objects
        .then (result) ->
            result[0].rows.map (item) ->
                id:        item.id
                title:     item.value?.title
                completed: item.value?.completed
                order:     item.key

    #---------------------------------------------------------------------------
    # sCrud - create a new item
    #---------------------------------------------------------------------------
    create: (item) ->

        # prepare the item to be created
        item = @_sanitize item
        delete item.id if item?

        unless item?
            err = new Error "item cannot be null"
            return Q.reject err

        # add the item
        @_dbCall "insert", item

        # return the item with it's new id
        .then (result) ->
            item.id = result[0].id
            return item

    #---------------------------------------------------------------------------
    # scRud - read an item
    #---------------------------------------------------------------------------
    read: (id) ->
        unless id?
            err = new Error "id cannot be null"
            return Q.reject err

        # get the item
        @_get id

        # return null if not found
        .fail (err) =>
            return null if err.message is "missing"
            throw err

        # return sanitized version
        .then (item) =>
            @_sanitize item

    #---------------------------------------------------------------------------
    # scrUd - update an item
    #
    # note - always overwrites existing revision
    #---------------------------------------------------------------------------
    update: (id, item) ->

        # prepare the item for update
        item = @_sanitize item

        unless id?
            err = new Error "id cannot be null"
            return Q.reject err

        unless item?
            err = new Error "item cannot be null"
            return Q.reject err

        if id isnt item.id
            err = new Error "id does not match item.id"
            return Q.reject err

        # get the item, so we can get the current rev
        @_get id

        # update the item
        .then (item_) =>
            delete item.id

            item._id  = item_._id
            item._rev = item_._rev

            @_dbCall "insert", item

        # return the sanitized object
        .then () =>
            item.id = id
            @_sanitize item

    #---------------------------------------------------------------------------
    # scruD - delete an item
    #
    # note - always deletes the existing revision
    #---------------------------------------------------------------------------
    delete: (id) ->
        unless id?
            err = new Error "id cannot be null"
            return Q.reject err

        # get the item, so we can get the current rev
        @_get id

        # if the item doesn't exist, PERFECT! work already done
        .fail (err) =>
            return null if err.message is "missing"
            throw err

        # delete the item if it exists
        .then (item) =>
            return null unless item?
            @_dbCall "destroy", id, item._rev

        # return the id
        .then () ->
            { id }

    #---------------------------------------------------------------------------
    # run a gc cycle, removing items over the threshold
    #---------------------------------------------------------------------------
    gc: ->

        # get the number of items in the db
        @count()

        # if it's ok, return null, otherwise query them all
        .then (count) =>
            return if count <= MAX_ITEMS

            @search()

        # if there are items to be deleted, delete one of them
        .then (items) =>
            return unless items
            return unless items.length

            utils.log "gc: #{items[0].id}"
            @delete items[0].id

        # assuming it was deleted ok, schedule another gc to delete another
        # will stop iterating when count <= MAX_ITEMS
        .then (id) =>
            return unless id

            process.nextTick => @gc()

        # woe!
        .fail (err) ->
            utils.log "gc: error: #{err}"

        # report errors
        .done()

    #---------------------------------------------------------------------------
    # shortcut for getting an item by id
    #---------------------------------------------------------------------------
    _get: (id) ->
        @_dbCall  "get", id

        .then (result) ->
            result[0]

    #---------------------------------------------------------------------------
    # wrapper to invoke nano async, returning a promise
    #---------------------------------------------------------------------------
    _dbCall: (method, args...) ->
        Q.npost @nanoDB, method, args

    #---------------------------------------------------------------------------
    # sanitize an item by white-listing valid properties
    #---------------------------------------------------------------------------
    _sanitize: (obj) ->
        return null unless obj?

        {id, title, completed, order} = obj
        obj = {id, title, completed, order}

        return null if !obj.id? and !obj.title? and !obj.completed? and !obj.order?

        obj

#---------------------------------------------------------------------------
# debug wrapper for a promise logging some diagnostics
#---------------------------------------------------------------------------
debugP = (p, label) ->
    return p if true

    utils.log "#{label} ->"

    p

    .then (result) =>
        utils.log "#{label}: success: #{utils.JL result}"
        result

    .fail (err) =>
        utils.log "#{label}: error: #{err}"
        throw err

    return p

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
