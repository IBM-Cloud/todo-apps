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

from paver.easy import *
from shutil import rmtree, copytree, copyfile
from subprocess import call
import os
import getpass

@task
def clean():
	print '************************************'
	print '* Cleaning                         *'
	print '************************************'
	rmtree('static', True)
	try:
	    os.remove('manifest.yml')
	except OSError:
		pass

@task
def copy_client_code():
	print '************************************'
	print '* Copying Client Code              *'
	print '************************************'
	copytree('../frontend', 'static')

@task
def copy_mongo_manifest():
	print '************************************'
	print '* Copying Mongo Manifest           *'
	print '************************************'
	copyfile('deploy/mongo-manifest.yml', 'manifest.yml')

@task
def copy_cloudant_manifest():
	print '************************************'
	print '* Copying Cloudant Manifest           *'
	print '************************************'
	copyfile('deploy/cloudant-manifest.yml', 'manifest.yml')	

@task
def create_mongo_service():
	print '************************************'
	print '* Creating Mongo DB Service        *'
	print '************************************'
	call(["cf", "create-service", "mongodb", "100", "todo-mongo-db"])

@task
def create_cloudant_service():
	print '************************************'
	print '* Creating Cloudant Service        *'
	print '************************************'
	call(["cf", "create-service", "cloudantNoSQLDB", "Lite", "todo-couch-db"])

@task
def cf_login():
	print '************************************'
	print '* Logging Into Bluemix             *'
	print '************************************'
	call(["cf", "login", "-a", "https://api.ng.bluemix.net"])

@task
def deploy_to_bluemix():
	print '************************************'
	print '* Pushing App To Bluemix           *'
	print '************************************'
	name = raw_input("What would you like to call this app?\n")
	call(["cf", "push", name])

@task
@needs('clean', 'copy_client_code', 'copy_mongo_manifest', 'create_mongo_service', 'deploy_to_bluemix')
def deploy_mongo_todo():
	pass

@task
@needs('clean', 'copy_client_code', 'copy_cloudant_manifest', 'create_cloudant_service', 'deploy_to_bluemix')
def deploy_cloudant_todo():
	pass	