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
const Cloudant = require('cloudant');

function DB(credentials) {
  const DB_NAME = 'todos';
  const cloudant = Cloudant({
    url: credentials.url,
    plugin: 'retry',
    retryAttempts: 10,
    retryTimeout: 500
  });

  const self = this;
  let db;

  self.type = function() {
    return 'Cloudant';
  };

  self.init = () => {
    return new Promise((resolve, reject) => {
      cloudant.db.get(DB_NAME, (err) => {
        if (!err) {
          console.log('Database', DB_NAME, 'already exists');
          db = cloudant.db.use(DB_NAME);
          resolve();
        } else {
          console.log('Database', DB_NAME, 'does not exists, creating it');
          cloudant.db.create(DB_NAME, (err) => {
            if (err) {
              reject(err);
            } else {
              db = cloudant.db.use(DB_NAME);
              resolve();
            }
          });
        }
      });
    });
  };

  self.count = () => {
    return new Promise((resolve, reject) => {
      db.list((err, result) => {
        if (err) {
          reject(err);
        } else {
          resolve(result.total_rows);
        }
      });
    });
  };

  self.search = () => {
    return new Promise((resolve, reject) => {
      db.list({ include_docs: true }, (err, result) => {
        if (err) {
          reject(err);
        } else {
          resolve(result.rows.map(row => {
            const doc = row.doc;
            doc.id = doc._id;
            delete doc._id;
            return doc;
          }));
        }
      });
    });
  };

  self.create = (item) => {
    console.log('create', item);
    return new Promise((resolve, reject) => {
      db.insert(item, { include_docs: true }, (err, savedItem) => {
        if (err) {
          reject(err);
        } else {
          const newItem = {
            id: savedItem.id,
            _rev: savedItem. rev,
            title: item.title,
            completed: item.completed,
            order: item.order
          };
          console.log('created');
          resolve(newItem);
        }
      });
    });
  };

  self.read = (id) => {
    console.log('read', id);
    return new Promise((resolve, reject) => {
      db.get(id, (err, item) => {
        if (err) {
          reject(err);
        } else {
          item.id = item._id;
          delete item._id;
          console.log('read', item);
          resolve(item);
        }
      });
    });
  };

  self.update = (id, newValue) => {
    console.log('update', id, newValue);
    return new Promise((resolve, reject) => {
      newValue._id = newValue.id;
      delete newValue.id;
      db.insert(newValue, (err, updatedItem) => {
        if (err) {
          reject(err);
        } else {
          newValue.id = newValue._id;
          newValue._rev = updatedItem._rev;
          delete newValue._id;
          console.log('updated', newValue);
          resolve(newValue);
        }
      });
    });
  };

  self.delete = (id) => {
    console.log('delete', id);
    return new Promise((resolve, reject) => {
      self.read(id).then(item => {
        db.destroy(item.id, item._rev, (err, body) => {
          if (err) {
            reject(err);
          } else {
            console.log('deleted', item);
            resolve(item);
          }
        });
      }).catch(err => {
        reject(err);
      });
    });
  };
}

module.exports = function(credentials) {
  return new DB(credentials);
}
