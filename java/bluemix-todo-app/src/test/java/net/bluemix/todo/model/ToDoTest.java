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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetId() {
		ToDo td = new ToDo();
		assertNull(td.getId());
		td.setId("123abc");
		assertEquals("123abc", td.getId());
	}

	@Test
	public void testIsCompleted() {
		ToDo td = new ToDo();
		assertFalse(td.isCompleted());
		td.setCompleted(true);
		assertTrue(td.isCompleted());
	}

	@Test
	public void testGetTitle() {
		ToDo td = new ToDo();
		assertEquals("", td.getTitle());
		td.setTitle("Pick up kids at 3");
		assertEquals("Pick up kids at 3", td.getTitle());
	}
	
	@Test
	public void testGetOrder() {
	  ToDo td = new ToDo();
	  td.setOrder(1);
	  assertEquals(1, td.getOrder());
	}
	
	@Test
	public void testEquals() {
		ToDo td = new ToDo();
		assertFalse(td.equals(null));
		assertTrue(td.equals(td));
		td.setTitle("this is a test task");
		td.setCompleted(true);
		assertFalse(td.equals(new ToDo()));
		ToDo td2 = new ToDo();
		td2.setTitle("this is a test task");
		td2.setCompleted(true);
		assertTrue(td.equals(td2));
	}
}
