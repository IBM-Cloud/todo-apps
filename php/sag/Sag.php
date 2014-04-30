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

require_once('SagException.php');
require_once('SagCouchException.php');
require_once('httpAdapters/SagNativeHTTPAdapter.php');
require_once('httpAdapters/SagCURLHTTPAdapter.php');

/**
 * The Sag class provides the core functionality for talking to CouchDB.
 *
 * @version 0.9.0
 * @package Core
 */
class Sag {
  /**
   * @var string Used by login() to use HTTP Basic Authentication.
   * @static
   */
  public static $AUTH_BASIC = "AUTH_BASIC";

  /**
   * @var string Used by login() to use HTTP Cookie Authentication.
   * @static
   */
  public static $AUTH_COOKIE = "AUTH_COOKIE";

  /**
   * @var string Used to identify SagNativeHTTPAdapter by setHTTPAdapter() and
   * company.
   * @static
   */
  public static $HTTP_NATIVE_SOCKETS = 'HTTP_NATIVE_SOCKETS';

  /**
   * @var string Used to identify SagCURLHTTPAdapter by setHTTPAdapter() and
   * company.
   * @static
   */
  public static $HTTP_CURL = 'HTTP_CURL';

  private $db;                          //Database name to hit.
  private $host;                        //IP or address to connect to.
  private $port;                        //Port to connect to.
  private $pathPrefix = '';             //Prepended to URLs.

  private $user;                        //Username to auth with.
  private $pass;                        //Password to auth with.
  private $authType;                    //One of the Sag::$AUTH_* variables
  private $authSession;                 //AuthSession cookie value from/for CouchDB

  private $cache;

  private $staleDefault;                //Whether or not to use ?stale=ok on all design doc calls

  private $globalCookies = array();

  private $httpAdapter;
  private $httpAdapterType;

  /**
   * @param string $host (OPTIONAL) The host's IP or address of the Couch we're
   * connecting to. Defaults to '127.0.0.1'.
   *
   * @param string $port (OPTIONAL) The host's port that Couch is listening on.
   * Defaults to '5984'.
   */
  public function __construct($host = "127.0.0.1", $port = "5984")
  {
    $this->host = $host;
    $this->port = $port;

    //sets to the default by ... default
    $this->setHTTPAdapter();
  }

  /**
   * Set which HTTP library you want to use for communicating with CouchDB.
   *
   * @param string $type The type of adapter you want to use. Should be one of
   * the Sag::$HTTP_* variables.
   * @return Sag Returns $this.
   *
   * @see Sag::$HTTP_NATIVE_SOCKETS
   * @see Sag::$HTTP_CURL
   */
  public function setHTTPAdapter($type = null) {
    if(!$type) {
      $type = extension_loaded("curl") ? self::$HTTP_CURL : self::$HTTP_NATIVE_SOCKETS;
    }

    // nothing to be done
    if($type === $this->httpAdapterType) {
      return true;
    }

    // remember what was already set (ie., might have called decode() already)
    $prevDecode = null;
    $prevTimeouts = null;
    if($this->httpAdapter) {
      $prevDecode = $this->httpAdapter->decodeResp;
      $prevTimeouts = $this->httpAdapter->getTimeouts();
    }

    // the glue
    switch($type) {
      case self::$HTTP_NATIVE_SOCKETS:
        $this->httpAdapter = new SagNativeHTTPAdapter($this->host, $this->port);
        break;

      case self::$HTTP_CURL:
        $this->httpAdapter = new SagCURLHTTPAdapter($this->host, $this->port);
        break;

      default:
        throw SagException("Invalid Sag HTTP adapter specified: $type");
    }

    // restore previous decode value, if any
    if(is_bool($prevDecode)) {
      $this->httpAdapter->decodeResp = $prevDecode;
    }

    // restore previous timeout vlaues, if any
    if(is_array($prevTimeouts)) {
      $this->httpAdapter->setTimeoutsFromArray($prevTimeouts);
    }

    $this->httpAdapterType = $type;

    return $this;
  }

