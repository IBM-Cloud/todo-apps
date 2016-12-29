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

require_once('SagCache.php');
require_once('SagException.php');

/**
 * Stores cached items in PHP's memory as serialized JSON, which was
 * benchmarked as being faster than serliaze() and clone.
 *
 * Cache sizing is not supported with this caching mechanism. This is because
 * PHP is not accurate at reporting memory allocation and it does not make
 * sense to increase latency to implement a broken feature.
 *
 * @package Cache 
 * @version 0.9.0
 */
class SagMemoryCache extends SagCache {
  private $cache;

  public function __construct() {
    parent::__construct();
    $this->cache = array();
  }

  public function set($url, &$item) {
    if(empty($url)) {
      throw new SagException('You need to provide a URL to cache.');
    }

    if(!parent::mayCache($item)) {
      return false;
    }

    // If it already exists, then remove the old version but keep a copy
    if(isset($this->cache[$url])) {
      $oldCopy = json_decode($this->cache[$url]);
      self::remove($url);
    }

    $this->cache[$url] = json_encode($item);

    return (isset($oldCopy) && is_object($oldCopy)) ? $oldCopy : true;
  }

  public function get($url) {
    return (isset($this->cache[$url])) ? json_decode($this->cache[$url]) : null;
  }

  public function remove($url) {
    unset($this->cache[$url]);

    return true;
  }

  public function clear() {
    unset($this->cache);
    $this->cache = array();

    return true;
  }

  public function setSize($bytes) {
    throw new SagException('Cache sizes are not supported in SagMemoryCache - caches have infinite size.');
  }

  public function getSize() {
    throw new SagException('Cache sizes are not supported in SagMemoryCache - caches have infinite size.');
  }

  public function getUsage() {
    throw new SagException('Cache sizes are not supported in SagMemoryCache - caches have infinite size.');
  }
} 
?>
