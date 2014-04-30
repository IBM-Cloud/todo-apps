# Licensed under the Apache License. See footer for details.

_    = require "underscore"
nopt = require "nopt"

utils  = require "./utils"
server = require "./server"

cli = exports

#-------------------------------------------------------------------------------
exports.main = ->

    # set up our command-line options options
    options =
        port:    [ "p", Number  ]
        verbose: [ "v", Boolean ]
        help:    [ "h", Boolean ]
        db:      [ "d", String]

    shortOptions = "?": ["--help"]
    for optionName, optionRec of options
        if optionRec[0] isnt ""
            shortOptions[optionRec[0]] = ["--#{optionName}"]

    for optionName, optionRec of options
        options[optionName] = optionRec[1]

    # parse the command-line
    parsed = nopt options, shortOptions, process.argv, 2

    args = parsed.argv.remain

    # check if help requested
    return help() if args[0] in ["?", "help"]
    return help() if parsed.help

    # build options from command-line and environment
    envOptions = {}
    envOptions.port = process.env.PORT if process.env.PORT?

    options = _.defaults envOptions, parsed, {db : 'cloudant'}

    # start the server
    server.start options

#-------------------------------------------------------------------------------
help = ->
    console.log """
        #{utils.PROGRAM} #{utils.VERSION}

            #{utils.DESCRIPTION}

        usage: #{utils.PROGRAM} [options]

        options:
            -p --port NUMBER     tcp/ip to run server on
            -v --verbose         be verbose
            -h --help            print this help

        The port can also be specified by setting the PORT environment variable.
    """

#-------------------------------------------------------------------------------
exports.main() if require.main is module

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