  /**
   * Returns the current HTTP adapter being used.
   *
   * @return string Will be equal to Sag::$HTTP_NATIVE_SOCKETS or
   * Sag::$HTTP_CURL.
   */
  public function currentHTTPAdapter() {
    return $this->httpAdapterType;
  }

  /**
   * Updates the login credentials in Sag that will be used for all further
   * communications. Pass null to both $user and $pass to turn off
   * authentication, as Sag does support blank usernames and passwords - only
   * one of them has to be set for packets to be sent with authentication.
   *
   * Cookie authentication will cause a call to the server to establish the
   * session, and will throw an exception if the credentials weren't valid.
   *
   * @param string $user The username you want to login with. (null for none)
   * @param string $pass The password you want to login with. (null for none)
   * @param string $type The type of login system being used. Defaults to
   * Sag::$AUTH_BASIC.
   *
   * @return mixed Returns true if the input was valid. If using $AUTH_COOKIE,
   * then the autoSession value will be returned. Throws on failure.
   *
   * @see $AUTH_BASIC
   * @see $AUTH_COOKIE
   */
  public function login($user, $pass, $type = null) {
    if($type == null) {
      $type = Sag::$AUTH_BASIC;
    }

    $this->authType = $type;

    switch($type) {
      case Sag::$AUTH_BASIC:
        //these will end up in a header, so don't URL encode them
        $this->user = $user;
        $this->pass = $pass;

        return true;
        break;

      case Sag::$AUTH_COOKIE:
        $user = urlencode($user);
        $pass = urlencode($pass);

        $res = $this->procPacket(
          'POST',
          '/_session',
          sprintf('name=%s&password=%s', $user, $pass),
          array('Content-Type' => 'application/x-www-form-urlencoded')
        );

        $this->authSession = $res->cookies->AuthSession;

        return $this->authSession;

        break;
    }

    //should never reach this line
    throw new SagException("Unknown auth type for login().");
  }

  /**
   * Get current session information on the server with /_session.
   *
   * @return stdClass
   */
  public function getSession() {
    return $this->procPacket('GET', '/_session');
  }

  /**
   * Sets whether Sag will decode CouchDB's JSON responses with json_decode()
   * or to simply return the JSON as a string. Defaults to true.
   *
   * @param bool $decode True to decode, false to not decode.
   * @return Sag Returns $this.
   */
  public function decode($decode) {
    if(!is_bool($decode)) {
      throw new SagException('decode() expected a boolean');
    }

    $this->httpAdapter->decodeResp = $decode;

    return $this;
  }

  /**
   * Performs an HTTP GET operation for the supplied URL. The database name you
   * provided is automatically prepended to the URL, so you only need to give
   * the portion of the URL that comes after the database name.
   *
   * You are responsible for URL encoding your own parameters.
   *
   * @param string $url The URL, with or without the leading slash.
   * @return mixed
   */
  public function get($url) {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    //The first char of the URL should be a slash.
    if(strpos($url, '/') !== 0) {
      $url = "/$url";
    }

    $url = "/{$this->db}$url";

    if($this->staleDefault) {
      $url = self::setURLParameter($url, 'stale', 'ok');
    }

    //Deal with cached items
    $response = null;
    if($this->cache) {
      $prevResponse = $this->cache->get($url);

      if($prevResponse) {
        $response = $this->procPacket('GET', $url, null, array('If-None-Match' => $prevResponse->headers->etag));

        if($response->headers->_HTTP->status == 304) {
          //cache hit
          $response->fromCache = true;

          return $prevResponse;
        }
      
        $this->cache->remove($url); 
      }

      unset($prevResponse);
    }

    /*
     * Not caching, or we are caching but there's nothing cached yet, or our
     * cached item is no longer good.
     */
    if(!$response) {
      $response = $this->procPacket('GET', $url);
    }

    if($this->cache) {
      $this->cache->set($url, $response);
    }

    return $response;
  }

