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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import net.bluemix.todo.connector.CloudantServiceInfo;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MongoServiceInfo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * Creates an instance of the a {@link ToDoStore} to use.
 */
public class ToDoStoreFactory {
  private static final int PERIOD = 30; //in seconds
  private static ToDoStore instance;
  private static CloudFactory cloudFactory;
  private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

  /**
   * Gets an instance of {@link ToDoStore}. 
   * @return A {@link ToDoStore}.
   */
  public static ToDoStore getInstance() throws ToDoStoreException {
    if(instance == null) {
      cloudFactory = new CloudFactory();
      try {
        Cloud cloud = cloudFactory.getCloud();
        List<ServiceInfo>infos = cloud.getServiceInfos();
        MongoServiceInfo mongoInfo = null;
        CloudantServiceInfo cloudantInfo = null;
        for(ServiceInfo info : infos) {
          if(info.getId().equals("todo-mongo-db")) {
            mongoInfo = (MongoServiceInfo)info;
            break;
          }
          if(info.getId().equals("todo-couch-db")) {
            cloudantInfo = (CloudantServiceInfo)info;
            break;
          }
        }
        if(mongoInfo != null) {
          instance = new MongoStore(getCollection(mongoInfo));
        } else if(cloudantInfo != null) {
          instance = new CloudantStore(getWebTarget(cloudantInfo));
        } else {
          instance = new InMemoryStore();
        }
      } catch(CloudException e) {
        instance = new InMemoryStore();
      }
      exec.scheduleAtFixedRate(new Cleanup(), PERIOD, PERIOD, TimeUnit.SECONDS);
    }
    return instance;
  }
  
  private static DBCollection getCollection(MongoServiceInfo info) throws ToDoStoreException {
    MongoClient client;
    try {
      client = new MongoClient(info.getHost(), info.getPort());
      DB db = client.getDB(info.getDatabase());
      boolean auth = db.authenticate(info.getUserName(), info.getPassword().toCharArray());
      if(!auth) {
        throw new ToDoStoreException("Could not authenticate to Mongo DB.");
      }
      return db.getCollection("todos");
    } catch (Exception e) {
      throw new ToDoStoreException("Error creating Mongo DB client.", e);
    }
  }
  
  private static WebTarget getWebTarget(CloudantServiceInfo info) {
    HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basic(info.getUsername(), info.getPassword());
    Client client = ClientBuilder.newClient().register(basicAuthFeature);
    return client.target(info.getUrl());
  }
}
