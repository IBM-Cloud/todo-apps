<?php
/**
 * Uses the PHP cURL bindings for HTTP communication with CouchDB. This gives
 * you more advanced features, like SSL supports, with the cost of an
 * additional dependency that your shared hosting environment might now have. 
 *
 * @version 0.9.0
 * @package HTTP
 */
require_once('SagHTTPAdapter.php');

class SagCURLHTTPAdapter extends SagHTTPAdapter {
  private $ch;

  private $followLocation; //whether cURL is allowed to follow redirects

  public function __construct($host, $port) {
    if(!extension_loaded('curl')) {
      throw new SagException('Sag cannot use cURL on this system: the PHP cURL extension is not installed.');
    }

    parent::__construct($host, $port);

    /*
     * PHP doesn't like it if you tell cURL to follow location headers when
     * open_basedir is set in PHP's configuration. Only check to see if it's
     * set once so we don't ini_get() on every request.
     */
    $this->followLocation = !ini_get('open_basedir');

    $this->ch = curl_init();
  }

  public function procPacket($method, $url, $data = null, $reqHeaders = array(), $specialHost = null, $specialPort = null) {
    // the base cURL options
    $opts = array(
      CURLOPT_URL => "{$this->proto}://{$this->host}:{$this->port}{$url}",
      CURLOPT_PORT => $this->port,
      CURLOPT_FOLLOWLOCATION => $this->followLocation,
      CURLOPT_HEADER => true,
      CURLOPT_RETURNTRANSFER => true,
      CURLOPT_NOBODY => false,
      CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
      CURLOPT_CUSTOMREQUEST => $method
    );

    // cURL wants the headers as an array of strings, not an assoc array
    if(is_array($reqHeaders) && sizeof($reqHeaders) > 0) {
      $opts[CURLOPT_HTTPHEADER] = array();

      foreach($reqHeaders as $k => $v) {
        $opts[CURLOPT_HTTPHEADER][] = "$k: $v";
      }
    }

    // send data through cURL's poorly named opt
    if($data) {
      $opts[CURLOPT_POSTFIELDS] = $data;
    }

    // special considerations for HEAD requests
    if($method == 'HEAD') {
      $opts[CURLOPT_NOBODY] = true;
    }

    // connect timeout
    if(is_int($this->socketOpenTimeout)) {
      $opts[CURLOPT_CONNECTTIMEOUT] = $this->socketOpenTimeout;
    }

    // exec timeout (seconds)
    if(is_int($this->socketRWTimeoutSeconds)) {
      $opts[CURLOPT_TIMEOUT] = $this->socketRWTimeoutSeconds;
    }

    // exec timeout (ms)
    if(is_int($this->socketRWTimeoutMicroseconds)) {
      $opts[CURLOPT_TIMEOUT_MS] = $this->socketRWTimeoutMicroseconds;
    }

    // SSL support: don't verify unless we have a cert set
    if($this->proto === 'https') {
      if(!$this->sslCertPath) {
        $opts[CURLOPT_SSL_VERIFYPEER] = false;
      }
      else {
        $opts[CURLOPT_SSL_VERIFYPEER] = true;
        $opts[CURLOPT_SSL_VERIFYHOST] = true;
        $opts[CURLOPT_CAINFO] = $this->sslCertPath;
      }
    }

    curl_setopt_array($this->ch, $opts);

    $chResponse = curl_exec($this->ch);

    if($chResponse !== false) {
      // prepare the response object
      $response = new stdClass();
      $response->headers = new stdClass();
      $response->headers->_HTTP = new stdClass();
      $response->body = '';

      // split headers and body
      list($respHeaders, $response->body) = explode("\r\n\r\n", $chResponse, 2);

      // split up the headers
      $respHeaders = explode("\r\n", $respHeaders);

      for($i = 0; $i < sizeof($respHeaders); $i++) {
        // first element will always be the HTTP status line
        if($i === 0) {
          $response->headers->_HTTP->raw = $respHeaders[$i];

          preg_match('(^HTTP/(?P<version>\d+\.\d+)\s+(?P<status>\d+))S', $respHeaders[$i], $match);

          $response->headers->_HTTP->version = $match['version'];
          $response->headers->_HTTP->status = $match['status'];
          $response->status = $match['status'];
        }
        else {
          $line = explode(':', $respHeaders[$i], 2);
          $line[0] = strtolower($line[0]);
          $response->headers->$line[0] = ltrim($line[1]);

          if($line[0] == 'set-cookie') {
            $response->cookies = $this->parseCookieString($line[1]);
          }
        }
      }
    }
    else if(curl_errno($this->ch)) {
      throw new SagException('cURL error #' . curl_errno($this->ch) . ': ' . curl_error($this->ch));
    }
    else {
      throw new SagException('cURL returned false without providing an error.');
    }

    // in the event cURL can't follow and we got a Location header w/ a 3xx
    if(!$this->followLocation &&
        isset($response->headers->location) &&
        $response->status >= 300 &&
        $response->status < 400
    ) {
      $parts = parse_url($response->headers->location);

      if(empty($parts['path'])) {
        $parts['path'] = '/';
      }

      $adapter = $this->makeFollowAdapter($parts);

      // we want the old headers (ex., Auth), but might need a new Host
      if(isset($parts['host'])) {
        $reqHeaders['Host'] = $parts['host'];

        if(isset($parts['port'])) {
          $reqHeaders['Host'] .= ':' . $parts['port'];
        }
      }

      return $adapter->procPacket($method, $parts['path'], $data, $reqHeaders);
    }

    return self::makeResult($response, $method);
  }

  /**
   * Used when we need to create a new adapter to follow a redirect because
   * cURL can't.
   *
   * @param array $parts Return value from url_parts() for the location header.
   * @return SagCURLHTTPAdapter Returns $this if talking to the same server
   * with the same protocol, otherwise creates a new instance.
   */
  private function makeFollowAdapter($parts) {
    // re-use $this if we just got a path or the host/proto info matches
    if(empty($parts['host']) ||
        ($parts['host'] == $this->host &&
          $parts['port'] == $this->port &&
          $parts['scheme'] == $this->proto
        )
    ) {
      return $this;
    }

    if(empty($parts['port'])) {
      $parts['port'] = ($parts['scheme'] == 'https') ? 443 : 5984;
    }

    $adapter = new SagCURLHTTPAdapter($parts['host'], $parts['port']);
    $adapter->useSSL($parts['scheme'] == 'https');
    $adapter->setTimeoutsFromArray($this->getTimeouts());

    return $adapter;
  }
}
?>
