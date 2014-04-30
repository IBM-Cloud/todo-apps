# Licensed under the Apache License. See footer for details.

#-------------------------------------------------------------------------------
# returns a new Tx object, ready to have an operation run on it
#
# these objects convert http requests into db actions and generate http responses
#-------------------------------------------------------------------------------
exports.tx = (request, response, todoDB) ->
    new Tx request, response, todoDB

#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
class Tx

    #---------------------------------------------------------------------------
    constructor: (@request, @response, @todoDB) ->

    #---------------------------------------------------------------------------
    search:  ->

        # get items
        @todoDB.search()

        # return item on success
        .then (items) =>
            @response.send items

        .fail (err) => @response.send 500, {err: "#{err}"}
        .done()

    #---------------------------------------------------------------------------
    create: ->

        # create item
        @todoDB.create @request.body

        # return item on success
        .then (item) =>
            @response.send item

        .fail (err) => @response.send 500, {err: "#{err}"}
        .done()

    #---------------------------------------------------------------------------
    read:   ->

        # read item
        @todoDB.read @request.params.id

        # send item or 404 response if not found
        .then (item) =>
            if item?
                @response.send item
            else
                @response.send 404

        .fail (err) => @response.send 500, {err: "#{err}"}
        .done()

    #---------------------------------------------------------------------------
    update: ->

        # read item
        @todoDB.read @request.params.id

        # if not found, send 404, otherwise do update
        .then (item) =>
            unless item?
                @response.send 404
                return null

            # perform update
            @todoDB.update @request.params.id, @request.body

            # return item on success
            .then (item) =>
                @response.send item

        .fail (err) => @response.send 500, {err: "#{err}"}
        .done()


    #---------------------------------------------------------------------------
    delete: ->

        # read item
        @todoDB.read @request.params.id

        # if not found, send 404, otherwise do delete
        .then (item) =>
            unless item?
                @response.send 404
                return null

            # perform delete
            @todoDB.delete @request.params.id

            # return item on success
            .then (id) =>
                @response.send item

        .fail (err) => @response.send 500, {err: "#{err}"}
        .done()

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
