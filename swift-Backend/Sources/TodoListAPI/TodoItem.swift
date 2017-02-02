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

public struct TodoItem {

    /// ID
    public let documentID: String

    // Order
    public let order: Int

    /// Text to display
    public let title: String

    /// Whether completed or not
    public let completed: Bool

    public init(documentID: String, order: Int, title: String, completed: Bool) {
        self.documentID = documentID
        self.order = order
        self.title = title
        self.completed = completed
    }

}


extension TodoItem : Equatable { }

public func == (lhs: TodoItem, rhs: TodoItem) -> Bool {
    return lhs.documentID == rhs.documentID && lhs.order == rhs.order &&
        lhs.title == rhs.title && lhs.completed == rhs.completed

}