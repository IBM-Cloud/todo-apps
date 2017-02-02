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

import Foundation


public struct DatabaseConfiguration {

    public var host: String?
    public var port: UInt16?
    public var username: String?
    public var password: String?
    public var options = [String: AnyObject]()

    public init(host: String?, port: UInt16?, username: String?, password: String?) {
        self.host = host
        self.port = port
        self.username = username
        self.password = password
    }

}