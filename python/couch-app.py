#
# Copyright IBM Corp. 2014
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

from flask import Flask, abort, request
from apscheduler.scheduler import Scheduler
from couchdb.design import ViewDefinition
from json import dumps
import os
import json
import couchdb

app = Flask(__name__, static_url_path='/static')

# If you want to run this locally using a local Couch DB be sure to enter 
# your username and password.
# The other option is to run locally and point to your Cloudant account,
# in this case just change the url, username, and password variables
url = 'http://admin@127.0.0.1:5984/'
if os.environ.has_key('VCAP_SERVICES'):
    vcap_json = json.loads(os.environ['VCAP_SERVICES'])
    for key, value in vcap_json.iteritems():
        couch_services = filter(lambda s: s['name'] == 'todo-couch-db', value)
        if len(couch_services) != 0:
            couch_service = couch_services[0]
            url = couch_service['credentials']['url']

couch = couchdb.Server(url)
try:
    db = couch['bluemix-todo']
except:
    db = couch.create('bluemix-todo')

view = ViewDefinition('todos', 'allTodos', '''function(doc){if(doc.title && doc.completed != null){emit(doc.order,{title: doc.title,completed: doc.completed})}}''', "_count")
view_doc = view.get_doc(db)
if not view_doc:
    # If the view does not exist create it
    view.sync(db)

# Schedule something that will remove ToDos if we get too many
sched = Scheduler()
sched.start()
def cleanup():
    results = db.view('todos/allTodos')
    if len(results) > 0:
        count = results.rows[0]['value']
        if count > 20:
            id = db.view('todos/allTodos', reduce=False).rows[0]['id']
            db.delete(db.get(id))
sched.add_interval_job(cleanup, seconds=30)

# Serve up index.html when requests come to root
@app.route('/')
def index():
    return app.send_static_file('index.html')

# Serve up static resources, JavaScript, HTML, CSS
@app.route('/<path:path>')
def js_proxy(path):
    # send_static_file will guess the correct MIME type
    return app.send_static_file(os.path.join(path))

#Helper function to preprocess Cloudant ToDos to what the client expects
def to_todo_obj(todo):
    new_todo = {
        'id' : todo['id'],
        'order' : todo['key'],
        'title' : todo['value']['title'],
        'completed' : todo['value']['completed']
    }
    return new_todo

@app.route('/api/todos', methods = ['GET'])
def get_todos():
    return dumps(map(to_todo_obj, db.iterview('todos/allTodos', 10, reduce=False)))

@app.route('/api/todos', methods = ['POST'])
def create_todo():
    if not request.json:
        abort(400)
    todo = request.json
    id, rev = db.save(todo)
    todo['id'] = str(id)
    del todo['_id']
    del todo['_rev']
    return dumps(todo)

@app.route('/api/todos/<id>', methods = ['DELETE'])
def delete_todo(id):
    doc = db.get(id)
    db.delete(doc)
    return '', 204

@app.route('/api/todos/<id>', methods = ['PUT'])
def update_todo(id):
    if not request.json:
        abort(400)
    todo = db.get(id)
    if not todo:
        abort(400)
    new_todo = request.json
    todo['title'] = new_todo['title']
    todo['order'] = new_todo['order']
    todo['completed'] = new_todo['completed']
    todo['id'] = id;
    db.save(todo)
    del todo['_rev']
    del todo['_id']
    return dumps(todo)

port = os.getenv('VCAP_APP_PORT', '5000')
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(port), debug = True, use_reloader=False)