<?php
/*
 * Copyright IBM Corp. 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /**
  * This PHP file uses the Slim Framework to construct a REST API.
  * Most of the heavy lifting happens in MongoApp.
  */
require 'vendor/autoload.php';
require_once('./MongoApp.php');
$app = new \Slim\Slim();

$app->get('/api/todos', function () {
    echo json_encode(MongoApp::Instance()->get());
});

$app->post('/api/todos', function() {
	global $app;
	$todo = json_decode($app->request()->getBody(), true);
    echo json_encode(MongoApp::Instance()->post($todo));
});

$app->delete('/api/todos/:id', function($id) {
	global $app;
    MongoApp::Instance()->delete($id);
    $app->response()->status(204);
});

$app->put('/api/todos/:id', function($id) {
	global $app;
	$todo = json_decode($app->request()->getBody(), true);
    echo json_encode(MongoApp::Instance()->put($id, $todo));
});

$app->run();