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

/**
 * This exception is thrown when CouchDB reports an error, such as sending
 * malformed JSON, and not Sag.
 *
 * The exception's code ($e->getCode()) is the HTTP status code. For example,
 * if the requested document isn't found, then the code would be set to "404"
 * (string).
 *
 * @version 0.9.0
 * @package Core
 */
class SagCouchException extends Exception {
  public function __construct($msg = "", $code = 0) {
    parent::__construct("CouchDB Error: $msg", $code);
  }
}
