/**
 * Copyright IBM Corporation 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

import Kitura
import KituraNet
import KituraSys

import HeliumLogger
import LoggerAPI

import TodoListAPI
import TodoListCouchDB

import Foundation

Log.logger = HeliumLogger()

let config = Configuration.sharedInstance
config.loadCloudFoundry()

let todos: TodoListCouchDB
if let dbConfig = config.databaseConfiguration {
    todos = TodoListCouchDB(dbConfig)
} else {
    todos = TodoListCouchDB()
}

//todos.setup() {
//    error in 
//}

let controller = TodoListController(backend: todos)

let server = HTTPServer.listen(port: config.port, delegate: controller.router)
Server.run()
Log.info("Server is started on \(config.url).")