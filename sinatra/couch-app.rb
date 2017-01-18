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

# Helper function to construct the proper URL 
# to the Couch DB.
# URL should be http(s)://username:password@user.cloudant.com/bluemix-todo
def get_couch_db(url)
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

db = get_couch_db(ENV['CLOUDANT_URI'] || ENV['HTTP_URI'])
create_view(db)

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

get '/jobid' do
  ENV['CNTM_JOB_UUID']
end

get '/instanceid' do
  ENV['CNTM_INSTANCE_UUID']
end

# Translate the Cloudant ToDo JSON into JSON the client is expecting
def to_client_todo(obj) obj['value'].merge({'id' => obj['id'], 'order' => obj['key']}) end