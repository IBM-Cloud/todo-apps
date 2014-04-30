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
 * Represents a row with the reduced value.
 */
public class CloudantReducedRow {
  private String key;
  private int value;
  
  /**
   * Gets the key.
   * @return The key.
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Sets the key.
   * @param key The key to set.
   */
  public void setKey(String key) {
    this.key = key;
  }
  
  /**
   * Gets the value.
   * @return The value.
   */
  public int getValue() {
    return value;
  }
  
  /**
   * Sets the value.
   * @param value The value to set.
   */
  public void setValue(int value) {
    this.value = value;
  }

}

