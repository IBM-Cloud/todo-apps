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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a ToDo item.
 */
@XmlRootElement
public class ToDo {
  private boolean completed;
  private String title;
  private String id;
  private int order;

  /**
   * Creates a new ToDo.
   */
  public ToDo() {
    this.title = "";
    this.completed = false;
  }

  /**
   * Gets the ToDo ID.
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
   * Indicates if the ToDo is completed.
   * @return True if the ToDo is completed, false if it is not completed.
   */
  public boolean isCompleted() {
    return completed;
  }

  /**
   * Sets the ToDo to completed.
   * @param completed The completed state.
   */
  public void setCompleted(boolean completed) {
    this.completed = completed;
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
    if(obj instanceof ToDo) {
      ToDo test = (ToDo)obj;
      boolean result = false;
      result = id == null ? id == test.getId() : id.equals(test.getId());
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
    return Arrays.hashCode(new Object[]{id, title, completed, order});
  }
}