  /**
   * Performs an HTTP HEAD operation for the supplied document. This operation
   * does not try to read from a provided cache, and does not cache its
   * results.
   *
   * @see http://wiki.apache.org/couchdb/HTTP_Document_API#HEAD
   *
   * @param string $url The URL, with or without the leading slash.
   * @return mixed
   */
  public function head($url) {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    //The first char of the URL should be a slash.
    if(strpos($url, '/') !== 0) {
      $url = "/$url";
    }

    if($this->staleDefault) {
      $url = self::setURLParameter($url, 'stale', 'ok');
    }

    //we're only asking for the HEAD so no caching is needed
    return $this->procPacket('HEAD', "/{$this->db}$url");
  }

  /**
   * DELETE's the specified document.
   *
   * @param string $id The document's _id.
   * @param string $rev The document's _rev.
   *
   * @return mixed
   */
  public function delete($id, $rev)
  {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    if(!is_string($id) || !is_string($rev) || empty($id) || empty($rev)) {
      throw new SagException('delete() expects two strings.');
    }

    $url = "/{$this->db}/$id";

    if($this->cache) {
      $this->cache->remove($url);
    }

    return $this->procPacket('DELETE', $url.'?rev='.urlencode($rev));
  }

  /**
   * PUT's the data to the document.
   *
   * @param string $id The document's _id.
   * @param mixed $data The document, which should have _id and _rev
   * properties. Can be an object, array, or string.
   *
   * @return mixed
   */
  public function put($id, $data)
  {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    if(!is_string($id)) {
      throw new SagException('put() expected a string for the doc id.');
    }

    if(!isset($data) || (!is_object($data) && !is_string($data) && !is_array($data))) {
      throw new SagException('put() needs an object for data - are you trying to use delete()?');
    }

    $toSend = (is_string($data)) ? $data : json_encode($data);

    $url = "/{$this->db}/$id";
    $response = $this->procPacket('PUT', $url, $toSend);

    unset($toSend);

    /*
     * We're going to pretend like we issued a GET or HEAD by replacing the PUT
     * response's body with the data we sent. We then update that data with the
     * _rev from the PUT's response's body. Of course this should only run when
     * there is a successful write to the database: we don't want to be caching
     * failures.
     */
    if($this->cache && $response->body->ok) {
      if(is_string($data)) {
        $data = json_decode($data);
      }

      $data->_rev = $response->body->rev;

      $toCache = clone $response;
      $toCache->body = $data;

      $this->cache->set($url, $toCache);

      unset($toCache);
    }

    return $response;
  }


  /**
   * POST's the provided document. When using a SagCache, the created document
   * and response are not cached.
   *
   * @param mixed $data The document that you want created. Can be an object,
   * array, or string.
   * @param string $path Can be the path to a view or /all_docs. The database
   * will be prepended to the value.
   *
   * @return mixed
   */
  public function post($data, $path = null) {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    if(!isset($data) || (!is_string($data) && !is_object($data) && !is_array($data))) {
      throw new SagException('post() needs an object for data.');
    }

    if(!is_string($data)) {
      $data = json_encode($data);
    }

    if(is_string($path) && !empty($path)) {
      $path = ((substr($path, 0, 1) != '/') ? '/' : '').$path;
    }
    else if(isset($path)) {
      throw new SagException('post() needs a string for a path.');
    }

    return $this->procPacket('POST', "/{$this->db}{$path}", $data);
  }

  /**
   * Bulk pushes documents to the database.
   * 
   * This function does not leverage the caching mechanism you specify with
   * setCache().
   *
   * @param array $docs An array of the documents you want to be pushed; they
   * can be JSON strings, objects, or arrays.
   * @param bool $allOrNothing Whether to treat the transactions as "all or
   * nothing" or not. Defaults to false.
   *
   * @return mixed
   */
  public function bulk($docs, $allOrNothing = false) {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    if(!is_array($docs)) {
      throw new SagException('bulk() expects an array for its first argument');
    }

    if(!is_bool($allOrNothing)) {
      throw new SagException('bulk() expects a boolean for its second argument');
    }

    $data = new stdClass();

    //Only send all_or_nothing if it's non-default (true), saving bandwidth.
    if($allOrNothing) {
      $data->all_or_nothing = $allOrNothing;
    }

    $data->docs = $docs;

    return $this->procPacket("POST", "/{$this->db}/_bulk_docs", json_encode($data));
  }

