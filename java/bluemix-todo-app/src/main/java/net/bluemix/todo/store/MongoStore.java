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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.bluemix.todo.model.ToDo;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * An implementation of {@link ToDoStore} backed by Mongo DB.
 */
public class MongoStore implements ToDoStore {

  private DBCollection coll;

  /**
   * Creates a ToDo store backed by Mongo DB.
   * @param coll The collection from the Mongo DB to use.
   */
  public MongoStore(DBCollection coll) {
    this.coll = coll;
  }

  @Override
  public Collection<ToDo> getAll() {
    List<ToDo> todos = new ArrayList<ToDo>();
    DBCursor cursor = coll.find();
    while(cursor.hasNext()) {
      todos.add(createToDo(cursor.next()));
    }
    cursor.close();
    return todos;
  }

  @Override
  public ToDo get(String id) {
    BasicDBObject query = new BasicDBObject("_id", id);
    DBCursor cursor = coll.find(query);
    if(cursor.hasNext()) {
      ToDo td = createToDo(cursor.next());
      cursor.close();
      return td;
    } else {
      return null;
    }
  }

  @Override
  public ToDo persist(ToDo td) {
    DBObject dbObj = convertToDbObject(td);
    coll.insert(dbObj);
    return createToDo(dbObj);
  }

  @Override
  public ToDo update(String id, ToDo td) {
    DBObject query = new BasicDBObject("_id", new ObjectId(id));
    DBObject update = convertToDbObject(td);
    coll.update(query, update);
    DBCursor cursor = coll.find(query);
    ToDo newTd = createToDo(cursor.next());
    cursor.close();
    return newTd;
  }

  @Override
  public void delete(String id) {
    DBObject query = new BasicDBObject("_id", new ObjectId(id));
    coll.remove(query);
  }

  private ToDo createToDo(DBObject dbObj) {
    ToDo td = new ToDo();
    td.setTitle((String)dbObj.get("title"));
    td.setCompleted((Boolean)dbObj.get("completed"));
    td.setId(((ObjectId)dbObj.get("_id")).toString());
    td.setOrder((Integer)dbObj.get("order"));
    return td;
  }

  private DBObject convertToDbObject(ToDo td) {
    DBObject dbObj = new BasicDBObject("title", td.getTitle()).
            append("completed", td.isCompleted()).append("order", td.getOrder());
    if(td.getId() != null) {
      dbObj.put("_id", new ObjectId(td.getId()));
    }
    return dbObj;
  }

  @Override
  public int count() throws ToDoStoreException {
    return getAll().size();
  }
}
