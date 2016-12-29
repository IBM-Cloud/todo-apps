<?php
/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

require_once('Sag.php');

/**
 * Provides utilities to work with and manage CouchDB users, which wraps the
 * Sag class.
 *
 * @version 0.9.0
 * @package Utils
 */
class SagUserUtils {
  private static $USER_ID_PREFIX = 'org.couchdb.user:';

  /**
   * @param Sag $sag An instantiated copy of Sag that you want this class to
   * use. If you don't specify a database (empty($sag->currentDatabase())) then
   * it will be set to '_users'.
   *
   * @return SagUserUtils
   */
  public function __construct($sag) {
    if(!($sag instanceof Sag)) {
      throw new SagException('Tried to call setSag() with a non-Sag implementation.');
    }

    //Use the database if they pre-selected it, else default to Couch's default.
    $db = $sag->currentDatabase();

    if(empty($db)) {
      $sag->setDatabase('_users');
    }

    $this->sag = $sag;
  }

  /**
   * Creates a user and returns the server's response. If no name is provided,
   * then the id is duplicated into that position.
   *
   * @param string $id The user's ID without the 'org.couchdb.user:' prefix.
   *
   * @param string $password The password, which will be salted and encrypted
   * for you.
   *
   * @param string $name (OPTIONAL) The user's name. If not provided, then it
   * will be the same as the provided $id.
   *
   * @param array $roles (OPTIONAL) An array of roles (strings) for the user.
   *
   * @return object The server's response, as you would expect from Sag's put()
   * function.
   *
   * @see Sag::put()
   */
  public function createUser($id, $password, $name = null, $roles = array()) {
    if(!is_string($id) || empty($id)) {
      throw new SagException('Invalid user id.');
    }

    if(!is_string($password) || empty($password)) {
      throw new SagException('Invalid user password.');
    }

    if($name && (!is_string($name) || empty($name))) {
      throw new SagException('Invalid user name.');
    }

    if(!is_array($roles)) {
      throw new SagException('Invalid list of roles: it must be an array.');
    }
    else {
      foreach($roles as $k => $v) {
        if(!is_int($k)) {
          throw new SagException('The roles array cannot be an associative array.');
        }

        if(!is_string($v) || empty($v)) {
          throw new SagException("An invalid role was specified at array position $k");
        }
      }
    }

    if(!$name) {
      $name = $id;
    }

    $id = self::$USER_ID_PREFIX.$id;

    $salt = self::makeSalt();

    return $this->sag->put($id, array(
      '_id' => $id,
      'type' => 'user',
      'name' => $name,
      'roles' => $roles,
      'password_sha' => sha1($password . $salt),
      'salt' => $salt
    ));
  }

  /**
   * Returns the user document from the database (just the response body, not
   * HTTP info).
   *
   * @param string $id The user's _id.
   *
   * @param bool $hasPrepend Specify whether the $id you are providing has
   * 'org.couchdb.user:' prepended to it. If it doesn't (set to false, which is
   * the default) then the string will be prepended for you.
   *
   * @return object The user document: just the body property from Sag->get()'s
   * return value.
   */
  public function getUser($id, $hasPrepend = false) {
    return $this->sag->get((($hasPrepend) ? '' : self::$USER_ID_PREFIX) . $id);
  }

  /**
   * Takes a user document and new password, generates a salt, and updates the
   * password for that user document. You can optionally have the function send
   * the updated document to the server as well.
   *
   * @param object $doc The user document. Expected to look like what
   * SagUserUtils->getUser() returns.
   *
   * @param string $newPassword The new password for the user.
   *
   * @param bool $upload Whether to PUT the document to the server after
   * updating it. Defaults to false.
   *
   * @return object If you set $upload to false, then just the updated document
   * is returned. If you set $upload to true, then the result of Sag->put() is
   * returned, so you get the updated document and HTTP information.
   */
  public function changePassword($doc, $newPassword, $upload = false) {
    if(empty($doc->_id)) {
      throw new SagException('This does not look like a document: there is no _id.');
    }

    if(empty($doc->salt) || empty($doc->password_sha)) {
      throw new SagException('This does not look like a user or it is an admin. Change admin passwords via the server config.');
    }

    if(empty($newPassword)) {
      throw new SagException('Empty password are not allowed.');
    }

    $doc->password_sha = sha1($newPassword + self::makeSalt());

    return ($upload) ? $this->sag->put($doc->_id, $doc) : $doc;
  }

  /**
   * Generates a salt that will be used when setting users' passwords. By
   * default it requests a uuid from the CouchDB server you specified.
   *
   * @return string A randomly generated string of characters that will be used
   * to salt users' passwords.
   */
  public function makeSalt() {
    return $this->sag->generateIDs(1)->body->uuids[0];   
  }
}
?>