  /**
   * COPY's the document.
   *
   * If you are using a SagCache and are copying to an existing destination,
   * then the result will be cached (ie., what's copied to the /$destID URL).
   *
   * @param string The _id of the document you're copying.
   * @param string The _id of the document you're copying to.
   * @param string The _rev of the document you're copying to. Defaults to
   * null.
   *
   * @return mixed
   */
  public function copy($srcID, $dstID, $dstRev = null) {
    if(!$this->db) {
      throw new SagException('No database specified');
    }

    if(empty($srcID) || !is_string($srcID)) {
      throw new SagException('copy() got an invalid source ID');
    }

    if(empty($dstID) || !is_string($dstID)) {
      throw new SagException('copy() got an invalid destination ID');
    }

    if($dstRev != null && (empty($dstRev) || !is_string($dstRev))) {
      throw new SagException('copy() got an invalid source revision');
    }

    $headers = array(
      "Destination" => "$dstID".(($dstRev) ? "?rev=$dstRev" : "")
    );

    $response = $this->procPacket('COPY', "/{$this->db}/$srcID", null, $headers); 

    return $response;
  }

  /**
   * Sets which database Sag is going to send all of its database related
   * communications to (ex., dealing with documents).
   *
   * When specifying that the database should be created if it doesn't already
   * exists, this will cause an HTTP GET to be sent to /dbName and
   * createDatabase($db) if a 404 response is returned. So, only turn it on if
   * it makes sense for your application, because it could cause needless HTTP
   * GET calls.
   *
   * @param string $db The database's name, as you'd put in the URL. This
   * string will be URL encoded using PHP's urlencode().
   * @param bool $createIfNotFound Whether to try and create the specified
   * database if it doesn't exist yet (checks every time this is called).
   *
   * @return Sag Returns $this. Throws on failure.
   */
  public function setDatabase($db, $createIfNotFound = false) {
    if($this->db != $db || $createIfNotFound) {
      if(!is_string($db)) {
        throw new SagException('setDatabase() expected a string.');
      }

      $db = urlencode($db);

      if($createIfNotFound) {
        try {
          self::procPacket('HEAD', "/{$db}");
        }
        catch(SagCouchException $e) {
          if($e->getCode() != 404) {
            throw $e; //these are not the errors that we are looking for
          }

          self::createDatabase($db);
        }
      }

      $this->db = $db;
    }

    return $this;
  }

