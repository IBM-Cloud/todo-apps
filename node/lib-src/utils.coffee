# Licensed under the Apache License. See footer for details.

pkg = require "../package.json"

#-------------------------------------------------------------------------------
# global verbose logging setting
#-------------------------------------------------------------------------------
VERBOSE = false

#-------------------------------------------------------------------------------
# reference exports as `utils`, set some constants from the packge.json
#-------------------------------------------------------------------------------
utils = exports

utils.PROGRAM     = pkg.name
utils.VERSION     = pkg.version
utils.DESCRIPTION = pkg.description

#-------------------------------------------------------------------------------
# log a message
#-------------------------------------------------------------------------------
utils.log = (message) ->
    console.log "#{utils.PROGRAM}: #{message}"

#-------------------------------------------------------------------------------
# log a message and then exit
#-------------------------------------------------------------------------------
utils.logError = (message) ->
    utils.log message
    process.exit 1

#-------------------------------------------------------------------------------
# log a message when VERBOSE is true
#-------------------------------------------------------------------------------
utils.vlog = (message) ->
    return unless VERBOSE

    utils.log message

#-------------------------------------------------------------------------------
# set VERBOSE on and off
#-------------------------------------------------------------------------------
utils.verbose = (value) ->
    VERBOSE = !!value

#-------------------------------------------------------------------------------
# make a JSONable object super readable
#-------------------------------------------------------------------------------
utils.JL = (object) ->
    JSON.stringify object, null, 4

#-------------------------------------------------------------------------------
# make a JSONable object kinda readable
#-------------------------------------------------------------------------------
utils.JS = (object) ->
    JSON.stringify object

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
