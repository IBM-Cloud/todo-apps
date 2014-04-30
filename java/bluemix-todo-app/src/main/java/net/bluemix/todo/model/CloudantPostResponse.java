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
package net.bluemix.todo.model;

/**
 * Represents the response returned from POSTing a new document.
 */
public class CloudantPostResponse {
  
  private String id;
  private boolean ok;
  private String rev;
  
  /**
   * Gets the ID of the newly created document.
   * @return The ID of the newly created document.
   */
  public String getId() {
    return id;
  }
  
  /**
   * Sets the ID of response.
   * @param id The ID to set.
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * Indicates if the POST was OK.
   * @return True if the POST was OK, false otherwise.
   */
  public boolean isOk() {
    return ok;
  }
  
  /**
   * Sets the OK field.
   * @param ok The OK response.
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }
  
  /**
   * Gets the revision.
   * @return The revision.
   */
  public String getRev() {
    return rev;
  }
  
  /**
   * Sets the revision.
   * @param rev The revision to set.
   */
  public void setRev(String rev) {
    this.rev = rev;
  }
}