  /**
   * Gets all the documents in the database with _all_docs. Its results will
   * not be cached by SagCache.
   *
   * @param bool $incDocs Whether to include the documents or not. Defaults to
   * false.
   * @param int $limit Limits the number of documents to return. Must be >= 0,
   * or null for no limit. Defaults to null (no limit).
   * @param string $startKey The startkey variable (valid JSON). Defaults to
   * null.
   * @param string $endKey The endkey variable (valid JSON). Defaults to null.
   * @param array $keys An array of keys (strings) of the specific documents
   * you're trying to get.
   * @param bool $descending Whether to sort the results in descending order or
   * not.
   *
   * @return mixed
   */
  public function getAllDocs($incDocs = false, $limit = null, $startKey = null, $endKey = null, $keys = null, $descending = false) {
    if(!$this->db) {
      throw new SagException('No database specified.');
    }

    $qry = array();

    if($incDocs !== false) {
      if(!is_bool($incDocs)) {
        throw new SagException('getAllDocs() expected a boolean for include_docs.');
      }

      $qry[] = "include_docs=true";
    }

    if(isset($startKey)) {
      if(!is_string($startKey)) {
        throw new SagException('getAllDocs() expected a string for startkey.');
      }

      $qry[] = 'startkey='.urlencode($startKey);
    }

    if(isset($endKey)) {
      if(!is_string($endKey)) {
        throw new SagException('getAllDocs() expected a string for endkey.');
      }

      $qry[] = 'endkey='.$endKey;
    }

    if(isset($limit)) {
      if(!is_int($limit) || $limit < 0) {
        throw new SagException('getAllDocs() expected a positive integeter for limit.');
      }

      $qry[] = 'limit='.urlencode($limit);
    }

    if($descending !== false) {
      if(!is_bool($descending)) {
        throw new SagException('getAllDocs() expected a boolean for descending.');
      }

      $qry[] = "descending=true";
    }

    $qry = '?'.implode('&', $qry);

    if(isset($keys))
    {
      if(!is_array($keys)) {
        throw new SagException('getAllDocs() expected an array for the keys.');
      }

      $data = new stdClass();
      $data->keys = $keys;

      return $this->procPacket('POST', "/{$this->db}/_all_docs$qry", json_encode($data));
    }

    return $this->procPacket('GET', "/{$this->db}/_all_docs$qry");
  }

  /**
   * Gets all the databases on the server with _all_dbs.
   *
   * @return mixed
   */
  public function getAllDatabases() {
    return $this->procPacket('GET', '/_all_dbs');
  }

  /**
   * Uses CouchDB to generate IDs.
   *
   * @param int $num The number of IDs to generate (>= 0). Defaults to 10.
   * @return mixed
   */
  public function generateIDs($num = 10) {
    if(!is_int($num) || $num < 0) {
      throw new SagException('generateIDs() expected an integer >= 0.');
    }

    //don't need to URL encode since ints are, well, harmless lil' ol' ints
    return $this->procPacket('GET', "/_uuids?count=$num");
  }

  /**
   * Creates a database with the specified name.
   *
   * @param string $name The name of the database you want to create.
   *
   * @return mixed
   */
  public function createDatabase($name) {
    if(empty($name) || !is_string($name)) {
      throw new SagException('createDatabase() expected a valid database name');
    }

    return $this->procPacket('PUT', "/$name");
  }

  /**
   * Deletes the specified database.
   *
   * @param string $name The database's name.
   *
   * @return mixed
   */
  public function deleteDatabase($name) {
    if(empty($name) || !is_string($name)) {
      throw new SagException('deleteDatabase() expected a valid database name');
    }

    return $this->procPacket('DELETE', "/$name");
  }

  /**
   * Starts a replication job between two databases, independently of which
   * database you set with Sag.
   *
   * @param string $src The name of the database that you are replicating from.
   * @param string $target The name of the database that you are replicating
   * to.
   * @param bool $continuous Whether to make this a continuous replication job
   * or not. Defaults to false.
   * @param bool $createTarget Specifies create_target, which will create the
   * target database if it does not already exist. (optional)
   * @param string $filter The name of the filter function to use. (optional)
   * @param mixed $filterQueryParams An object or associative array of
   * parameters to be passed to the filter function via query_params. Only used
   * if $filter is set.
   *
   * @return mixed
   */
  public function replicate($src, $target, $continuous = false, $createTarget = null, $filter = null, $filterQueryParams = null) {
    if(empty($src) || !is_string($src)) {
      throw new SagException('replicate() is missing a source to replicate from.');
    }

    if(empty($target) || !is_string($target)) {
      throw new SagException('replicate() is missing a target to replicate to.');
    }

    if(!is_bool($continuous)) {
      throw new SagException('replicate() expected a boolean for its third argument.');
    }

    if(isset($createTarget) && !is_bool($createTarget)) {
      throw new SagException('createTarget needs to be a boolean.');
    }

    if(isset($filter)) {
      if(!is_string($filter)) {
        throw new SagException('filter must be the name of a design doc\'s filter function: ddoc/filter');
      }

      if(isset($filterQueryParams) && !is_object($filterQueryParams) && !is_array($filterQueryParams)) {
        throw new SagException('filterQueryParams needs to be an object or an array');
      }
    }

    $data = new stdClass();
    $data->source = $src;
    $data->target = $target;

    /*
     * These guys are optional, so only include them if non-default to save on
     * packet size.
     */
    if($continuous) {
      $data->continuous = true;
    }

    if($createTarget) {
      $data->create_target = true;
    }

    if($filter) {
      $data->filter = $filter;

      if($filterQueryParams) {
        $data->filterQueryParams = $filterQueryParams;
      }
    }

    return $this->procPacket('POST', '/_replicate', json_encode($data));
  }

