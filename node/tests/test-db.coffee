# Licensed under the Apache License. See footer for details.

Q      = require "q"
expect = require "expect.js"

dbLib = require "../lib/couch-db"

Q.longStackSupport = true

dbURL = "http://127.0.0.1:5984/todos-test"
db    = null

log = (message) -> console.log "test-db: #{message}"

process.on "uncaughtException", (err) ->
    log err.stack
    process.exit 1

#-------------------------------------------------------------------------------
NEXT_ORDER = 1

getOrder = ->
    NEXT_ORDER++

#-------------------------------------------------------------------------------
describe "db", ->

    #---------------------------------------------------------------------------
    before (done) ->
        dbLib.init dbURL

        .then (db_) ->
            db = db_

            db.search()

        .then (items) ->
            deletions = for item in items
                db.delete item.id

            Q.all(deletions)

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be empty", (done) ->

        db.count()

        .then (count) ->
            expect(count).to.be 0

            db.search()

        .then (items) ->
            expect(items.length).to.be 0

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be able to create an item", (done) ->

        item =
            title:     "item C"
            completed: false
            order:     getOrder()

        db.create item

        .then (item_) ->
            expect(item_.title    ).to.be item.title
            expect(item_.completed).to.be item.completed

            db.count()

        .then (count) ->
            expect(count).to.be 1

            db.search()

        .then (items)->
            expect(items.length).to.be 1

            item_ = items[0]
            expect(item_.title    ).to.be item.title
            expect(item_.completed).to.be item.completed

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be able to read an item", (done) ->

        item =
            title:     "item R"
            completed: true
            order:     getOrder()

        db.create item

        .then (item_) ->
            db.read item_.id

        .then (item_) ->
            expect(item_.title    ).to.be item.title
            expect(item_.completed).to.be item.completed

            db.count()

        .then (count) ->
            expect(count).to.be 2

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be able to update an item", (done) ->

        item =
            title:     "item U"
            completed: false
            order:     getOrder()

        db.create item

        .then (item_) ->
            item_.title     = "-#{item.title}-"
            item_.completed = !item_.completed

            db.update item_.id, item_

        .then (item_) ->
            expect(item_.title    ).to.be "-#{item.title}-"
            expect(item_.completed).to.be !item.completed

            db.read item_.id

        .then (item_) ->
            expect(item_.title    ).to.be "-#{item.title}-"
            expect(item_.completed).to.be !item.completed

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be able to delete an item", (done) ->

        item =
            title:     "item D"
            completed: false
            order:     getOrder()

        id = null

        db.create item

        .then (item_) ->
            id = item_.id

            db.delete item_.id

        .then (item_) ->
            expect(item_.id).to.be id

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error when creating bogus item", (done) ->

        db.create null

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "item cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should return null for read of non-existant item", (done) ->

        db.read "non-existant"

        .then (item_) ->
            expect(item_).to.not.be.ok()

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error for update of non-existant item", (done) ->

        item =
            id:        "non-existant"
            title:     "item Une"
            completed: false

        db.update item.id, item

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "missing"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should return id for delete of non-existant item", (done) ->

        db.delete "non-existant"

        .then (id) ->
            expect(id).to.not.be("non-existant")

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error when updating id in item doesn't match", (done) ->

        item =
            id:        "1"
            title:     "item Wrong ID"
            completed: true

        db.update "0", item

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "id does not match item.id"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error to create null item", (done) ->

        item = null

        db.create item

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "item cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error to read null id", (done) ->

        db.read null

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "id cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error to update null id", (done) ->

        db.update null, null

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "id cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error to update null item", (done) ->

        db.update 0, null

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "item cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should be error to delete null id", (done) ->

        db.delete null

        .then (item_) ->
            expect().fail("expected failure")

        .fail (err) ->
            expect(err.message).to.be "id cannot be null"

        .fin -> done()
        .done()

    #---------------------------------------------------------------------------
    it "should ignore id on create", (done) ->

        item =
            id:        "should be ignored"
            title:     "item ignore id on create"
            completed: false
            order:     getOrder()

        id = null

        db.create item

        .then (item_) ->
            expect(item_.id).not.to.be item.id

            db.read item_.id

        .then (item_) ->
            expect(item_.title    ).to.be item.title
            expect(item_.completed).to.be item.completed

        .fail (err) -> expect().fail("failed: #{err}")
        .fin -> done()
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
