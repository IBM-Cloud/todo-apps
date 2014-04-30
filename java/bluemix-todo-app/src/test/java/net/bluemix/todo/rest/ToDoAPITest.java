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
package net.bluemix.todo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.bluemix.todo.model.ToDo;
import net.bluemix.todo.store.InMemoryStore;
import net.bluemix.todo.store.ToDoStore;
import net.bluemix.todo.store.ToDoStoreException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoAPITest {
	private ToDo td1;
	private ToDo td2;
	private ToDoStore store;
	private ToDoAPI api;
	private ToDoStore exceptionStore = new ToDoStore(){

		@Override
		public Collection<ToDo> getAll() throws ToDoStoreException {
			throw new ToDoStoreException("Error");
		}

		@Override
		public ToDo get(String id) throws ToDoStoreException {
			throw new ToDoStoreException("Error");
		}

		@Override
		public ToDo persist(ToDo td) throws ToDoStoreException {
			throw new ToDoStoreException("Error");
		}

		@Override
		public ToDo update(String id, ToDo td) throws ToDoStoreException {
			throw new ToDoStoreException("Error");
		}

		@Override
		public void delete(String id) throws ToDoStoreException {
			throw new ToDoStoreException("Error");
		}

    @Override
    public int count() throws ToDoStoreException {
      throw new ToDoStoreException("Error");
    }
	};
	private ToDoAPI errorApi;
	
	@Before
	public void setUp() throws Exception {
		td1 = new ToDo();
		td1.setId("123");
		td1.setTitle("pick up kids");
		td2 = new ToDo();
		td2.setTitle("pick up laundry");
		td2.setCompleted(true);
		td2.setId("abc");
		store = new InMemoryStore();
		store.persist(td1);
		store.persist(td2);
		api = new ToDoAPI(store);
		errorApi = new ToDoAPI(exceptionStore);
	}

	@After
	public void tearDown() throws Exception {
		td1 = null;
		td2 = null;
		store = null;
		api = null;
		errorApi = null;
	}

	@Test
	public void testGetToDo() {
		assertEquals(td1, api.getToDo(td1.getId()));
	}
	
	@Test
	public void testGetToDoBadRequest() {
		try {
			api.getToDo(null);
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testGetToDoException() {
		try {
			errorApi.getToDo("123");
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testToDoDoesNotExist() {
		try {
			api.getToDo("123");
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}

	@Test
	public void testGetToDos() {
		Collection<ToDo> todos = api.getToDos();
		assertTrue(todos.contains(td1));
		assertTrue(todos.contains(td2));
		assertEquals(2, todos.size());
	}
	
	@Test
	public void testGetToDosException() {
		try {
			errorApi.getToDos();
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus());
		}
	}

	@Test
	public void testNewToDo() {
		ToDo td = new ToDo();
		td.setTitle("do a test");
		td = api.newToDo(td);
		assertNotNull(td.getId());
		Collection<ToDo> todos = api.getToDos();
		assertTrue(todos.contains(td1));
		assertTrue(todos.contains(td2));
		assertTrue(todos.contains(td));
		assertEquals(3, todos.size());	
	}
	
	@Test
	public void testNewToDoException() {
		try {
			api.newToDo(null);
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testNewToDoInternalError() {
		try {
			errorApi.newToDo(new ToDo());
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus());
		}
	}

	@Test
	public void testUpdateToDo() {
		ToDo td = new ToDo();
		td.setTitle("do a test");
		td.setCompleted(true);
		api.updateToDo(td1.getId(), td);
		td = api.getToDo(td1.getId());
		assertEquals("do a test", td.getTitle());
		assertTrue(td.isCompleted());
	}

	@Test
	public void testUpdateToDoIdException() {
		try {
			api.updateToDo(null, new ToDo());
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testUpdateToDoException() {
		try {
			api.updateToDo("123", null);
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testUpdateToDoInternalError() {
		try {
			errorApi.updateToDo("123", new ToDo());
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testUpdateToDoDoesNotExist() {
		try{
			api.updateToDo("123", new ToDo());
			fail("Expected exception to be thrown.");
		} catch(WebApplicationException e) {
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
		}
	}
	
	@Test
	public void testDeleteToDo() {
		api.deleteToDo(td1.getId());
		Collection<ToDo> todos = api.getToDos();
		assertTrue(todos.contains(td2));
		assertEquals(1, todos.size());	
	}
	
	@Test
	public void testDeleteToDoException() {
		try {
			errorApi.deleteToDo("123");
			fail("Expected exception to be thrown.");
		} catch (WebApplicationException e) {
			assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus());
		}
		
	}
}
