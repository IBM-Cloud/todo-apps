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

/**
 * An error that occurs in the ToDo store.
 */
public class ToDoStoreException extends Exception {
  private static final long serialVersionUID = 673164517448334112L;

  /**
   * Creates a new ToDo error.
   * @param msg The message describing the error.
   */
  public ToDoStoreException(String msg) {
    super(msg);
  }

  /**
   * Creates a new ToDo error.
   * @param msg The message describing the error.
   * @param t The throwable that occurred.
   */
  public ToDoStoreException(String msg, Throwable t) {
    super(msg, t);
  }
}
