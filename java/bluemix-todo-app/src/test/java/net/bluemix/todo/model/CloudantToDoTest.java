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

public class CloudantToDoTest {
  
  private ToDo td;

  @Before
  public void setUp() throws Exception {
    td = new ToDo();
    td.setId("123");
    td.setTitle("This is a test");
  }

  @After
  public void tearDown() throws Exception {
    td = null;
  }

  @Test
  public void testGet_id() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertEquals("123", ctd.get_id());
    ctd = new CloudantToDo();
    ctd.set_id("456");
    assertEquals("456", ctd.get_id());
  }

  @Test
  public void testGet_rev() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertNull(ctd.get_rev());
    ctd = new CloudantToDo();
    ctd.set_rev("def");
    assertEquals("def", ctd.get_rev());
  }

  @Test
  public void testGetTitle() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertEquals("This is a test", ctd.getTitle());
    ctd = new CloudantToDo();
    ctd.setTitle("Another test");
    assertEquals("Another test", ctd.getTitle());
  }

  @Test
  public void testIsCompleted() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertFalse(ctd.isCompleted());
    ctd = new CloudantToDo();
    ctd.setCompleted(true);
    assertTrue(ctd.isCompleted());
  }

  @Test
  public void testGetToDo() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertEquals(td, ctd.getToDo());
    ctd = new CloudantToDo();
    ctd.set_id("456");
    ctd.set_rev("def");
    ctd.setTitle("Another test");
    ToDo testTd = new ToDo();
    testTd.setId("456");
    testTd.setTitle("Another test");
    assertEquals(testTd, ctd.getToDo());
  }
  
  @Test
  public void testGetOrder() {
    CloudantToDo ctd = new CloudantToDo();
    ctd.setOrder(1);
    assertEquals(1, ctd.getOrder());
  }
  
  @Test
  public void testEquals() {
    CloudantToDo ctd = new CloudantToDo(td);
    assertFalse(ctd.equals(null));
    assertFalse(ctd.equals("test"));
    CloudantToDo testCtd = new CloudantToDo();
    testCtd.set_id("123");
    testCtd.setTitle("This is a test");
    assertTrue(ctd.equals(testCtd));
  }

}

