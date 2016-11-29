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
function DB() {
  const self = this;

  let NEXT_ID = 0;
  function getNextId() { return ''+(NEXT_ID++); }

  self.items = [];

  self.type = () => {
    return 'In Memory';
  }

  self.init = () => {
    return Promise.resolve();
  }

  self.count = () => {
    return Promise.resolve(self.items.length);
  }

  self.search = () => {
    return Promise.resolve(self.items);
  }

  self.create = (item) => {
    console.log('Create with', item);
    const newItem = JSON.parse(JSON.stringify(item));
    newItem.id = getNextId();
    self.items.push(newItem);
    return Promise.resolve(newItem);
  }

  self.read = (id) => {
    let result = self.items.filter(item => item.id === id)[0] || null;
    return Promise.resolve(result);
  }

  self.update = (id, newValue) => {
    console.log('update', id, newValue, self.items);
    let itemIndex = self.items.findIndex(item => (item.id === id));
    console.log('index is', itemIndex);
    if (itemIndex === -1) {
      return Promise.reject('not found');
    } else {
      const newArrayValue = JSON.parse(JSON.stringify(newValue));
      newArrayValue.id = id;
      self.items[itemIndex] = newArrayValue;
      return Promise.resolve(newArrayValue);
    }
  }

  self.delete = (id) => {
    console.log('delete', id);
    let itemToDelete = self.items.find(item => item.id === id);
    if (itemToDelete) {
      self.items = self.items.filter(item => item !== itemToDelete);
      return Promise.resolve(itemToDelete);
    } else {
      return Promise.reject('not found');
    }
  }
}

module.exports = function() {
  return new DB();
}
