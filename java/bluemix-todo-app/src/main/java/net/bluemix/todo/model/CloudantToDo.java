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

import java.util.Arrays;

/**
 * Represents a ToDo document stored in Cloudant.
 */
public class CloudantToDo {
  
  private String _id;
  private String _rev;
  private String title;
  private boolean completed;
  private int order;

  /**
   * Default constructor, needed 
   */
  public CloudantToDo() {
    this.completed = false;
    this.title = "";
  }
  
  /**
   * Create a Cloudant ToDo from a general ToDo.
   * @param td The ToDo.
   */
  public CloudantToDo(ToDo td) {
    this._id = td.getId();
    this.order = td.getOrder();
    this.title = td.getTitle();
    this.completed = td.isCompleted();
  }
  
  /**
   * Gets the ID.
   * @return The ID.
   */
  public String get_id() {
    return _id;
  }
  
  /**
   * Sets the ID
   * @param _id The ID to set.
   */
  public void set_id(String _id) {
    this._id = _id;
  }
  
  /**
   * Gets the revision of the document.
   * @return The revision of the document.
   */
  public String get_rev() {
    return _rev;
  }
  
  /**
   * Sets the revision.
   * @param _rev The revision to set.
   */
  public void set_rev(String _rev) {
    this._rev = _rev;
  }
  
  /**
   * Gets the title.
   * @return The title.
   */
  public String getTitle() {
    return title;
  }
  
  /**
   * Sets the title.
   * @param title The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }
  
  /**
   * Indicates if the ToDo is completed.
   * @return True if the ToDo is complete, false otherwise.
   */
  public boolean isCompleted() {
    return completed;
  }
  
  /**
   * Sets the completed state of the ToDo.
   * @param completed True if the ToDo is completed, false otherwise.
   */
  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
  
  /**
   * Creates a general ToDo from the Cloudant ToDo
   * @return The general ToDo.
   */
  public ToDo getToDo() {
    ToDo td = new ToDo();
    td.setId(this.get_id());
    td.setCompleted(this.isCompleted());
    td.setTitle(this.getTitle());
    return td;
  }
  
  /**
   * Gets the order.
   * @return The order.
   */
  public int getOrder() {
    return order;
  }

  /**
   * Sets the order.
   * @param order The order to set.
   */
  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CloudantToDo) {
      CloudantToDo test = (CloudantToDo)obj;
      boolean result = _id == null ? _id == test.get_id() : _id.equals(test.get_id());
      result &= _rev == null ? _rev == test.get_rev() : _rev.equals(test.get_rev());
      result &= title == null ? title == test.getTitle() : title.equals(test.getTitle());
      result &= completed == test.isCompleted();
      result &= order == test.getOrder();
      return result;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[]{_id, _rev, title, completed, order});
  }
}

