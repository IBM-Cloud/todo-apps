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

require_once("SagException.php");

/**
 * All the caching systems that Sag can leverage must extend this. The cache
 * values should always be the object that Sag::procPacket() would return.
 *
 * The default cache size is 1MB (one million bytes).
 *
 * Cache values are objects (stdClass for PHP storage, or JSON for external
 * storage).
 *
 * @package Cache 
 * @version 0.9.0
 */
abstract class SagCache {
  private $maxSize;                                     //in bytes

  private $currentSize;                                 //in bytes

  public function __construct() {
    $this->currentSize = 0;
    $this->maxSize = 1000000; 
  }

  /** 
   * Returns the cached object or null if nothing is cached.
   * 
   * @param string $url The URL of the request we're caching.  
   * @return object
   */
  abstract public function get($url);

  /**
   * Caches the item returned at the provided key, replacing any pre-existing
   * cached item. If the cache's size will be exceeded by caching the new item,
   * then it will remove items from the cache until there is sufficient room.
   *
   * Returns false if the item cannot be added to the cache for any reason:
   * exceeds the cache size, invalid type, or relevant HTTP headers.
   *
   * Returns true if we were able to add the item, and there was no previously
   * cached item.
   *
   * Returns the previously cached item if there was one and we were able to
   * add the new item to the cache.
   *
   * Sag will refuse to cache the object by throwing a SagException if adding
   * the file to the cache would exceed 95% of the disk or partition's free
   * space.
   *
   * @param string $url The URL of the request we're caching.
   * @param object $item The response we're caching.
   * @return mixed
   */
  abstract public function set($url, &$item);

  /**
   * Removes the item from the cache and returns it (null if nothing was
   * cached).
   *
   * @param string $url The URL of the response we're removing from the cache.
   * @return mixed
   */
  abstract public function remove($url);

  /**
   * Clears the whole cache without applying any logic.
   *
   * Returns true if the entire cache was cleared, otherwise false if only part
   * or none of it was cleared.
   *
   * @return bool
   */
  abstract public function clear();

  /**
   * Sets the max size of the cache in bytes.
   * 
   * @param int $bytes The size of the cache in bytes (>0).
   */
  public function setSize($bytes) {
    if(!is_int($bytes) || $bytes <= 0) {
      throw new Exception("The cache size must be a positive integer (bytes).");
    }

    $this->maxSize = $bytes;
  }

  /**
   * Returns the max size of the cache, irrespective of what is or isn't in the
   * cache.
   *
   * @return int
   */
  public function getSize() {
    return $this->maxSize;
  }

  /**
   * Returns the total size of the items in the cache in bytes. Not reliable if
   * you're using SagMemoryCache.
   * 
   * @return int
   */
  public function getUsage() {
    return $this->currentSize;
  }

  /**
   * Generates the hash of the provided URL that will be used as the cache key.
   *
   * @param string $url The URL to hash.
   * @return string
   */
  public function makeKey($url) {
    return sha1($url);
  }

  protected function addToSize($amt) {
    if(!is_int($amt) && !is_float($amt)) {
      throw new SagException('Invalid cache size modifier.');
    }

    $this->currentSize += $amt;
  }

  /**
   * Returns whether or not the item may be cached. It has to be a stdClass
   * that Sag would return, with a valid E-Tag, and no cache headers that tell
   * us to not cache.
   *
   * @param The item that we're trying to cache - it should be a response as a
   * stdClass.
   * @return bool
   */
  protected function mayCache($item) {
    return (
      isset($item) && 
      is_object($item) && 
      isset($item->headers) &&
      is_string($item->headers->etag) &&
      !empty($item->headers->etag) &&
      isset($item->body) &&
      is_object($item->body)
    );
  }
}
