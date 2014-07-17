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
require 'mongo'
require 'json'
require 'rufus-scheduler'

#defaults for running locally
uri = 'mongodb://localhost:27017'
database = 'db'
if ENV['VCAP_SERVICES']
  svcs = JSON.parse ENV['VCAP_SERVICES']  
  mongo = svcs.detect { |k,v| k =~ /^mongolab/ }.last.first
  creds = mongo['credentials']
  uri = creds['uri']
  database = uri[%r{/([^/\?]+)(\?|$)}, 1]
end
DB = Mongo::MongoClient.from_uri(uri).db(database, :pool_size => 5, :timeout => 5)
collection = DB.collection('todos')

scheduler = Rufus::Scheduler.new
scheduler.every '30s' do
  if collection.count > 20
    first = collection.find_one
    collection.remove('_id' => first['_id'])
  end
end

#When a GET is issued to the root redirect to index.html
get '/' do
  send_file File.join(settings.public_folder, 'index.html')
end

get '/api/todos' do
  collection.find.to_a.map{|t| from_bson_id(t)}.to_json
end

get '/api/todos/:id' do
  from_bson_id(collection.find_one(to_bson_id(params[:id]))).to_json
end

post '/api/todos' do
  tdJson = JSON.parse(request.body.read.to_s)
  oid = collection.insert(tdJson)
  from_bson_id(collection.find_one(oid)).to_json
end

delete '/api/todos/:id' do
  collection.remove('_id' => to_bson_id(params[:id]))
  nil
end

put '/api/todos/:id' do
  tdJson = JSON.parse(request.body.read.to_s).reject{|k,v| k == '_id'}
  collection.update({'_id' => to_bson_id(params[:id])}, {'$set' => tdJson})
  tdJson['id'] = params[:id]
  tdJson.to_json
end

#Utility functions for converting to/from Monogos Object IDs
def to_bson_id(id) BSON::ObjectId.from_string(id) end
def from_bson_id(obj) obj.merge({'id' => obj['_id'].to_s}).reject{|k| k == '_id'} end