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
package net.bluemix.todo.store;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import net.bluemix.todo.model.CloudantAllDocs;
import net.bluemix.todo.model.CloudantCount;
import net.bluemix.todo.model.CloudantPostResponse;
import net.bluemix.todo.model.CloudantReducedRow;
import net.bluemix.todo.model.CloudantRow;
import net.bluemix.todo.model.CloudantToDo;
import net.bluemix.todo.model.ToDo;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloudantStoreTest {
  private CloudantAllDocs docs;
  private List<CloudantRow> rows;
  private CloudantRow row1;
  private CloudantRow row2;
  private CloudantRow row3;
  private CloudantToDo ctd1;
  private CloudantToDo ctd2;
  private CloudantToDo ctd3;
  private CloudantCount count;
  private List<CloudantReducedRow> reducedRows;
  private CloudantReducedRow reducedRow;

  private WebTarget createMockWebTarget() {
    IMocksControl control = createControl();
    WebTarget wt = control.createMock(WebTarget.class);
    expect(wt.register(isA(LoggingFilter.class))).andReturn(wt).anyTimes();
    return wt;
  }
  
  private Invocation.Builder createBuilder() {
    IMocksControl control = createControl();
    return control.createMock(Invocation.Builder.class);
  }
  
  @Before
  public void setUp() throws Exception {
    docs = new CloudantAllDocs();
    rows = new ArrayList<CloudantRow>();
    ctd1 = new CloudantToDo();
    count = new CloudantCount();
    reducedRows = new ArrayList<CloudantReducedRow>();
    reducedRow = new CloudantReducedRow();
    reducedRow.setValue(123);
    reducedRows.add(reducedRow);
    count.setRows(reducedRows);
    
    ctd1.setTitle("title1");
    ctd1.set_id("123");
    ctd1.set_rev("abc");
    ctd2 = new CloudantToDo();
    ctd2.setTitle("title2");
    ctd2.set_id("456");
    ctd2.set_rev("def");
    ctd3 = new CloudantToDo();
    ctd3.setTitle("title3");
    ctd3.set_id("789");
    ctd3.set_rev("ghi");
    row1 = new CloudantRow();
    row1.setId(ctd1.get_id());
    row1.setValue(ctd1.getToDo());
    row2 = new CloudantRow();
    row2.setId(ctd2.get_id());
    row2.setValue(ctd2.getToDo());
    row3 = new CloudantRow();
    row3.setId(ctd3.get_id());
    row3.setValue(ctd3.getToDo());
    rows.add(row1);
    rows.add(row2);
    rows.add(row3);
    docs.setRows(rows);
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
    docs = null;
    count = null;
    reducedRow = null;
    reducedRows = null;
  }

  @Test
  public void testGetAll() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(3);
    Capture<Class<CloudantAllDocs>> classCapture = new Capture<Class<CloudantAllDocs>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(docs);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    expect(builder.get()).andReturn(resp).times(3);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt).times(2);
    expect(wt.path(eq("_design"))).andReturn(wt).times(2);
    expect(wt.path(eq("_view"))).andReturn(wt);
    expect(wt.path(eq("allTodos"))).andReturn(wt);
    expect(wt.queryParam(eq("reduce"), eq(false))).andReturn(wt);
    //expect(wt.queryParam(eq("include_docs"), eq(true))).andReturn(wt);
    expect(wt.request(eq("application/json"))).andReturn(builder).anyTimes();
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    Collection<ToDo> todos = store.getAll();
    List<ToDo> testToDos = new ArrayList<ToDo>();
    testToDos.add(ctd1.getToDo());
    testToDos.add(ctd2.getToDo());
    testToDos.add(ctd3.getToDo());
    assertEquals(testToDos, todos);
    assertEquals(CloudantAllDocs.class, classCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }

  @Test
  public void testGet() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(3);
    Capture<Class<CloudantToDo>> classCapture = new Capture<Class<CloudantToDo>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(ctd1);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    expect(builder.get()).andReturn(resp).times(3);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt);
    expect(wt.path(eq("_design"))).andReturn(wt).times(1);
    expect(wt.path(eq("123"))).andReturn(wt);
    expect(wt.request(eq("application/json"))).andReturn(builder).anyTimes();
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    assertEquals(ctd1.getToDo(), store.get("123"));
    assertEquals(CloudantToDo.class, classCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }

  @Test
  public void testPersist() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(2);
    expect(resp.getStatus()).andReturn(201);
    CloudantPostResponse postResp = new CloudantPostResponse();
    postResp.setId("123");
    postResp.setOk(true);
    postResp.setRev("abc");
    Capture<Class<CloudantPostResponse>> classCapture = new Capture<Class<CloudantPostResponse>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(postResp);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    ToDo td = new ToDo();
    td.setTitle("this is a test");
    expect(builder.post(isA(Entity.class))).andReturn(resp);
    expect(builder.get()).andReturn(resp).times(2);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt);
    expect(wt.path(eq("_design"))).andReturn(wt).times(1);
    expect(wt.request(eq("application/json"))).andReturn(builder).anyTimes();
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    ToDo testTd = new ToDo();
    testTd.setTitle("this is a test");
    testTd.setId("123");
    assertEquals(testTd, store.persist(td));
    assertEquals(CloudantPostResponse.class, classCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }

  @Test
  public void testUpdate() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(3);
    expect(resp.getStatus()).andReturn(201);
    Capture<Class<CloudantToDo>> classToDoCapture = new Capture<Class<CloudantToDo>>();
    expect(resp.readEntity(capture(classToDoCapture))).andReturn(ctd1);
    CloudantPostResponse postResp = new CloudantPostResponse();
    postResp.setId("123");
    postResp.setOk(true);
    postResp.setRev("def");
    Capture<Class<CloudantPostResponse>> classCapture = new Capture<Class<CloudantPostResponse>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(postResp);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    ToDo td = new ToDo();
    td.setTitle("new text");
    td.setId("123");
    expect(builder.put(isA(Entity.class))).andReturn(resp);
    expect(builder.get()).andReturn(resp).times(3);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt);
    expect(wt.path(eq("_design"))).andReturn(wt).times(1);
    expect(wt.queryParam(eq("rev"), eq("abc"))).andReturn(wt);
    expect(wt.path(eq("123"))).andReturn(wt).times(2);
    expect(wt.request(eq("application/json"))).andReturn(builder).anyTimes();
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    ToDo testTd = new ToDo();
    testTd.setTitle("new text");
    testTd.setId("123");
    assertEquals(testTd, store.update("123", td));
    assertEquals(CloudantPostResponse.class, classCapture.getValue());
    assertEquals(CloudantToDo.class, classToDoCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }

  @Test
  public void testDelete() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(4);
    Capture<Class<CloudantToDo>> classCapture = new Capture<Class<CloudantToDo>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(ctd1);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    expect(builder.get()).andReturn(resp).times(3);
    expect(builder.delete()).andReturn(resp);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt);
    expect(wt.path(eq("_design"))).andReturn(wt).times(1);
    expect(wt.queryParam(eq("rev"), eq("abc"))).andReturn(wt);
    expect(wt.path(eq("123"))).andReturn(wt).times(2);
    expect(wt.request(eq("application/json"))).andReturn(builder).times(4);
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    store.delete("123");
    assertEquals(CloudantToDo.class, classCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }
  
  @Test
  public void testCount() throws Exception {
    IMocksControl control = createControl();
    Response resp = control.createMock(Response.class);
    expect(resp.getStatus()).andReturn(200).times(3);
    Capture<Class<CloudantCount>> classCapture = new Capture<Class<CloudantCount>>();
    expect(resp.readEntity(capture(classCapture))).andReturn(count);
    replay(resp);
    WebTarget wt = createMockWebTarget();
    Invocation.Builder builder = createBuilder();
    expect(builder.get()).andReturn(resp).times(3);
    replay(builder);
    expect(wt.path(eq("bluemix-todo"))).andReturn(wt).times(2);
    expect(wt.path(eq("todos"))).andReturn(wt).times(2);
    expect(wt.path(eq("_design"))).andReturn(wt).times(2);
    expect(wt.path(eq("_view"))).andReturn(wt);
    expect(wt.path(eq("allTodos"))).andReturn(wt);
    expect(wt.request(eq("application/json"))).andReturn(builder).anyTimes();
    replay(wt);
    CloudantStore store = new CloudantStore(wt);
    assertEquals(123, store.count());
    assertEquals(CloudantCount.class, classCapture.getValue());
    verify(resp);
    verify(wt);
    verify(builder);
  }
}