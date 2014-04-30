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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.bluemix.todo.model.CloudantAllDocs;
import net.bluemix.todo.model.CloudantCount;
import net.bluemix.todo.model.CloudantPostResponse;
import net.bluemix.todo.model.CloudantRow;
import net.bluemix.todo.model.CloudantToDo;
import net.bluemix.todo.model.ToDo;

/**
 * A {@link ToDoStore} using Cloudant.
 */
public class CloudantStore implements ToDoStore {

  private static final String REVISION_PARAM = "rev";
  private static final String DESIGN_DOC_NAME = "todos";
  private static final String VIEW_NAME = "allTodos";
  //This JSON represents the design document containing a view we will use when retrieving Todos
  private static final String DESIGN_DOC = 
          "{" +
            "\"views\": {" +
              "\"allTodos\": {" +
                "\"reduce\": \"_count\"," +
                "\"map\": \"function(doc){if(doc.title && doc.completed != null){emit(doc.order,{title: doc.title,completed: doc.completed})}}\"" +
              "}" +
            "}" +
           "}";
  
  private static final Logger LOG = Logger.getLogger(CloudantStore.class.getName());
  
  private WebTarget target;
  
  /**
   * Creates a CloudantStore.
   * @param target The target (URL) for the CloudantStore.
   * @throws ToDoStoreException 
   */
  public CloudantStore(WebTarget target) throws ToDoStoreException {
    //Uncomment to enable HTTP logging in Jersey
    //target = target.register(new LoggingFilter(LOG, true));
    createDB(target);
    this.target = target.path("bluemix-todo");
    createDesignDoc(this.target);
  }
  
  /**
   * Will create a database named "todos" if one does not exist.
   * @param target The base URL for Cloudant.
   * @throws ToDoStoreException Thrown if there is an error creating the DB.
   */
  private void createDB(WebTarget target) throws ToDoStoreException {
    Response resp = target.path("bluemix-todo").request(MediaType.APPLICATION_JSON).get();;
    int status = resp.getStatus();
    if(status == HttpURLConnection.HTTP_NOT_FOUND) {
      resp = target.path("bluemix-todo").request(MediaType.APPLICATION_JSON).put(Entity.text(""));
      status = resp.getStatus();
      if(status != HttpURLConnection.HTTP_CREATED && status != HttpURLConnection.HTTP_ACCEPTED) {
        throw new ToDoStoreException("Error creating the ToDo database.");
      }
    }
  }
  
  /**
   * Will create a design document called "todos" if one does not exist.
   * @param target The base URL for Cloudant.
   * @throws ToDoStoreException Thrown if there is an error creating the design document.
   */
  private static void createDesignDoc(WebTarget target) throws ToDoStoreException {
    WebTarget design = target.path("_design").path("todos");
    Response resp = design.request(MediaType.APPLICATION_JSON).get();
    int status = resp.getStatus();
    if(status == HttpURLConnection.HTTP_NOT_FOUND) {
      resp = design.request(MediaType.APPLICATION_JSON).put(Entity.json(DESIGN_DOC));
      status = resp.getStatus();
      if(status != HttpURLConnection.HTTP_CREATED) {
        throw new ToDoStoreException("Error creating the ToDo design document.");
      }
    }
  }

  @Override
  public Collection<ToDo> getAll() throws ToDoStoreException {
    Response allDocsResp = target.queryParam("reduce", false).
            path("_design").path(DESIGN_DOC_NAME).path("_view").path(VIEW_NAME).
            request(MediaType.APPLICATION_JSON).get();
    int status = allDocsResp.getStatus();
    if(status == HttpURLConnection.HTTP_OK) {
      List<ToDo> todos = new ArrayList<ToDo>();
      CloudantAllDocs all = allDocsResp.readEntity(CloudantAllDocs.class);
      for(CloudantRow row : all.getRows()) {
        ToDo td = row.getValue();
        td.setId(row.getId());
        td.setOrder(row.getKey());
        todos.add(td);
      }
      return todos;
    } else {
      throw new ToDoStoreException("There was an error retrieving the tasks from Cloudant. Error "
              + status);
    } 
  }

