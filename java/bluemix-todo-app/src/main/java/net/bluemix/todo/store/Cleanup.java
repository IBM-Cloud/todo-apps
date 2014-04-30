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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.bluemix.todo.model.ToDo;

/**
 * Handles deleting ToDos from the DB after they get over a certain number.
 */
public class Cleanup implements Runnable {
  
  private static final int MAX_TODOS = 30;
  private static final Logger LOG = Logger.getLogger(Cleanup.class.getName());

  @Override
  public void run() {
    try {
      //This code is very much not safe from underlying changes in the DB.
      //All we are trying to do here is to make sure no one is being mean an DOSing the DB
      ToDoStore store = ToDoStoreFactory.getInstance();
      if(store.count() >= MAX_TODOS) {
        Collection<ToDo> todos = store.getAll();
        if(todos.size() > 0) {
          store.delete(todos.iterator().next().getId());
        }
      }
    } catch (ToDoStoreException e) {
      LOG.logp(Level.WARNING, Cleanup.class.getName(), "run", "Error running cleanup.", e);
    } 
  }
}

