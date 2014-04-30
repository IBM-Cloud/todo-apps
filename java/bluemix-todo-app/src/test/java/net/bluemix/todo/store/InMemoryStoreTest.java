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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import net.bluemix.todo.model.ToDo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InMemoryStoreTest {
	
	ToDo td1;
	ToDo td2;
	private ToDoStore emptyStore;
	private ToDoStore populatedStore;

	@Before
	public void setUp() throws Exception {
		td1 = new ToDo();
		td1.setId("123");
		td1.setTitle("pick up kids");
		td2 = new ToDo();
		td2.setTitle("pick up laundry");
		td2.setCompleted(true);
		td2.setId("abc");
		emptyStore = new InMemoryStore();
		populatedStore = new InMemoryStore();
		td1 = populatedStore.persist(td1);
		td1 = populatedStore.persist(td2);
	}

	@After
	public void tearDown() throws Exception {
		emptyStore = null;
		populatedStore = null;
	}

	@Test
	public void testGetAll() throws Exception {
		assertTrue(emptyStore.getAll().isEmpty());
		Collection<ToDo> todos = populatedStore.getAll();
		assertTrue(todos.contains(td2));
		assertTrue(todos.contains(td1));
	}

	@Test
	public void testGet() throws Exception {
		assertNull(emptyStore.get("123"));
		assertNull(populatedStore.get("123"));
		assertEquals(td1, populatedStore.get(td1.getId()));
		assertEquals(td2, populatedStore.get(td2.getId()));
	}

	@Test
	public void testPersist() throws Exception {
		ToDo td = new ToDo();
		td.setTitle("do a test");
		td = emptyStore.persist(td);
		assertEquals(td, emptyStore.get(td.getId()));
	}

	@Test
	public void testUpdate() throws Exception {
		ToDo td = new ToDo();
		td.setCompleted(true);
		assertNull(emptyStore.update("123", td));
		populatedStore.update(td1.getId(), td);
		td = populatedStore.get(td1.getId());
		assertEquals("", td.getTitle());
		assertTrue(td.isCompleted());
	}

	@Test
	public void testDelete() throws Exception {
		emptyStore.delete("123");
		populatedStore.delete(td1.getId());
		assertNull(populatedStore.get(td1.getId()));
	}
	
	@Test
	public void testCount() throws Exception {
	  assertEquals(0, emptyStore.count());
	  assertEquals(2, populatedStore.count());
	}

}
