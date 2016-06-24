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
 */

import Foundation
import CloudFoundryEnv
import SwiftyJSON

import LoggerAPI

import TodoListAPI

public final class Configuration {

    let firstPathSegment = "api/todos"

    static let RedisServiceName = "*Redis*"
    static let CloudantServiceName = "todo-couch-db"

    static let DefaultWebHost = "localhost"
    static let DefaultWebPort = 8090

    var url: String = Configuration.DefaultWebHost
    var port: Int = Configuration.DefaultWebPort

    var databaseConfiguration: DatabaseConfiguration?

    static var sharedInstance = Configuration()

    private init() {

    }

    public func loadCloudFoundry() {
        do {
            try loadWebConfig()
            // try loadRedisConfig()
            try loadCloudantConfig()
        } catch _ {
            Log.error("Could not retrieve CF environment.")
        }

    }

    private func loadWebConfig() throws {
        let appEnv = try CloudFoundryEnv.getAppEnv()
        port = appEnv.port
        url = appEnv.url
    }

    private func loadRedisConfig() throws {
        if let redisService = try CloudFoundryEnv.getAppEnv().getService(spec:
            Configuration.RedisServiceName) {

            Log.info("Found Redis service named \(redisService.name)")

            if let credentials = redisService.credentials {
                let host = credentials["public_hostname"].stringValue
                let port = UInt16(credentials["username"].stringValue)!
                let password = credentials["password"].stringValue

                databaseConfiguration = DatabaseConfiguration(host: host, port: port,
                                                              username: nil, password: password)
            }

        } else {
            Log.info("Could not find Bluemix Redis service.")
        }
    }

    private func loadCloudantConfig() throws {
        if let service = try CloudFoundryEnv.getAppEnv().getService(spec:
            Configuration.CloudantServiceName) {

            Log.info("Found Cloudant service named \(service.name)")

            if let credentials = service.credentials {
                let host = credentials["host"].stringValue
                let username = credentials["username"].stringValue
                let password = credentials["password"].stringValue
                let port = UInt16(credentials["port"].stringValue)!

                databaseConfiguration = DatabaseConfiguration(host: host, port: port,
                                                              username: username, password: password )
            }

        } else {
            Log.info("Could not find Bluemix Cloudant service")
        }

    }
}
