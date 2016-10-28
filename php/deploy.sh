#!/bin/bash
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

clean() {
  echo "********************************"
  echo "* Cleaning                     *"
  echo "********************************"
  rm -rf public
  rm -rf vendor
  rm composer.lock
  rm composer.phar
  rm manifest.yml
  rm index.php
}

copy_cloudant_manifest() {
  echo "********************************"
  echo "* Copying Couch Manifest       *"
  echo "********************************"
  cp deploy/couch-manifest.yml ./manifest.yml
}

copy_mongo_manifest() {
  echo "********************************"
  echo "* Copying Mongo Manifest       *"
  echo "********************************"
  cp deploy/mongo-manifest.yml ./manifest.yml
}

copy_mongo_index() {
  echo "********************************"
  echo "* Copying Mongo Index          *"
  echo "********************************"
  cp mongo-index.php index.php
}

copy_cloudant_index() {
  echo "********************************"
  echo "* Copying Cloudant Index       *"
  echo "********************************"
  cp couch-index.php index.php
}

copy_frontend() {
  echo "********************************"
  echo "* Copying Frontend Files       *"
  echo "********************************"
  mkdir public
  cp -r ../frontend/* public
}

install_dependencies() {
  echo "********************************"
  echo "* Installing Dependencies      *"
  echo "********************************"
  curl -s https://getcomposer.org/installer | php
  php composer.phar install
}

create_mongo_service() {
  echo "********************************"
  echo "* Creating Mongo DB Service    *"
  echo "********************************"
  cf create-service mongodb 100 todo-mongo-db
}

create_cloudant_service() {
  echo "********************************"
  echo "* Creating Cloudant Service    *"
  echo "********************************"
  cf create-service cloudantNoSQLDB Lite todo-couch-db
}

deploy() {
  echo "********************************"
  echo "* Deploying To BlueMix         *"
  echo "********************************"
  read -p "What would you like to call this app?`echo $'\n> '`" name
  cf push $name
}

clean
install_dependencies
copy_frontend

if [ "$1" == "mongo" ]
then
  copy_mongo_index
  copy_mongo_manifest
  create_mongo_service
  deploy
elif [ "$1" == "cloudant" ]
then
  copy_cloudant_index
  copy_cloudant_manifest
  create_cloudant_service
  deploy
else
  echo "Specify either mongo or cloudant as the first argument."
fi

exit $?