  /**
   * Starts a compaction job on the database you selected, or optionally one of
   * its views.
   *
   * @param string $viewName The database's view that you want to compact,
   * instead of the whole database.
   *
   * @return mixed
   */
  public function compact($viewName = null) {
    return $this->procPacket('POST', "/{$this->db}/_compact".((empty($viewName)) ? '' : "/$viewName"));
  }

  /**
   * Create or update attachments on documents by passing in a serialized
   * version of your attachment (a string).
   *
   * @param string $name The attachment's name.
   * @param string $data The attachment's data, in string representation. Ie.,
   * you need to serialize your attachment.
   * @param string $contentType The proper Content-Type for your attachment.
   * @param string $docID The _id of the document that the attachment
   * belongs to.
   * @param string $rev optional The _rev of the document that the attachment
   * belongs to. Leave blank if you are creating a new document.
   *
   * @return mixed
   */
  public function setAttachment($name, $data, $contentType, $docID, $rev = null) {
    if(empty($docID)) {
      throw new SagException('You need to provide a document ID.');
    }

    if(empty($name)) {
      throw new SagException('You need to provide the attachment\'s name.');
    }

    if(empty($data)) {
      throw new SagException('You need to provide the attachment\'s data.');
    }

    if(!is_string($data)) {
      throw new SagException('You need to provide the attachment\'s data as a string.');
    }

    if(empty($contentType)) {
      throw new SagException('You need to provide the data\'s Content-Type.');
    }

    return $this->procPacket('PUT', "/{$this->db}/{$docID}/{$name}".(($rev) ? "?rev=".urlencode($rev) : ""), $data, array("Content-Type" => $contentType));
  }

  /**
   * Sets how long Sag should wait to establish a connection to CouchDB.
   *
   * @param int $seconds
   * @return Sag Returns $this.
   */
  public function setOpenTimeout($seconds) {
    //the adapter will take care of the validation for us
    $this->httpAdapter->setOpenTimeout($seconds);

    return $this;
  }

  /**
   * How long Sag should wait to execute a request with CouchDB. If not set,
   * then either default_socket_timeout from your php.ini or cURL's defaults
   * are used depending on which adapter you're using.
   *
   * Use setOpenTimeout() to set the timeout on opening the socket.
   *
   * @param int $seconds The seconds part of the timeout.
   * @param int $microseconds optional The microseconds part of the timeout.
   * @return Sag Returns $this.
   */
  public function setRWTimeout($seconds, $microseconds = 0) {
    $this->httpAdapter->setRWTimeout($seconds, $microseconds);

    return $this;
  }

  /*
   * Pass an implementation of the SagCache, such as SagFileCache, that will be
   * used when retrieving objects. It is taken and stored as a reference. 
   *
   * @param SagCache An implementation of SagCache (ex., SagFileCache).
   * @return Sag Returns $this.
   */
  public function setCache(&$cacheImpl) {
    if(!($cacheImpl instanceof SagCache)) {
      throw new SagException('That is not a valid cache.');
    }

    $this->cache = $cacheImpl;

    return $this;
  }

  /**
   * Returns the cache object that's currently being used. 
   *
   * @return SagCache
   */
  public function getCache() {
    return $this->cache;
  }

