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

import org.springframework.cloud.service.BaseServiceInfo;

/**
 * Represents the properties of the Cloudant user provided service.
 */
public class CloudantServiceInfo extends BaseServiceInfo {  
  private String username;
  private String password;
  private String url;

  /**
   * Creates a Cloudant Service.
   * @param id The id of the service.
   * @param username The Cloudant username.
   * @param password The Cloudant password.
   * @param url The Cloudant URL.
   */
  public CloudantServiceInfo(String id, String username, String password, String url) {
    super(id);
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * Gets the username.
   * @return The username.
   */
  @ServiceProperty
  public String getUsername() {
    return username;
  }
  
  /**
   * Gets the password.
   * @return The password.
   */
  @ServiceProperty
  public String getPassword() {
    return password;
  }
  
  /**
   * Gets the URL.
   * @return The URL.
   */
  @ServiceProperty
  public String getUrl() {
    return url;
  }
}

