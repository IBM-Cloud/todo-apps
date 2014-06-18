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
from flask import Flask, jsonify, abort, request
from pymongo import MongoClient
from bson import Binary, Code, ObjectId
from bson.json_util import dumps
from apscheduler.scheduler import Scheduler
import os
import json

app = Flask(__name__, static_url_path='/static')

# Gets the Mongo DB URL and DB from VCAP_SERVICES if present, else
# assumes the Mongo instance is running locally
url = 'mongodb://localhost:27017/'
dbName = 'db'
if os.environ.has_key('VCAP_SERVICES'):
    vcapJson = json.loads(os.environ['VCAP_SERVICES'])
    for key, value in vcapJson.iteritems():
        #Only find the services with the name todo-mongo-db, there should only be one
        mongoServices = filter(lambda s: s['name'] == 'todo-mongo-db', value)
        if len(mongoServices) != 0:
            mongoService = mongoServices[0]
            url = mongoService['credentials']['url']
            dbName = mongoService['credentials']['db']

client = MongoClient(url)
db = client[dbName]
collection = db.todos

# Create a scheduled task that will cleanup ToDos if the list grows too big
sched = Scheduler()
sched.start()
def cleanup():
    if collection.count() > 20:
        collection.remove(collection.find_one())
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

#Helper function to preprocess Mongo ToDos to what the client expects
def to_todo_obj(todo):
    todo['id'] = str(todo['_id'])
    del todo['_id']
    return todo

@app.route('/api/todos', methods = ['GET'])
def get_todo():
	return dumps(map(to_todo_obj, collection.find()))

@app.route('/api/todos', methods = ['POST'])
def create_todo():
    if not request.json:
        abort(400)
    todo = request.json
    id = collection.insert(todo)
    todo['id'] = str(id)
    del todo['_id']
    return dumps(todo)

@app.route('/api/todos/<id>', methods = ['DELETE'])
def delete_todo(id):
    collection.remove({'_id' : ObjectId(id)})
    return '', 204

@app.route('/api/todos/<id>', methods = ['PUT'])
def update_todo(id):
    if not request.json:
        abort(400)
    todo = collection.find_one({'_id' : ObjectId(id)})
    newToDo = request.json
    todo['title'] = newToDo['title']
    todo['order'] = newToDo['order']
    todo['completed'] = newToDo['completed']
    collection.save(todo)
    del todo['_id']
    return dumps(todo)

port = os.getenv('VCAP_APP_PORT', '5000')
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(port), debug = True)