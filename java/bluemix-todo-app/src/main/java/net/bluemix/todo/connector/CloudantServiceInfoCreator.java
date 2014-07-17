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
package net.bluemix.todo.connector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

/**
 * Creates an instance of the Cloudant user provided service when bound to the application.  The
 * Spring Cloud library will automatically create an instance of this class and call the createServiceInfo
 * method as long as it is listed in the src/main/resources/META-INF/service/org.springframework.cloud.cloudfoundry.CloudFoundry
 * file.
 */
public class CloudantServiceInfoCreator extends CloudFoundryServiceInfoCreator<CloudantServiceInfo> {
  
  /**
   * Creates a new instance of the creator.
   */
  public CloudantServiceInfoCreator() {
    super(new Tags(), "cloudant");
  }

  @Override
  public CloudantServiceInfo createServiceInfo(Map<String, Object> serviceData) {
    Map<String, Object> credentials = (Map<String, Object>) serviceData.get("credentials");
    String id = (String)serviceData.get("name");
    try {
      URI uri = new URI((String)credentials.get("url"));
      String scheme = uri.getScheme();
      int port = uri.getPort();
      String host = uri.getHost();
      String path = uri.getPath();
      String query = uri.getQuery();
      String fragment = uri.getFragment();
      String url = new URI(scheme, "", host, port, path, query, fragment).toString();
      String[] userInfo = uri.getUserInfo().split(":");
      return new CloudantServiceInfo(id, userInfo[0], userInfo[1], url);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  @Override
  public boolean accept(Map<String, Object> serviceData) {
    String name = (String)serviceData.get("name");
    return "todo-couch-db".equals(name);
  }
}

