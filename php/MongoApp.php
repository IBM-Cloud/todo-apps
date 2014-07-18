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
  * Class to handle performing basic CRUD operations to a Mongo
  * DB.  This class assumes you have the Mongo DB driver extension
  * installed in PHP.
  */
final class MongoApp {
	private static $inst = null;
	private $mongoUrl = 'mongodb://localhost:27017';
	private $dbName = 'db';

    public static function Instance() {
        if (self::$inst === null) {
            self::$inst = new MongoApp();
        }
        return self::$inst;
    }

    private function __construct() {
		if($vcapStr = getenv('VCAP_SERVICES')) {
			$vcap = json_decode($vcapStr, true);
			foreach ($vcap as $serviceTypes) {
				foreach ($serviceTypes as $service) {
					if($service['name'] == 'todo-mongo-db') {
						$credentials = $service['credentials'];
						$this->mongoUrl = $credentials['uri'];
						$parsedUrl = parse_url($credentials['uri']);
						$this->dbName = preg_replace('/\/(.*)/', '$1', $parsedUrl['path']);
						break;
					}
				}
			}
		}
    }

    private function getConnection() {
		return new Mongo($this->mongoUrl);
	}

	private function getTodosCollection($conn) {
		$name = $this->dbName;
		$mongodb = $conn->$name;
    	return $mongodb->todos;
	}

	/**
	 * Transforms the ToDo JSON from the Mongo DB to the JSON
	 * the client will expect.
	 */
	private function toClientToDo($mongoTodo) {
		$mongoTodo['id'] = $mongoTodo['_id']->{'$id'};
		unset($mongoTodo['_id']);
		return $mongoTodo;
	}

	/**
	 * Gets all ToDos from the DB.
	 */
	public function get() {
		$conn = $this->getConnection();
    	$collection = $this->getTodosCollection($conn);
		$cursor = $collection->find();
		$result = array();
		foreach ($cursor as $todo) {
			$result[] = $this->toClientToDo($todo);
		}
		$conn->close();
		return $result;
	}

	/**
	 * Creates a new ToDo in the DB.
	 */
	public function post($todo) {
		$conn = $this->getConnection();
    	$collection = $this->getTodosCollection($conn);
    	$collection->save($todo);
    	$conn->close();
		return $this->toClientToDo($todo);
	}

	/**
	 * Updates a ToDo in the DB.
	 */
	public function put($id, $todo) {
		$conn = $this->getConnection();
    	$collection = $this->getTodosCollection($conn);
    	$mongoTodo = $collection->findOne(array('_id' => new MongoId($id)));
    	$mongoTodo['title'] = $todo['title'];
    	$mongoTodo['completed'] = $todo['completed'];
    	$mongoTodo['order'] = $todo['order'];
    	$collection->save($mongoTodo);
    	$conn->close();
    	return $this->toClientToDo($mongoTodo);
	}

	/**
	 * Deletes a ToDo from the DB.
	 */
	public function delete($id) {
		$conn = $this->getConnection();
    	$collection = $this->getTodosCollection($conn);
    	$collection->remove(array('_id' => new MongoId($id)));
    	$conn->close();
	}
}