/**
 * Copyright 2016 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const assert = require('chai').assert;
let db;

let NEXT_ORDER = 1;
function getOrder() { return NEXT_ORDER++; }

describe('DB', () => {

  before((done) => {
    db = require('../lib/in-memory')();
    console.log(db.type());
    db.init().then(() => done());
  });

  it('should be empty', (done) => {
    db.count().then(count => {
      assert.equal(0, count);
      done();
    });
  });

  it('should be able to create an item', (done) => {
    const item = {
      title: 'item C',
      completed: false,
      order: getOrder()
    };
    db.create(item).then(savedItem => {
      assert.equal(item.title, savedItem.title);
      assert.equal(item.completed, savedItem.completed);
      assert.equal(item.order, savedItem.order);
    }).then(() => {
      db.count().then(count => {
        assert.equal(1, count);
      });
    }).then(() => {
      db.search().then(items => {
        assert.equal(1, items.length);
        assert.equal(item.title, items[0].title);
      }).then(() => { done(); });
    })
  });

  it('should be able to read an item', (done) => {
    const item = {
      title: 'item R',
      completed: true,
      order: getOrder()
    };

    db.create(item).then(savedItem => {
      db.read(savedItem.id).then(loadedItem => {
        assert.equal(loadedItem.title, item.title);
        assert.equal(loadedItem.completed, item.completed);
      });
    }).then(() => {
      db.count().then(count => {
        assert.equal(2, count);
      }).then(() => {
        done();
      });
    });
  });

  it('should be able to update an item', (done) => {
    const item = {
      title: 'item U',
      completed: false,
      order: getOrder()
    };

    db.create(item).then(savedItem => {
      savedItem.title     = `-${item.title}-`;
      savedItem.completed = !savedItem.completed;
      db.update(savedItem.id, savedItem).then(updatedItem => {
        assert.equal(updatedItem.title, `-${item.title}-`);
        assert.equal(updatedItem.completed, !item.completed);
        db.read(updatedItem.id).then(loadedItem => {
          assert.equal(loadedItem.title, `-${item.title}-`);
          assert.equal(loadedItem.completed, !item.completed);
        }).then(() => done());
      });
    });
  });

  it('should be able to delete an item', (done) => {
    const item = {
      title: 'item D',
      completed: false,
      order: getOrder()
    };

    let id = null

    db.create(item).then(savedItem => {
      id = savedItem.id;
      db.delete(savedItem.id).then(deletedItem => {
        assert.equal(deletedItem.id, id);
      }).then(() => done());
    });
  });

});
