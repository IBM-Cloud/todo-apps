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

import LoggerAPI
import SwiftyJSON

import TodoListAPI

import Credentials
import CredentialsFacebookToken

final class TodoListController {

    let todos: TodoListAPI
    let router = Router()

    let credentialsMiddleware = Credentials()
    let fbCredentialsPlugin = CredentialsFacebookToken()

    init(backend: TodoListAPI) {

        self.todos = backend

        credentialsMiddleware.register(plugin: fbCredentialsPlugin)

        setupRoutes()

    }

    private func setupRoutes() {

        let id = "\(config.firstPathSegment)/:id"

        router.all("/*", middleware: BodyParser())
        router.all("/*", middleware: AllRemoteOriginMiddleware())
        router.get("/api/todos", handler: onGetTodos)
        router.get(id, handler: onGetByID)
        router.options("/*", handler: onGetOptions)
        router.post("/api/todos", handler: onAddItem )
        router.post(id, handler: onUpdateByID)
        router.patch(id, handler: onUpdateByID)
        router.delete(id, handler: onDeleteByID)
        router.delete("/api/todos", handler: onDeleteAll)
    }

    private func onGetTodos(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        todos.get() {
            todos, error in
            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }
                if let todos = todos {
                    let json = JSON(todos.toDictionary())
                    try response.status(HTTPStatusCode.OK).send(json: json).end()
                } else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                }
            } catch {
                Log.error("Communication error")
            }
        }

    }

    private func onGetByID(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        guard let id = request.params["id"] else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("Request does not contain ID")
            return
        }

        todos.get(withDocumentID: id) {
            item, error in

            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }
                if let item = item {
                    let result = JSON(item.toDictionary())

                    try response.status(HTTPStatusCode.OK).send(json: result).end()

                } else {
                    Log.warning("Could not find the item")
                    response.status(HTTPStatusCode.badRequest)
                    return
                }
            } catch {
                Log.error("Communication error")
            }
        }
    }

    /**
     */
    private func onGetOptions(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        response.headers["Access-Control-Allow-Headers"] = "accept, content-type"
        response.headers["Access-Control-Allow-Methods"] = "GET,HEAD,POST,DELETE,OPTIONS,PUT,PATCH"

        response.status(HTTPStatusCode.OK)

        next()

    }

    /**
     */
    private func onAddItem(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        guard let body = request.body else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("No body found in request")
            return
        }

        guard case let .json(json) = body else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("Body contains invalid JSON")
            return
        }

        let title = json["title"].stringValue
        let order = json["order"].intValue
        let completed = json["completed"].boolValue

        guard title != "" else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("Request does not contain valid title")
            return
        }

        todos.add(title: title, order: order, completed: completed) {
            newItem, error in

            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }

                guard let item = newItem else {
                    response.status(HTTPStatusCode.badRequest)
                    Log.error("Request does not contain valid title")
                    return
                }

                let result = JSON(item.toDictionary())

                Log.info("Added \(title) to the TodoList")

                do {
                    try response.status(HTTPStatusCode.OK).send(json: result).end()
                } catch {
                    Log.error("Error sending response")
                }
            } catch {
                Log.error("Communication error")

            }

        }

    }

    private func onUpdateByID(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        guard let documentID = request.params["id"] else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("id parameter not found in request")
            return
        }

        guard let body = request.body else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("No body found in request")
            return
        }

        guard case let .json(json) = body else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("Body contains invalid JSON")
            return
        }

        let title = json["title"].stringValue
        let order = json["order"].intValue
        let completed = json["completed"].boolValue

        guard title != "" else {
            response.status(HTTPStatusCode.badRequest)
            Log.error("Request does not contain valid title")
            return
        }

        todos.update(documentID: documentID, title: title, order: order, completed: completed) {
            newItem, error in

            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }
                if let newItem = newItem {
                    let result = JSON(newItem.toDictionary())
                    try response.status(HTTPStatusCode.OK).send(json: result).end()
                } else {
                    Log.error("Database returned invalid new item")
                    try response.status(HTTPStatusCode.badRequest).end()
                }
            } catch {
                Log.error("Communication error")
            }

        }

    }

    private func onDeleteByID(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        guard let documentID = request.params["id"] else {
            Log.warning("Could not parse ID")
            response.status(HTTPStatusCode.badRequest)
            return
        }

        todos.delete(withDocumentID: documentID) {
            error in

            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }
                try response.status(HTTPStatusCode.OK).end()
                Log.info("Deleted document \(documentID)")
            } catch {
                Log.error("Could not produce response")
            }

        }

    }

    private func onDeleteAll(request: RouterRequest, response: RouterResponse, next: () -> Void) {

        todos.clear() {
            error in
            
            do {
                guard error == nil else {
                    try response.status(HTTPStatusCode.badRequest).end()
                    Log.error(error.debugDescription)
                    return
                }
                try response.status(HTTPStatusCode.OK).end()
                Log.info("Deleted all the documents")
            } catch {
                Log.error("Could not produce response")
            }
        }

    }


}