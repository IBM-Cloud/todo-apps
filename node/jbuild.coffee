# Licensed under the Apache License. See footer for details.

#-------------------------------------------------------------------------------
# use this file with jbuild: https://www.npmjs.org/package/jbuild
# install jbuild with:
#    linux/mac: sudo npm -g install jbuild
#    windows:        npm -g install jbuild
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
tasks = defineTasks exports,
    watch: "watch for source file changes, then run build, test and server"
    serve: "run the test server stand-alone"
    build: "build the server"
    test:  "run tests"
    copymongomanifest: "copies the mongo manifest file"
    copycloudantmanifest: "copies the cloudant manifest file"
    createmongoservice: "creates a Mongo DB service called todo-mongo-db"

WatchSpec = "lib-src/**/* tests/**/*"

#-------------------------------------------------------------------------------
mkdir "-p", "tmp"

#-------------------------------------------------------------------------------
tasks.build = ->
    log "running build"

    unless test "-d", "node_modules"
        exec "npm install"

        log ""
        log "---------------------------------------"
        log "exiting jbuild because of `npm install`"
        log "---------------------------------------"

        process.exit 1

    cleanDir "lib"

    log "running build"

    log "- compiling server coffee files"
    coffee "--output lib lib-src"

    log "- copying front end files"
    cleanDir "www"
    cp "-R", "../frontend/*", "www"

#-------------------------------------------------------------------------------
tasks.watch = ->
    watchIter()

    watch
        files: WatchSpec.split " "
        run:   watchIter

    watchFiles "jbuild.coffee" :->
        log "jbuild file changed; exiting"
        process.exit 0

#-------------------------------------------------------------------------------
tasks.serve = ->
    log "running server"

    command = "server --verbose --serve"
    #command = "lib/db"
    server.start "tmp/server.pid", "node", command.split " "

#-------------------------------------------------------------------------------
tasks.test = ->
    log "running tests"

    tests = "tests/test-*.coffee"

    options =
        ui:         "bdd"
        reporter:   "spec"
        slow:       300
        compilers:  "coffee:coffee-script"
        require:    "coffee-script/register"

    options = for key, val of options
        "--#{key} #{val}"

    options = options.join " "

    mocha "#{options} #{tests}", silent:true, (code, output) ->
        console.log "test results:\n#{output}"

#-------------------------------------------------------------------------------

tasks.copymongomanifest = ->
    rm "manifest.yml"
    cp "mongo-manifest.yml", "manifest.yml"

#-------------------------------------------------------------------------------

tasks.copycloudantmanifest = ->
    rm "manifest.yml"
    cp "couch-manifest.yml", "manifest.yml"

#-------------------------------------------------------------------------------

tasks.createmongoservice = ->
    exec "cf create-service mongodb 100 todo-mongo-db" 

#-------------------------------------------------------------------------------

exports.createcloudantservice = 
    doc : "creates a Cloudant service"
    run : (username, password, url) ->
        exec 'cf cups todo-couch-db -p \'{"username":"' + username + '","password":"' + password + '","url":"' + url + '"}\''

#-------------------------------------------------------------------------------

exports.deploycloudant =
    doc : "deploys the ToDo app with Cloudant as the backend"
    run: (appName, username, password, url) ->
        tasks.build()
        tasks.copycloudantmanifest()
        exports.createcloudantservice.run(username, password, url)
        exec "cf push " + appName

#-------------------------------------------------------------------------------

exports.deploymongo =
    doc : "deploys the ToDo app with Mongo DB as the backend"
    run: (appName) ->
        tasks.build()
        tasks.copymongomanifest()
        tasks.createmongoservice()
        exec "cf push " + appName

#-------------------------------------------------------------------------------

watchIter = ->
    tasks.build()
    tasks.test()
    tasks.serve()

#-------------------------------------------------------------------------------
cleanDir = (dir) ->
    mkdir "-p", dir
    rm "-rf", "#{dir}/*"

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
