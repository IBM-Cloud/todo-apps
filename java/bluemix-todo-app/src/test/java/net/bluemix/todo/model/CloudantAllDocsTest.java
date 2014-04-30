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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloudantAllDocsTest {
  private List<CloudantRow> rows;
  private CloudantRow row1;
  private CloudantRow row2;
  private CloudantRow row3;
  private CloudantToDo ctd1;
  private CloudantToDo ctd2;
  private CloudantToDo ctd3;
  
  @Before
  public void setUp() throws Exception {
    rows = new ArrayList<CloudantRow>();
    ctd1 = new CloudantToDo();
    ctd2 = new CloudantToDo();
    ctd3 = new CloudantToDo();
    row1 = new CloudantRow();
    row1.setDoc(ctd1);
    row2 = new CloudantRow();
    row2.setDoc(ctd2);
    row3 = new CloudantRow();
    row3.setDoc(ctd3);
    rows.add(row1);
    rows.add(row2);
    rows.add(row3);
  }

  @After
  public void tearDown() throws Exception {
    rows = null;
    row1 = null;
    row2 = null;
    row3 = null;
    ctd1 = null;
    ctd2 = null;
    ctd3 = null;
  }

  @Test
  public void testGetRows() {
    CloudantAllDocs resp = new CloudantAllDocs();
    resp.setRows(rows);
    assertEquals(rows, resp.getRows());
  }
}

