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
package net.bluemix.todo.store;

import java.util.Collection;

import net.bluemix.todo.model.ToDo;

/**
 * Defines the API for a ToDo store.
 *
 */
public interface ToDoStore {

  /**
   * Gets all ToDos from the store.
   * @return All ToDos.
   * @throws ToDoStoreException Thrown if there is an error getting the ToDos from the store.
   */
  public Collection<ToDo> getAll() throws ToDoStoreException;

  /**
   * Gets an individual ToDo from the store.
   * @param id The ID of the ToDo to get.
   * @return The ToDo.
   * @throws ToDoStoreException Thrown if there is an error getting the ToDo.
   */
  public ToDo get(String id) throws ToDoStoreException;

  /**
   * Persists a ToDo to the store.
   * @param td The ToDo to persist.
   * @return The persisted ToDo.  The ToDo will not have a unique ID.
   * @throws ToDoStoreException Thrown if there is an error persisting the ToDo.
   */
  public ToDo persist(ToDo td) throws ToDoStoreException;

  /**
   * Updates a ToDo in the store.
   * @param id The ID of the ToDo to update.
   * @param td The ToDo with updated information.
   * @return The updated ToDo.
   * @throws ToDoStoreException Thrown if there is an error updating the ToDo.
   */
  public ToDo update(String id, ToDo td) throws ToDoStoreException;

  /**
   * Deletes a ToDo from the store.
   * @param id The ID of the ToDo to delete.
   * @throws ToDoStoreException Thrown if there is an error deleting a ToDo.
   */
  public void delete(String id) throws ToDoStoreException;
  
  /**
   * Counts the number of ToDos
   * @return The total number of ToDos.
   * @throws ToDoStoreException Thrown if we cannot get a count.
   */
  public int count() throws ToDoStoreException;
}
