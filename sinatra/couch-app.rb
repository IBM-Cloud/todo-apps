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

require 'rubygems'
require 'sinatra'
require 'couchrest'
require 'json'
require 'rufus-scheduler'

# Helper function to construct the proper URL 
# to the Couch DB.
# URL should be http(s)://username:password@user.cloudant.com/bluemix-todo
def get_couch_db(creds) 
  url = creds['url']
  if !url.end_with?('/')
    url = url + '/'
  end
  url = url + 'bluemix-todo'

  auth = creds['username'] + ':' + creds['password'] + '@'
  if(url.start_with?('https'))
    url = url.insert(8, auth)
  else
    url = url.insert(7, auth)
  end
  puts 'Using URL: ' + url
  #This will create the DB if it does not exist, however it will fail if you do not have permissions
  CouchRest.database!(url)
end

# Helper function to create the view if it does not already exist
def create_view(db) 
  begin
    db.get('_design/todos')
  rescue RestClient::ResourceNotFound => nfe
    db.save_doc({
      "_id" => "_design/todos",
      :views => {
        :allTodos => {
          :reduce => "_count",
          :map => "function(doc){if(doc.title && doc.completed != null){emit(doc.order,{title: doc.title,completed: doc.completed})}}"
        }
      }
      })
  end
end

creds = nil
if ENV['VCAP_SERVICES']
  svcs = JSON.parse ENV['VCAP_SERVICES']  
  svcs['user-provided'].each do |serv|
    if serv['name'] == 'todo-couch-db'
      creds = serv['credentials']
      break
    end
  end
end

db = get_couch_db(creds)
create_view(db)

#Schedule a task to run every 5 seconds to clean up the ToDos
scheduler = Rufus::Scheduler.new
scheduler.every '30s' do
  rows = db.view('todos/allTodos')['rows']
  if rows.length > 0
    if rows[0]['value'] > 20
      params = {
        :reduce => false 
      }
      id = db.view('todos/allTodos', params)['rows'][0]['id']
      db.delete_doc(db.get(id))
    end
  end
end

#When a GET is issued to the root redirect to index.html
get '/' do
  send_file File.join(settings.public_folder, 'index.html')
end

get '/api/todos' do
  params = {
    :reduce => false 
  }
  db.view('todos/allTodos', params)['rows'].to_a.map{|t| to_client_todo(t)}.to_json
end

get '/api/todos/:id' do
  db.get(params[:id]).to_json
end

post '/api/todos' do
  tdJson = JSON.parse(request.body.read.to_s)
  resp = db.save_doc(tdJson)
  # Alot happening, add the id property, and remove the _id and revision
  # properties
  tdJson.merge({'id' => tdJson['_id']}).reject{
    |k| k == '_id' || k =='_rev'}.to_json
end

delete '/api/todos/:id' do
  db.delete_doc(db.get(params[:id]))
  nil
end

put '/api/todos/:id' do
  tdJson = JSON.parse(request.body.read.to_s)
  doc = db.get(params[:id])
  # Add the updates to the document and remove the id property
  doc = doc.merge(tdJson).reject{|k| k == 'id'}
  # Add the _id property to do the update
  doc['_id'] = params[:id]
  db.save_doc(doc)
  tdJson.to_json
end

# Translate the Cloudant ToDo JSON into JSON the client is expecting
def to_client_todo(obj) obj['value'].merge({'id' => obj['id'], 'order' => obj['key']}) end