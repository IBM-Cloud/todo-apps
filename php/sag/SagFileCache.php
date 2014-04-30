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
 * Cache to the local hard disk. Uses /tmp by default, but you can specify
 * another location.
 *
 * Cache keys are used for file names and the contents are JSON. System file
 * sizes are used to calculate the cache's current size.
 *
 * Every operation goes to the disk, so this cache will not be ideal for high
 * I/O operations.
 *
 * @package Cache 
 * @version 0.9.0
 */
class SagFileCache extends SagCache {
  private static $fileExt = ".sag";

  private $fsLocation;

  /**
   * @param string $location The file system path to the directory that should
   * be used to store the cache files. The local system's temp directory is
   * used by default.
   * @return SagFileCache
   */
  public function __construct($location) {
    if(!is_dir($location)) {
      throw new SagException("The provided cache location is not a directory.");
    }

    if(!is_readable($location) || !is_writable($location)) {
      throw new SagException("Insufficient privileges to the supplied cache directory.");
    }

    parent::__construct();

    $this->fsLocation = rtrim($location, "/ \t\n\r\0\x0B");

    /* 
     * Just update - don't freak out if the size isn't right, as the user might
     * update it to non-default, they might not do anything with the cache,
     * they might clean it themselves, etc. give them time. We'll freak when we
     * add.
     */
    foreach(glob($this->fsLocation."/*".self::$fileExt) as $file) {
      self::addToSize(filesize($file));
    }
  }   

  /**
   * Generates the full filename/path that would be used for a given URL's
   * cache object.
   *
   * @param string $url The URL for the cached item.
   * @return string
   */
  public function makeFilename($url) {
    return "$this->fsLocation/".self::makeKey($url).self::$fileExt;
  }

  public function set($url, &$item) {
    if(empty($url)) {
      throw new SagException('You need to provide a URL to cache.');
    }

    if(!parent::mayCache($item)) {
      return false;
    }

    $serialized = json_encode($item);
    $target = self::makeFilename($url);

    // If it already exists, then remove the old version but keep a copy
    if(is_file($target)) {
      $oldCopy = self::get($url);
      self::remove($url);
    }

    $fh = fopen($target, "w"); //in case self::remove() didn't get it?

    fwrite($fh, $serialized, strlen($serialized)); //don't throw up if we fail - we're not mission critical
    self::addToSize(filesize($target));

    fclose($fh);

    // Only return the $oldCopy if it exists
    return (isset($oldCopy) && is_object($oldCopy)) ? $oldCopy : true;
  }

  public function get($url) {
    $target = self::makeFilename($url);

    if(!is_file($target)) {
      return null;
    }

    if(!is_readable($target)) {
      throw new SagException("Could not read the cache file for $url at $target - please check its permissions.");
    }

    return json_decode(file_get_contents($target));
  }

  public function remove($url) {
    $target = $this->makeFilename($url);
    return self::removeFile($target);
  }

  public function clear() {
    $part = false;

    foreach(glob($this->fsLocation."/*".self::$fileExt) as $file) {
      if(!self::removeFile($file)) {
        $part = true;
      }
    } 

    return !$part;
  }

  private function removeFile($path) {
    $size = filesize($path);

    if(!unlink($path)) {
      return false;
    }

    self::addToSize(-$size);

    return true;
  }
} 
?>
