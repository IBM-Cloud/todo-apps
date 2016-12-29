<?php
/**
 * Provides a common interface for Sag to connect to CouchDB over HTTP,
 * allowing for different drivers to be used thereby controling your project's
 * dependencies.
 *
 * @version 0.9.0
 * @package HTTP
 */
abstract class SagHTTPAdapter {
  public $decodeResp = true;

  protected $host;
  protected $port;

  protected $proto = 'http'; //http or https
  protected $sslCertPath;

  protected $socketOpenTimeout;                 //The seconds until socket connection timeout
  protected $socketRWTimeoutSeconds;            //The seconds for socket I/O timeout
  protected $socketRWTimeoutMicroseconds;       //The microseconds for socket I/O timeout

  public function __construct($host = "127.0.0.1", $port = "5984") {
    $this->host = $host;
    $this->port = $port;
  }

  /**
   * Used by the concrete HTTP adapters, this abstracts out the generic task of
   * turning strings from the net into response objects.
   *
   * @param string $response The body of the HTTP packet.
   * @param string $method The request's HTTP method ("HEAD", etc.).
   * @returns stdClass The response object.
   */
  protected function makeResult($response, $method) {
    //Make sure we got the complete response.
    if(
      $method != 'HEAD' &&
      isset($response->headers->{'content-length'}) &&
      strlen($response->body) != $response->headers->{'content-length'}
    ) {
      throw new SagException('Unexpected end of packet.');
    }

    /*
     * HEAD requests can return an HTTP response code >=400, meaning that there
     * was a CouchDB error, but we don't get a $response->body->error because
     * HEAD responses don't have bodies.
     *
     * We do this before the json_decode() because even running json_decode()
     * on undefined can take longer than calling it on a JSON string. So no
     * need to run any of the $json code.
     */
    if($method == 'HEAD') {
      if($response->status >= 400) {
        throw new SagCouchException('HTTP/CouchDB error without message body', $response->headers->_HTTP->status);
      }

      return $response;
    }

    // Decode whether they ask for a raw response or not for error messages.
    if(
      !empty($response->headers->{'content-type'}) &&
      $response->headers->{'content-type'} == 'application/json'
    ) {
      $json = json_decode($response->body);

      if(isset($json)) {
        if(!empty($json->error)) {
          throw new SagCouchException("{$json->error} ({$json->reason})", $response->headers->_HTTP->status);
        }

        if($this->decodeResp) {
          $response->body = $json;
        }
      }
    }

    return $response;
  }

  /**
   * A utility function for the concrete adapters to turn the HTTP Cookie
   * header's value into an object (map).
   *
   * @param string $cookieStr The HTTP Cookie header value (not including the
   * "Cookie: " key.
   * @returns stdClass An object mapping cookie name to cookie value.
   */
  protected function parseCookieString($cookieStr) {
    $cookies = new stdClass();

    foreach(explode('; ', $cookieStr) as $cookie) {
      $crumbs = explode('=', $cookie);
      if(!isset($crumbs[1])) {
        $crumbs[1] = '';
      }
      $cookies->{trim($crumbs[0])} = trim($crumbs[1]);
    }

    return $cookies;
  }

  /**
   * Processes the packet, returning the server's response.
   *
   * @param string $method The HTTP method for the request (ex., "HEAD").
   * @param string $url The URL to hit, not including the host info (ex.,
   * "/_all_docs").
   * @param string $data A serialized version of any data that needs to be sent
   * in the packet's body.
   * @param array $reqHeaders An associative array of headers where the keys
   * are the header names.
   * @param mixed $specialHost Uses the provided host for this packet only -
   * does not change the adapter's global host setting.
   * @param mixed $specialPort Uses the provided port for this packet only -
   * does not change the adapter's global port setting.
   * @returns stdClass The response object created by makeResponse().
   * @see makeResponse()
   */
  abstract public function procPacket($method, $url, $data = null, $reqHeaders = array(), $specialHost = null, $specialPort = null);

  /**
   * Whether to use HTTPS or not.
   *
   * @param bool $use Whether to use HTTPS or not.
   */
  public function useSSL($use) {
    $this->proto = 'http' . (($use) ? 's' : '');
  }

  /**
   * Sets the location of the CA file.
   *
   * @param mixed $path The absolute path to the CA file, or null to unset.
   */
  public function setSSLCert($path) {
    $this->sslCertPath = $path;
  }

  /**
   * Returns whether Sag is using SSL.
   *
   * @returns bool Returns true if the adapter is using SSL, else false.
   */
  public function usingSSL() {
    return $this->proto === 'https';
  }

  /**
   * Sets how long Sag should wait to establish a connection to CouchDB.
   *
   * @param int $seconds The number of seconds.
   */
  public function setOpenTimeout($seconds) {
    if(!is_int($seconds) || $seconds < 1) {
      throw new SagException('setOpenTimeout() expects a positive integer.');
    }

    $this->socketOpenTimeout = $seconds;
  }

  /**
   * Set how long we should wait for an HTTP request to be executed.
   *
   * @param int $seconds The number of seconds.
   * @param int $microseconds The number of microseconds.
   */
  public function setRWTimeout($seconds, $microseconds) {
    if(!is_int($microseconds) || $microseconds < 0) {
      throw new SagException('setRWTimeout() expects $microseconds to be an integer >= 0.');
    }

    //TODO make this better, including checking $microseconds
    //$seconds can be 0 if $microseconds > 0
    if(
      !is_int($seconds) ||
      (
        (!$microseconds && $seconds < 1) ||
        ($microseconds && $seconds < 0)
      )
    ) {
      throw new SagException('setRWTimeout() expects $seconds to be a positive integer.');
    }

    $this->socketRWTimeoutSeconds = $seconds;
    $this->socketRWTimeoutMicroseconds = $microseconds;
  }

  /**
   * Returns an associative array of the currently set timeout values.
   *
   * @return array An associative array with the keys 'open', 'rwSeconds', and
   * 'rwMicroseconds'.
   *
   * @see setTimeoutsFromArray()
   */
  public function getTimeouts() {
    return array(
      'open' => $this->socketOpenTimeout,
      'rwSeconds' => $this->socketRWTimeoutSeconds,
      'rwMicroseconds' => $this->socketRWTimeoutMicroseconds
    );
  }

  /**
   * A utility function that sets the different timeout values based on an
   * associative array.
   *
   * @param array $arr An associative array with the keys 'open', 'rwSeconds',
   * and 'rwMicroseconds'.
   *
   * @see getTimeouts()
   */
  public function setTimeoutsFromArray($arr) {
    /*
     * Validation is lax in here because this should only ever be used with
     * getTimeouts() return values. If people are using it by hand then there
     * might be something wrong with the API.
     */
    if(!is_array($arr)) {
      throw SagException('Expected an array and got something else.');
    }

    if(is_int($arr['open'])) {
      $this->setOpenTimeout($arr['open']);
    }

    if(is_int($arr['rwSeconds'])) {
      if(is_int($arr['rwMicroseconds'])) {
        $this->setRWTimeout($arr['rwSeconds'], $arr['rwMicroseconds']);
      }
      else {
        $this->setRWTimeout($arr['rwSeconds']);
      }
    }
  }
}
?>
