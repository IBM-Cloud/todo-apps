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
 * Represents an individual row of results returned when getting documents from the Cloudant DB.
 */
public class CloudantRow {
  private String id;
  private int key;
  private CloudantToDo doc;
  private ToDo value;
  
  /**
   * Gets the value.
   * @return The value.
   */
  public ToDo getValue() {
    return value;
  }

  /**
   * Sets the value.
   * @param value The value to set.
   */
  public void setValue(ToDo value) {
    this.value = value;
  }
  
  /**
   * Gets the key.
   * @return The key.
   */
  public int getKey() {
    return key;
  }

  /**
   * Sets the key.
   * @param key The key to set.
   */
  public void setKey(int key) {
    this.key = key;
  }

  /**
   * Gets the ID.
   * @return The ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the ID.
   * @param id The ID to set.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the ToDo document for the row.
   * @return The ToDo.
   */
  public CloudantToDo getDoc() {
    return doc;
  }
  
  /**
   * Sets the ToDo document for the row.
   * @param doc The ToDo document to set.
   */
  public void setDoc(CloudantToDo doc) {
    this.doc = doc;
  }
}