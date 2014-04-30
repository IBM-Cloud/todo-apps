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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloudantRowTest {
  private CloudantToDo ctd;

  @Before
  public void setUp() throws Exception {
    ctd = new CloudantToDo();
    ctd.set_id("123");
    ctd.set_rev("abc");
    ctd.setTitle("This is a test");
  }

  @After
  public void tearDown() throws Exception {
    ctd = null;
  }

  @Test
  public void testGetDoc() {
    CloudantRow row = new CloudantRow();
    row.setDoc(ctd);
    assertEquals(ctd, row.getDoc());
  }
  
  @Test
  public void testGetValue() {
    CloudantRow row = new CloudantRow();
    row.setValue(ctd.getToDo());
    assertEquals(ctd.getToDo(), row.getValue());
  }
  
  @Test
  public void testGetId() {
    CloudantRow row = new CloudantRow();
    row.setId("123");
    assertEquals("123", row.getId());
  }
  
  @Test
  public void testGetKey() {
    CloudantRow row = new CloudantRow();
    row.setKey(0);
    assertEquals(0, row.getKey());
  }
}

