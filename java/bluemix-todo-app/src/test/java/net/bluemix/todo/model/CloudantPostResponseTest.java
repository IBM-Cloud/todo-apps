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

public class CloudantPostResponseTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetId() {
    CloudantPostResponse resp = new CloudantPostResponse();
    resp.setId("123");
    assertEquals("123", resp.getId());
  }

  @Test
  public void testIsOk() {
    CloudantPostResponse resp = new CloudantPostResponse();
    resp.setOk(false);
    assertFalse(resp.isOk());
    resp.setOk(true);
    assertTrue(resp.isOk());
  }

  @Test
  public void testGetRev() {
    CloudantPostResponse resp = new CloudantPostResponse();
    resp.setRev("abc");
    assertEquals("abc", resp.getRev());
  }
}

