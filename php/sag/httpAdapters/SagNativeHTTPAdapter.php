<?php
/**
 * Uses native PHP sockets to communicate with CouchDB. This means zero new
 * dependencies for your application.
 *
 * This is also the original socket code that was used in Sag.
 *
 * @version 0.9.0
 * @package HTTP
 */
require_once('SagHTTPAdapter.php');

class SagNativeHTTPAdapter extends SagHTTPAdapter {
  private $connPool = array();          //Connection pool

  /**
   * Closes any sockets that are left open in the connection pool.
   */
  public function __destruct() {
    foreach($this->connPool as $sock) {
      @fclose($sock);
    }
  }

  /**
   * Native sockets does not support SSL.
   */
  public function useSSL($use) {
    throw new SagException('Sag::$HTTP_NATIVE_SOCKETS does not support SSL.');
  }

  /**
   * Native sockets does not support SSL.
   */
  public function setSSLCert($path) {
    throw new SagException('Sag::$HTTP_NATIVE_SOCKETS does not support SSL.');
  }

  public function procPacket($method, $url, $data = null, $reqHeaders = array(), $specialHost = null, $specialPort = null) {
    if(is_string($specialHost) || is_string($specialPort)) {
      $host = ($specialHost) ? $specialHost : $this->host;
      $port = ($specialPort) ? $specialPort : $this->port;

      $reqHeaders['Host'] = "$host:$port";
    }
    else {
      $host = $this->host;
      $port = $this->port;
    }

    //Start building the request packet.
    $buff = "$method $url HTTP/1.1\r\n";

    foreach($reqHeaders as $k => $v) {
      $buff .= "$k: $v\r\n";
    }

    $buff .= "\r\n$data"; //it's okay if $data isn't set

    if($data && $method !== "PUT") {
      $buff .= "\r\n\r\n";
    }

    // Open the socket only once we know everything is ready and valid.
    $sock = null;

    while(!$sock) {
      if(sizeof($this->connPool) > 0) {
        $maybeSock = array_shift($this->connPool);
        $meta = stream_get_meta_data($maybeSock);

        if(!$meta['timed_out'] && !$meta['eof']) {
          $sock = $maybeSock;
        }
        elseif(is_resource($maybeSock)) {
          fclose($maybeSock);
        }
      }
      else {
        try {
          //these calls should throw on error
          if($this->socketOpenTimeout) {
            $sock = fsockopen($host, $port, $sockErrNo, $sockErrStr, $this->socketOpenTimeout);
          }
          else {
            $sock = fsockopen($host, $port, $sockErrNo, $sockErrStr);
          }

          /*
           * Some PHP configurations don't throw when fsockopen() fails, so we
           * will try to do it for them. We are using an Exception instead of a
           * SagException so that the catch-block grabs it and formats the
           * message.
           */
          if(!$sock) {
            throw new Exception($sockErrStr, $sockErrNo);
          }
        }
        catch(Exception $e) {
          throw new SagException('Was unable to fsockopen() a new socket: ' . $e->getMessage());
        }
      }
    }

    if(!$sock) {
      throw new SagException("Error connecting to {$host}:{$port} - $sockErrStr ($sockErrNo).");
    }

    // Send the packet.
    fwrite($sock, $buff);

    // Set the timeout.
    if(isset($this->socketRWTimeoutSeconds)) {
      stream_set_timeout($sock, $this->socketRWTimeoutSeconds, $this->socketRWTimeoutMicroseconds);
    }

    // Prepare the data structure to store the response.
    $response = new stdClass();
    $response->headers = new stdClass();
    $response->headers->_HTTP = new stdClass();
    $response->body = '';

    $isHeader = true;

    $chunkParsingDone = false;
    $chunkSize = null;

    // Read in the response.
    while(
      !$chunkParsingDone &&
      !feof($sock) && 
      (
        $isHeader ||
        (
          !$isHeader &&
          $method != 'HEAD' &&
          (
            isset($response->headers->{'transfer-encoding'}) == 'chunked' ||
            !isset($response->headers->{'content-length'}) ||
            (
              isset($response->headers->{'content-length'}) &&
              strlen($response->body) < $response->headers->{'content-length'}
            )
          )
        )
      )
    ) {
      $sockInfo = stream_get_meta_data($sock);

      if($sockInfo['timed_out']) {
        throw new SagException('Connection timed out while reading.');
      }

      /*
       * We cannot be promised that all responses will have a newline - ie.,
       * attachments. So when we're not dealing with headers (which will always
       * have newlines) or chunked encoding (which is just special), we should
       * give fgets() a length to read or else it will keep listening for bytes
       * until the socket times out.
       *
       * And we can't use a ternary because fgets() wants an int or undefined.
       */
      if(!$isHeader && !empty($response->headers->{'transfer-encoding'})
        && $response->headers->{'transfer-encoding'} !== 'chunked') {
        //the +1 is because fgets() reads (length - 1) bytes
        $line = fgets($sock, $response->headers->{'content-length'} - strlen($response->body) + 1);
      }
      else {
        $line = fgets($sock);
      }

      if(!$line && !$sockInfo['feof'] && !$sockInfo['timed_out']) {
        throw new SagException('Unexpectedly failed to retrieve a line from the socket before the end of the file.');
      }

      if($isHeader) {
        //Parse headers

        //Clean the input
        $line = trim($line);

        if($isHeader && empty($line)) {
          /*
           * Don't parse empty lines before the initial header as being the
           * header/body delim line.
           */
          if($response->headers->_HTTP->raw) {
            $isHeader = false; //the delim blank line
          }
        }
        else {
          if(!isset($response->headers->_HTTP->raw)) {
            //the first header line is always the HTTP info
            $response->headers->_HTTP->raw = $line;

            if(preg_match('(^HTTP/(?P<version>\d+\.\d+)\s+(?P<status>\d+))S', $line, $match)) {
              $response->headers->_HTTP->version = $match['version'];
              $response->headers->_HTTP->status = $match['status'];
              $response->status = $match['status'];
            }
            else {
              throw new SagException('There was a problem while handling the HTTP protocol.'); //whoops!
            }
          }
          else {
            $line = explode(':', $line, 2);
            $line[0] = strtolower($line[0]);
            $response->headers->$line[0] = $line[1] = ltrim($line[1]);

            switch($line[0]) {
              case 'set-cookie':
                $response->cookies = $this->parseCookieString($line[1]);
                break;

              case 'location':
                //only follow Location header on 3xx status codes
                if($response->headers->_HTTP->status >= 300 && $response->headers->_HTTP->status < 400) {
                  $line[1] = parse_url($line[1]);

                  return $this->procPacket(
                    $method,
                    $line[1]['path'],
                    $data,
                    $reqHeaders,
                    $line[1]['host'],
                    $line[1]['port']
                  );
                }

                break;
            }
          }
        }
      }
      else if(!empty($response->headers->{'transfer-encoding'})) {
        /*
         * Parse the response's body, which is being sent in chunks. Welcome to
         * HTTP/1.1 land.
         *
         * Each chunk is preceded with a size, so if we don't have a chunk size
         * then we should be looking for one. A zero chunk size means the
         * message is over.
         */
        if($chunkSize === null) {
          //Look for a chunk size
          $line = rtrim($line);

          if(!empty($line) || $line == "0") {
            $chunkSize = hexdec($line);

            if(!is_int($chunkSize)) {
              throw new SagException('Invalid chunk size: '.$line);
            }
          }
        }
        else if($chunkSize === 0) {
          // We are done processing all the chunks.
          $chunkParsingDone = true;
        }
        else if($chunkSize) {
          //We have a chunk size, so look for data
          if(strlen($line) > $chunkSize && strlen($line) - 2 > $chunkSize) {
            throw new SagException('Unexpectedly large chunk on this line.');
          }
          else {
            $response->body .= $line;

            preg_match_all("/\r\n/", $line, $numCRLFs);
            $numCRLFs = sizeof($numCRLFs);

            /*
             * Chunks can span >1 line, which PHP is going to give us one a a
             * time.
             */
            $chunkSize -= strlen($line);

            if($chunkSize <= 0) {
              /*
               * Nothing left to this chunk, so the next link is going to be
               * another chunk size. Or so we hope.
               */
              $chunkSize = null;
            }
          }
        }
        else {
          throw new SagException('Unexpected empty line.');
        }
      }
      else {
        /*
         * Parse the response's body, which is being sent in one piece like in
         * the good ol' days.
         */
        $response->body .= $line;
      }
    }

    // HTTP/1.1 assumes persisted connections, but proxies might close them.
    if(!empty($response->headers->connection)
      && strtolower($response->headers->connection) != 'close') {
      $this->connPool[] = $sock;
    }

    return self::makeResult($response, $method);
  }
}
?>
