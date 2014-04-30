# Licensed under the Apache License. See footer for details.

Q    = require "q"
_    = require "underscore"
monk = require "monk"

utils = require "./utils"

# get better stack traces for promises
Q.longStackSupport = true

# max items we store in the DB
MAX_ITEMS  = 20

# seconds between gc runs to trim items in the DB
GC_SECONDS = 30

#-------------------------------------------------------------------------------
# initialize the database, given the full URL
#
# returns a promise of the db when it's done initializing
#-------------------------------------------------------------------------------
exports.init = (url) ->

    # create the DB
    db = new DB url
    setInterval (-> db.gc()), GC_SECONDS * 1000
    db


#-------------------------------------------------------------------------------
# provides a SCRUD interface to our db - Search/Create/Read/Update/Delete
#-------------------------------------------------------------------------------
class DB

    #---------------------------------------------------------------------------
    constructor: (url) ->
        utils.log url
        @mongoDB = monk url

    #---------------------------------------------------------------------------
    # return count of items in the db
    #---------------------------------------------------------------------------
    count: ->
        @_dbCall 'count', {}
        .then (result) ->
            result

    #---------------------------------------------------------------------------
    # Scrud - return all items in the db
    #---------------------------------------------------------------------------
    search: ->
        @_dbCall 'find', {}
        .then (result) ->
            result.map (todo) ->
                todo.id = todo._id
                delete todo['_id']
                todo

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

        @_dbCall 'insert', item
        .then (result) ->
            item.id = result._id
            delete item['_id']
            item

    #---------------------------------------------------------------------------
    # scRud - read an item
    #---------------------------------------------------------------------------
    read: (id) ->
        unless id?
            err = new Error "id cannot be null"
            return Q.reject err

        @_dbCall 'findById', id
        .then (result) ->
            result

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

        @_dbCall 'findAndModify', {"_id" : id}, item
        .then (result) ->
            result.id = id
            delete result['_id']
            result.order = item.order
            result.completed = item.completed
            result.title = item.title
            result

    #---------------------------------------------------------------------------
    # scruD - delete an item
    #
    # note - always deletes the existing revision
    #---------------------------------------------------------------------------
    delete: (id) ->
        unless id?
            err = new Error "id cannot be null"
            return Q.reject err

        @_dbCall 'remove', {"_id" : id}

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
        coll = @mongoDB.get 'todos'
        Q.npost coll, method, args

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
