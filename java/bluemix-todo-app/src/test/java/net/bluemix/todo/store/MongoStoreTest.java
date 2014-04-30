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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.bluemix.todo.model.ToDo;

import org.bson.types.ObjectId;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class MongoStoreTest {
	
	private DBCollection createMockCollection() {
		IMocksControl control = createControl();
		return control.createMock(DBCollection.class);
	}
	
	private DBCursor createMockCursor() {
		IMocksControl control = createControl();
		return control.createMock(DBCursor.class);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() {
		DBCollection coll = createMockCollection();
		DBCursor cursor = createMockCursor();
		expect(cursor.hasNext()).andAnswer(new IAnswer<Boolean>() {
			private int count = 0;
			@Override
			public Boolean answer() throws Throwable {
				count++;
				return count == 3 ? false : true;
			}
		}).anyTimes();
		expect(cursor.next()).andAnswer(new IAnswer<DBObject>() {
			private int count = 0;
			@Override
			public DBObject answer() throws Throwable {
				count++;
				BasicDBObject dbObj = new BasicDBObject();
				dbObj.put("title", "This is todo " + count);
				dbObj.put("completed", false);
				dbObj.put("order", count);
				//Object IDs must be 24 characters long
				dbObj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa" + count));
				return dbObj;
			}
		}).anyTimes();
		cursor.close();
		expectLastCall();
		replay(cursor);
		expect(coll.find()).andReturn(cursor);
		replay(coll);
		MongoStore store = new MongoStore(coll);
		Collection<ToDo> todos = store.getAll();
		List<ToDo> testToDos = new ArrayList<ToDo>();
		ToDo td1 = new ToDo();
		td1.setId("aaaaaaaaaaaaaaaaaaaaaaa1");
		td1.setTitle("This is todo 1");
		td1.setOrder(1);
		ToDo td2 = new ToDo();
		td2.setId("aaaaaaaaaaaaaaaaaaaaaaa2");
		td2.setTitle("This is todo 2");
		td2.setOrder(2);
		testToDos.add(td1);
		testToDos.add(td2);
		assertEquals(testToDos, todos);
		verify(cursor);
		verify(coll);
	}

	@Test
	public void testGet() {
		DBCollection coll = createMockCollection();
		DBCursor cursor = createMockCursor();
		expect(cursor.hasNext()).andAnswer(new IAnswer<Boolean>() {
			private int count = 0;
			@Override
			public Boolean answer() throws Throwable {
				count++;
				return count == 2 ? false : true;
			}
		}).anyTimes();
		expect(cursor.next()).andAnswer(new IAnswer<DBObject>() {
			private int count = 0;
			@Override
			public DBObject answer() throws Throwable {
				count++;
				BasicDBObject dbObj = new BasicDBObject();
				dbObj.put("title", "This is todo " + count);
				dbObj.put("completed", false);
				dbObj.put("order", count);
				//Object IDs must be 24 characters long
				dbObj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa" + count));
				return dbObj;
			}
		}).anyTimes();
		cursor.close();
		expectLastCall();
		replay(cursor);
		DBCursor emptyCursor = createMockCursor();
		expect(emptyCursor.hasNext()).andReturn(false);
		replay(emptyCursor);
		expect(coll.find(eq(new BasicDBObject("_id", "aaaaaaaaaaaaaaaaaaaaaaa1")))).andReturn(cursor);
		expect(coll.find(eq(new BasicDBObject("_id", "1")))).andReturn(emptyCursor);
		replay(coll);
		MongoStore store = new MongoStore(coll);
		ToDo td = store.get("1");
		assertNull(td);
		td = store.get("aaaaaaaaaaaaaaaaaaaaaaa1");
		ToDo td1 = new ToDo();
		td1.setId("aaaaaaaaaaaaaaaaaaaaaaa1");
		td1.setTitle("This is todo 1");
		td1.setOrder(1);
		assertEquals(td, td1);
		verify(cursor);
		verify(emptyCursor);
		verify(coll);
	}

	@Test
	public void testPersist() {
		DBCollection coll = createMockCollection();
		ToDo td = new ToDo();
		td.setTitle("This is a test");
		td.setId("aaaaaaaaaaaaaaaaaaaaaaa1");
		expect(coll.insert(isA(DBObject.class))).andAnswer(new IAnswer<WriteResult>() {
			@Override
			public WriteResult answer() throws Throwable {
				DBObject obj = (DBObject)getCurrentArguments()[0];
				obj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa1"));
				return null;
			}
		});
		replay(coll);
		MongoStore store = new MongoStore(coll);
		assertEquals(td, store.persist(td));
		verify(coll);
	}

	@Test
	public void testUpdate() {
		DBCollection coll = createMockCollection();
		DBCursor cursor = createMockCursor();
		DBObject query = new BasicDBObject("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa2"));
		DBObject dbObj = new BasicDBObject();
		dbObj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa2"));
		dbObj.put("title", "new title");
		dbObj.put("completed", true);
		dbObj.put("order", 0);
		expect(cursor.next()).andReturn(dbObj);
		cursor.close();
		expectLastCall();
		replay(cursor);
		expect(coll.find(eq(query))).andReturn(cursor);
		ToDo newTd = new ToDo();
		newTd.setId("aaaaaaaaaaaaaaaaaaaaaaa2");
		newTd.setCompleted(true);
		newTd.setTitle("new title");
		newTd.setOrder(0);
		expect(coll.update(eq(query), eq(dbObj))).andReturn(null);
		replay(coll);
		MongoStore store = new MongoStore(coll);
		assertEquals(newTd, store.update("aaaaaaaaaaaaaaaaaaaaaaa2", newTd));
		verify(cursor);
		verify(coll);
	}

	@Test
	public void testDelete() {
		ToDo td = new ToDo();
		td.setId("aaaaaaaaaaaaaaaaaaaaaaa2");
		td.setTitle("this is a test");
		BasicDBObject dbObj = new BasicDBObject();
		dbObj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa2"));
		DBCollection coll = createMockCollection();
		expect(coll.remove(eq(dbObj))).andReturn(null);
		replay(coll);
		MongoStore store = new MongoStore(coll);
		store.delete("aaaaaaaaaaaaaaaaaaaaaaa2");
		verify(coll);
	}
	
	@Test
	public void testCount() throws Exception {
	  DBCollection coll = createMockCollection();
    DBCursor cursor = createMockCursor();
    expect(cursor.hasNext()).andAnswer(new IAnswer<Boolean>() {
      private int count = 0;
      @Override
      public Boolean answer() throws Throwable {
        count++;
        return count == 3 ? false : true;
      }
    }).anyTimes();
    expect(cursor.next()).andAnswer(new IAnswer<DBObject>() {
      private int count = 0;
      @Override
      public DBObject answer() throws Throwable {
        count++;
        BasicDBObject dbObj = new BasicDBObject();
        dbObj.put("title", "This is todo " + count);
        dbObj.put("completed", false);
        dbObj.put("order", count);
        //Object IDs must be 24 characters long
        dbObj.put("_id", new ObjectId("aaaaaaaaaaaaaaaaaaaaaaa" + count));
        return dbObj;
      }
    }).anyTimes();
    cursor.close();
    expectLastCall();
    replay(cursor);
    expect(coll.find()).andReturn(cursor);
    replay(coll);
    MongoStore store = new MongoStore(coll);
    assertEquals(2, store.count());
    verify(cursor);
    verify(coll);
	}
}