  @Override
  public ToDo get(String id) throws ToDoStoreException {
    Response docResp = getRequest(id);
    int status = docResp.getStatus();
    if(status == HttpURLConnection.HTTP_OK) {
      CloudantToDo td = docResp.readEntity(CloudantToDo.class);      
      return td.getToDo();
    } else {
      throw new ToDoStoreException("There was an error retrieving the doc with id " + id 
              + "from Cloudant. Error " + status);
    }
  }
  
  private Response getRequest(String id) {
    return target.path(id).request(MediaType.APPLICATION_JSON).get();
  }

  @Override
  public ToDo persist(ToDo td) throws ToDoStoreException {
    Response newdoc = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(td, MediaType.APPLICATION_JSON));
    int status = newdoc.getStatus();
    if(status == HttpURLConnection.HTTP_CREATED) {
      CloudantPostResponse post = newdoc.readEntity(CloudantPostResponse.class);
      td.setId(post.getId());
      return td;
    } else {
      throw new ToDoStoreException("There was an error POSTing the ToDo to Cloudant. Error " 
              + status);
    }
  }

  @Override
  public ToDo update(String id, ToDo td) throws ToDoStoreException {
    Response docResp = getRequest(id);
    int status = docResp.getStatus();
    if(status == HttpURLConnection.HTTP_OK) {
      CloudantToDo ctd = docResp.readEntity(CloudantToDo.class);
      CloudantToDo updatedCtd = new CloudantToDo(td);
      updatedCtd.set_rev(ctd.get_rev());
      Response updateReq = target.queryParam(REVISION_PARAM, ctd.get_rev()).path(id).
              request(MediaType.APPLICATION_JSON).put(Entity.entity(updatedCtd, MediaType.APPLICATION_JSON));
      status = updateReq.getStatus();
      if(status == HttpURLConnection.HTTP_CREATED) {
        CloudantPostResponse post = updateReq.readEntity(CloudantPostResponse.class);
        td.setId(post.getId());
        return td;
      } else {
        throw new ToDoStoreException("There was an error POSTing the ToDo to Cloudant. Error "
                + status);
      }
    } else {
      throw new ToDoStoreException("there was an error fetching the ToDo from Cloudant. Error " + status);
    }
  }

  @Override
  public void delete(String id) throws ToDoStoreException {
    Response docResp = getRequest(id);
    int status = docResp.getStatus();
    if(status == HttpURLConnection.HTTP_OK) {
      CloudantToDo ctd = docResp.readEntity(CloudantToDo.class);
      Response updateReq = target.queryParam(REVISION_PARAM, ctd.get_rev()).path(id).
              request(MediaType.APPLICATION_JSON).delete();
      status = updateReq.getStatus();
      if(status != HttpURLConnection.HTTP_OK) {
        throw new ToDoStoreException("There was an error deleting the ToDo from Cloudant. Error "
                + status);
      }
    } else {
      throw new ToDoStoreException("There was an error getting the ToDo from Cloudant. Error " + status);
    }
  }
  
  @Override
  public int count() throws ToDoStoreException {
    Response allDocsResp = target.path("_design").path(DESIGN_DOC_NAME).path("_view").path(VIEW_NAME).
            request(MediaType.APPLICATION_JSON).get();
    int status = allDocsResp.getStatus();
    if(status == HttpURLConnection.HTTP_OK) {
      CloudantCount count = allDocsResp.readEntity(CloudantCount.class);
      if(count.getRows().size() > 0) {
        return count.getRows().get(0).getValue();
      } else {
        //No rows means there are no documents in the view.
        return 0;
      }
    } else {
      throw new ToDoStoreException("There was an error retrieving the tasks from Cloudant. Error "
              + status);
    } 
  }
}