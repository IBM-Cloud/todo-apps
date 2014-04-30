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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.bluemix.todo.model.ToDo;

/**
 * An implementation of {@link ToDoStore} that stores ToDos in memory.
 *
 */
public class InMemoryStore implements ToDoStore {

  private Map<String, ToDo> store;

  /**
   * Creates a new ToDo store.
   */
  public InMemoryStore() {
    this.store = Collections.synchronizedMap(new HashMap<String, ToDo>());
  }

  @Override
  public Collection<ToDo> getAll() {
    return store.values();
  }

  @Override
  public ToDo get(String id) {
    return store.get(id);
  }

  @Override
  public ToDo persist(ToDo td) {
    td.setId(UUID.randomUUID().toString());
    synchronized(td) {
      store.put(td.getId(), td);
    }
    return td;
  }

  @Override
  public ToDo update(String id, ToDo td) {
    synchronized(store) {
      ToDo old = store.get(id);
      if(old == null) {
        return null;
      } else {
        old.setCompleted(td.isCompleted());
        old.setTitle(td.getTitle());
      }
      store.put(id, old);
      return old;
    }
  }

  @Override
  public void delete(String id) {
    synchronized(store) {
      store.remove(id);
    }
  }

  @Override
  public int count() throws ToDoStoreException {
    return store.size();
  }
}