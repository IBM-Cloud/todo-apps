<?php
/*
 * Copyright IBM Corp. 2016
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
require_once('./sag/Sag.php');

/**
 * Class to handle performing basic CRUD operations on a Couch DB.
 * This class uses the Sag library to talk to the Couch DB.
 */
final class CouchApp {
	private static $inst = null;
    private $sag;

    public static function Instance() {
        if (self::$inst === null) {
            self::$inst = new CouchApp();
        }
        return self::$inst;
    }

    private function __construct() {
		#If running locally enter your own host, port, username and password
    $host = '';
		$port = '';
		$username = '';
		$password = '';

		if($vcapStr = getenv('VCAP_SERVICES')) {
			$vcap = json_decode($vcapStr, true);
			foreach ($vcap as $serviceTypes) {
				foreach ($serviceTypes as $service) {
					if($service['name'] == 'todo-db-php') {
						$credentials = $service['credentials'];
						$username = $credentials['username'];
						$password = $credentials['password'];
						$parsedUrl = parse_url($credentials['url']);
						$host = $parsedUrl['host'];
						$port = isset($parsedUrl['port']) ?
						$parsedUrl['port'] : $parsedUrl['scheme'] == 'http' ?
						'80' : '443';
						break;
					}
				}
			}
		}
		$this->sag = new Sag($host, $port);
		$this->sag->useSSL(true);
		$this->sag->login($username, $password);
		$this->sag->setDatabase('bluemix-todo', true);
		$this->createView();
    }

    /**
	 * Transforms the ToDo JSON from the Mongo DB to the JSON
	 * the client will expect.
	 */
    private function toClientToDo($couchToDo) {
		$clientToDo = array('id' => $couchToDo->id);
		$clientToDo['title'] = $couchToDo->value->title;
		$clientToDo['completed'] = $couchToDo->value->completed;
		$clientToDo['order'] = $couchToDo->key;
		return $clientToDo;
	}

	/**
	 * Creates a view to use in the DB if one does not already exist.
	 */
	private function createView() {
		try {
			$view = $this->sag->get('_design/todos');
		} catch(SagCouchException $e) {
			$allToDos = array('reduce' => '_count',
				'map' => 'function(doc){if(doc.title && doc.completed != null){emit(doc.order,{title: doc.title,completed: doc.completed})}}');
			$views = array('allTodos' => $allToDos);
			$designDoc = array('views' => $views);
			$this->sag->put('_design/todos', $designDoc);
		}
	}

	/**
	 * Gets all ToDos from the DB.
	 */
	public function get() {
		$docs = $this->sag->get('_design/todos/_view/allTodos?reduce=false')->body;
		$todos = array();
		foreach ($docs->rows as $row) {
			$todos[] = $this->toClientToDo($row);
		}
		return $todos;
	}

	/**
	 * Creates a new ToDo in the DB.
	 */
	public function post($todo) {
		$resp = $this->sag->post($todo);
		$todo['id'] = $resp->body->id;
		return $todo;
	}

	/**
	 * Updates a ToDo in the DB.
	 */
	public function put($id, $todo) {
		$couchTodo = $this->sag->get($id)->body;
    	$couchTodo->title = $todo['title'];
    	$couchTodo->completed = $todo['completed'];
    	$couchTodo->order = $todo['order'];
    	$this->sag->put($id, $couchTodo);
    	$couchTodo->id = $id;
    	unset($couchTodo->_id);
    	unset($couchTodo->_rev);
    	return $couchTodo;
	}

	/**
	 * Deletes a ToDo from the DB.
	 */
	public function delete($id) {
		$rev = $this->sag->get($id)->body->_rev;
		$this->sag->delete($id, $rev);
	}
}
