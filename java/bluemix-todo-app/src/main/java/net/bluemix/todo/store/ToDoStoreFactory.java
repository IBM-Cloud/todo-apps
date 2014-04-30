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

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * Creates an instance of the a {@link ToDoStore} to use.
 */
public class ToDoStoreFactory {
  private static final int PERIOD = 30; //in seconds
  private static ToDoStore instance;
  private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

  /**
   * Gets an instance of {@link ToDoStore}. 
   * @return A {@link ToDoStore}.
   */
  public static ToDoStore getInstance() throws ToDoStoreException {
    if(instance == null) {
      CloudEnvironment env = new CloudEnvironment();
      Map<String, Object> mongo = env.getServiceDataByName("todo-mongo-db");
      Map<String, Object> cloudant = env.getServiceDataByName("todo-couch-db");
      if(mongo != null && mongo.size() != 0) {
        instance = new MongoStore(getCollection(mongo));
      } else if(cloudant != null && cloudant.size() != 0){
        instance = new CloudantStore(getWebTarget(cloudant));
      } else {
        instance = new InMemoryStore();
      }
      exec.scheduleAtFixedRate(new Cleanup(), PERIOD, PERIOD, TimeUnit.SECONDS);
    }
    return instance;
  }
  
  private static DBCollection getCollection(Map<String, Object> mongoMap) throws ToDoStoreException {
    Map<String, Object> creds = (Map<String, Object>)mongoMap.get("credentials");
    String dbHost = (String)creds.get("host");
    String dbName = (String)creds.get("database");
    if(dbName == null) {
      dbName = (String)creds.get("db");
    }
    Object portObj = creds.get("port");
    String port;
    if(portObj instanceof Integer) {
      port = portObj.toString();
    } else {
      port = (String)portObj;
    }
    String username = (String) creds.get("username");
    String password = (String) creds.get("password");
    MongoClient client;
    try {
      client = new MongoClient(dbHost, Integer.valueOf(port));
      DB db = client.getDB(dbName);
      boolean auth = db.authenticate(username, password.toCharArray());
      if(!auth) {
        throw new ToDoStoreException("Could not authenticate to Mongo DB.");
      }
      return db.getCollection("todos");
    } catch (Exception e) {
      throw new ToDoStoreException("Error creating Mongo DB client.", e);
    }
  }
  
  private static WebTarget getWebTarget(Map<String, Object> cloudantMap) {
    Map<String, Object> creds = (Map<String, Object>)cloudantMap.get("credentials");
    String url = (String)creds.get("url");
    String username = (String)creds.get("username");
    String pw = (String)creds.get("password");
    HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basic(username, pw);
    Client client = ClientBuilder.newClient().register(basicAuthFeature);
    return client.target(url);
  }
}