  /**
   * Returns the name of the database Sag is currently working with, or null if
   * setDatabase() hasn't been called yet.
   *
   * @return String
   */
  public function currentDatabase() {
    return $this->db;
  }

  /**
   * Retrieves the run time metrics from CouchDB that lives at /_stats.
   *
   * @return stdClass
   */
  public function getStats() {
    return $this->procPacket('GET', '/_stats');
  }

  /**
   * Set whether or not to include ?stale=ok by default when running GET and
   * HEAD requests.
   *
   * When set to true, a very slight overhead in the get() and head() functions
   * will occur, as they will parse out the parameters from the URL you
   * provide and ensure that no other value is being passed to the stale
   * variable.
   *
   * @param bool $stale True will make stale=ok be sent by default.
   * @return Sag Returns $this.
   */
  public function setStaleDefault($stale) {
    if(!is_bool($stale)) {
      throw new SagException('setStaleDefault() expected a boolean argument.');
    }

    $this->staleDefault = $stale;

    return $this;
  }

  /**
   * Sets a global cookie that will overwrite any other internal cookie values
   * that Sag tries to set. For example, if you set AuthSession and call
   * login(), then the AuthSession value you specify will overwrite the value
   * retrieved from the server, so don't set AuthSession while using login().
   *
   * Setting the value to null will make Sag no longer send the cookie.
   *
   * @param string $key The cookie's key.
   * @param string $value The cookie's value.
   * @return Sag Returns $this.
   *
   * @see getCookie()
   */
  public function setCookie($key, $value) {
    if(!$key || !is_string($key)) {
      throw new SagException('Unexpected cookie key.');
    }

    if($value && !is_string($value)) {
        throw new SagException('Unexpected cookie value.');
    }

    if($value) {
      $this->globalCookies[$key] = $value;
    }
    else {
      unset($this->globalCookies[$key]);
    }

    return $this;
  }

  /**
   * Returns the global cookie as set in setCookie().
   *
   * @return String The cookie's value or null if not set.
   *
   * @see setCookie()
   */
  public function getCookie($key) {
    return (!empty($this->globalCookies[$key])) ? $this->globalCookies[$key] : null;
  }

  /**
   * Set whether to use SSL or not.
   *
   * By default the host's certificate will not be verified: you must provide a
   * certifivate to setSSLCert() to enable verification.
   *
   * @param bool $use Set to true to use SSL, false to not.
   *
   * @return Sag Returns $this.
   *
   * @see setSSLCert()
   */
  public function useSSL($use) {
    if(!is_bool($use)) {
      throw new SagException('Excepted a boolean, but got something else.');
    }

    if($use !== $this->usingSSL()) {
      $this->httpAdapter->useSSL($use);
    }

    return $this;
  }

  /**
   * Returns whether Sag is using SSL or not.
   *
   * @return bool true means Sag is using SSL, false means Sag is not.
   *
   * @see useSSL()
   */
  public function usingSSL() {
    return $this->httpAdapter->usingSSL();
  }

  /**
   * Provide a path to a file that contains one or more certificates to verify
   * the CouchDB host with when using SSL. Only applies if you set
   * useSSL(true).
   *
   * @param string $path File path to the certificate file. Pass null to unset
   * the path.
   *
   * @return Sag Returns $this.
   *
   * @see useSSL()
   */
  public function setSSLCert($path) {
    if($path !== null) {
      if(!is_string($path) || !$path) {
        throw new SagException('Invalid file path provided.');
      }

      if(!is_file($path)) {
        throw new SagException('That path does not point to a file.');
      }

      if(!is_readable($path)) {
        throw new SagException('PHP does not have read privileges with that file.');
      }
    }

    $this->httpAdapter->setSSLCert($path);

    return $this;
  }

  public function setPathPrefix($path) {
    if(!is_string($path)) {
      throw new SagException('Invalid URL path prefix - must be a string.');
    }

    $this->pathPrefix = $path;

    return $this;
  }

