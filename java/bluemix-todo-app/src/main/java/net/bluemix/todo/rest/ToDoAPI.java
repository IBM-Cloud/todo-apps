/*
 * Copyright IBM Corp. 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bluemix.todo.rest;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.bluemix.todo.model.ToDo;
import net.bluemix.todo.store.ToDoStore;
import net.bluemix.todo.store.ToDoStoreException;
import net.bluemix.todo.store.ToDoStoreFactory;

/**
 * REST API for performing basic CRUD operations on TODOs.
 */
@Path("todos")
public class ToDoAPI {

  private ToDoStore store;

  /**
   * Default constructor.  This is used by the Jersey framework.
   * @throws ToDoStoreException Thrown if a store cannot be retrieved.
   */
  public ToDoAPI() throws ToDoStoreException {
    this.store = ToDoStoreFactory.getInstance();
  }

  /**
   * Constructor.  Used mainly for testing purposes.
   * @param store The ToDo store to use.
   */
  public ToDoAPI(ToDoStore store) {
    this.store = store;
  }

  /**
   * Gets a given ToDo.
   * REST API example: 
   * <code>
   * GET http://localhost:8080/api/todos/123
   * </code>
   * 
   * Response:
   * <code>
   * {
   *   "completed":false,
   *   "id":"1393339172666",
   *   "title":"get the kids"
   * }
   * </code>
   * @param id The ID of the ToDo.
   * @return The ToDo for the given ID.
   */
  @GET @Path("/{id}")
  @Produces("application/json")
  public ToDo getToDo(@PathParam("id") String id) {
    if(id == null) {
      throw new WebApplicationException("Must supply an ID, for example /api/todos/123.", 
              Response.Status.BAD_REQUEST);
    }
    try {
      ToDo td = store.get(id);
      if(td == null) {
        throw new WebApplicationException("ToDo with the ID " + id + " does not exist.",
                Response.Status.BAD_REQUEST);
      }
      return td;
    } catch (ToDoStoreException e) {
      throw new WebApplicationException("Error getting ToDo.", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets all ToDos.
   * REST API example:
   * <code>
   * GET http://localhost:8080/api/todos
   * </code>
   * 
   * Response:
   * <code>
   * [
   *   {
   *     "completed":false,
   *     "id":"1393339172666",
   *     "title":"get the kids"
   *   },
   *   {
   *     "completed":false,
   *     "id":"123",
   *     "title":"pick up milk"
   *   }
   * ]
   * </code>
   * @return A collection of all the ToDos
   */
  @GET
  @Produces("application/json")
  public Collection<ToDo> getToDos() {
    try {
      return store.getAll();
    } catch (ToDoStoreException e) {
      throw new WebApplicationException("Error getting all ToDos.", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Creates a new ToDo.
   * 
   * REST API example:
   * <code>
   * POST http://localhost:8080/api/todos
   * <code>
   * POST Body:
   * <code>
   * {
   *   "title":"pick up milk"
   * }
   * </code>
   * Response:
   * <code>
   * {
   *   "completed":false,
   *   "id":"123",
   *   "title":"pick up milk"
   * }
   * </code>
   * @param td The new ToDo to create.
   * @return The ToDo after it has been stored.  This will include a unique ID for the ToDO.
   */
  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public ToDo newToDo(ToDo td) {
    if(td == null) {
      throw new WebApplicationException("Must supply a ToDo in the POST body.", 
              Response.Status.BAD_REQUEST);
    }
    try {
      return store.persist(td);
    } catch (ToDoStoreException e) {
      throw new WebApplicationException("Error saving ToDo.", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates a ToDo.
   * 
   * REST API example:
   * <code>
   * PUT http://localhost:8080/api/todos/456
   * </code>
   * PUT Body:
   * <code>
   * {
   *   "completed":false,
   *   "id":"123",
   *   "title":"pick up milk"
   * }
   * </code>
   * Response:
   * <code>
   * {
   *   "completed":false,
   *   "id":"456",
   *   "title":"pick up milk"
   * }
   * </code>
   * @param id The ID of the ToDo to update.
   * @param td The data for the ToDo to be updated with.
   * @return The updated ToDo.
   */
  @PUT @Path("/{id}")
  @Produces("application/json")
  @Consumes("application/json")
  public ToDo updateToDo(@PathParam("id") String id, ToDo td) {
    if(id == null) {
      throw new WebApplicationException("Must supply an ID, for example /api/todos/123.", 
              Response.Status.BAD_REQUEST);
    }
    if(td == null) {
      throw new WebApplicationException("Must supply a ToDo in the PUT body.", 
              Response.Status.BAD_REQUEST);
    }
    try {
      ToDo updatedTd = store.update(id, td);
      if(updatedTd == null) {
        throw new WebApplicationException("The ToDo with the ID " + id + " does not exist.",
                Response.Status.BAD_REQUEST);
      }
      return updatedTd;
    } catch (ToDoStoreException e) {
      throw new WebApplicationException("Error updating ToDo.", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes a ToDo.
   * 
   * REST API example:
   * <code>
   * DELETE http://localhost:8080/api/todos/456
   * </code>
   * @param id The ID of the ToDo to delete.
   */
  @DELETE @Path("/{id}")
  public void deleteToDo(@PathParam("id") String id) {
    if(id == null) {
      throw new WebApplicationException("Must supply an ID, for example /api/todos/123,",
              Response.Status.BAD_REQUEST);
    }
    try {
      store.delete(id);
    } catch (ToDoStoreException e) {
      throw new WebApplicationException("Error deleting ToDo.", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}