  public function getPathPrefix() {
    return $this->pathPrefix;
  }

  // The main driver - does all the socket and protocol work.
  private function procPacket($method, $url, $data = null, $headers = array()) {
    /*
     * For now we only data data as strings. Streams and other formats will be
     * permitted later.
     */
    if($data && !is_string($data)) {
      throw new SagException('Unexpected data format. Please report this bug.');
    }

    if($this->pathPrefix && is_string($this->pathPrefix)) {
      $url = $this->pathPrefix . $url;
    }

    // Filter the use of the Expect header since we don't support Continue headers.
    $headers['Expect'] = isset($headers['Expect']) ? $headers['Expect'] : null;
    if(!$headers['Expect']){
        /*
       * PHP cURL will set the Expect header to 100-continue if we don't set it
       * ourselves. See https://github.com/sbisbee/sag/pull/51
       */
        $headers['Expect'] = (isset($headers['expect']) && $headers['expect']) ? $headers['expect'] : ' '; //1 char string, so it's == to true
    }

    if(strtolower($headers['Expect']) === '100-continue') {
      throw new SagException('Sag does not support HTTP/1.1\'s Continue.');
    }

    // Do some string replacing for HTTP sanity.
    $url = str_replace(array(" ", "\""), array('%20', '%22'), $url);

    // Build the request packet.
    $headers["Host"] = "{$this->host}:{$this->port}";
    $headers["User-Agent"] = "Sag/0.9.0";

    /*
     * This prevents some unRESTful requests, such as inline attachments in
     * CouchDB 1.1.0, from sending multipart responses that would break our
     * parser.
     */
    $headers['Accept'] = 'application/json';

    //usernames and passwords can be blank
    if($this->authType == Sag::$AUTH_BASIC && (isset($this->user) || isset($this->pass))) {
      $headers["Authorization"] = 'Basic '.base64_encode("{$this->user}:{$this->pass}");
    }
    elseif($this->authType == Sag::$AUTH_COOKIE && isset($this->authSession)) {
      $headers['Cookie'] = array( 'AuthSession' => $this->authSession );
      $headers['X-CouchDB-WWW-Authenticate'] = 'Cookie';
    }

    if(is_array($this->globalCookies) && sizeof($this->globalCookies)) {
      //might have been set before by auth handling
      if($headers['Cookie']) {
        $headers['Cookie'] = array_merge($headers['Cookie'], $this->globalCookies);
      }
      else {
        $headers['Cookie'] = $this->globalCookies;
      }
    }

    /*
     * Checking this again because $headers['Cookie'] could be set in two
     * different logic paths above.
     */
    if(!empty($headers['Cookie'])) {
      $buff = '';

      foreach($headers['Cookie'] as $k => $v) {
        $buff = (($buff) ? ' ' : '') . "$k=$v;";
      }

      $headers['Cookie'] = $buff;
      unset($buff);
    }

    /*
     * JSON is our default and most used Content-Type, but others need to be
     * specified to allow attachments.
     */
    if(!isset($headers['Content-Type'])) {
      $headers['Content-Type'] = 'application/json';
    }

    if($data) {
      $headers['Content-Length'] = strlen($data);
    }

    return $this->httpAdapter->procPacket($method, $url, $data, $headers);
  }

  /**
   * Takes a URL and k/v combo for a URL parameter, break the query string out
   * of the URL, and sets the parameter to the k/v pair you pass in. This will
   * overwrite a paramter's value if it already exists in the URL, or simply
   * create it if it doesn't already.
   *
   *
   * @param string $url The URL to run against.
   * @param string $key The name of the parameter to set in the URL.
   * @param string $value The value of the parameter to set in the URL.
   *
   * @return string The modified URL.
   */
  private function setURLParameter($url, $key, $value) {
    $url = parse_url($url);

    if(!empty($url['query'])) {
      parse_str($url['query'], $params);
    }
    $params[$key] = $value;

    return $url = $url['path'].'?'.http_build_query($params);
  }
}
