<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 *  ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 *  Copyright the ZAP development team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Client implementation for using the ZAP pentesting proxy remotely.

namespace Zap;

use Zap\Acsrf;
use Zap\AjaxSpider;
use Zap\Ascan;
use Zap\Authentication;
use Zap\Authorization;
use Zap\Autoupdate;
use Zap\Brk;
use Zap\Context;
use Zap\Core;
use Zap\ForcedUser;
use Zap\HttpSessions;
use Zap\ImportLogFiles;
use Zap\Params;
use Zap\Pnh;
use Zap\Pscan;
use Zap\Script;
use Zap\Search;
use Zap\SessionManagement;
use Zap\Spider;
use Zap\Stats;
use Zap\Users;

class ZapError extends \Exception{
	public function __construct($message, $code = 0, \Exception $previous = null) {
		parent::__construct($message, $code, $previous);
	}

	public function __toString() {
		return __CLASS__ . ": [{$this->code}]: {$this->message}\n";
	}
}

/**
 * Client API implementation for integrating with ZAP v2.
 */
class Zapv2 {

	// base JSON api url
	public $base = 'http://zap/JSON/';
	// base OTHER api url
	public $base_other = 'http://zap/OTHER/';

	/**
	 * Creates an instance of the ZAP api client.
	 *
	 * Note that all of the other classes in this directory are generated
	 * new ones will need to be manually added to this file
	 *
	 * @param string $proxy e.g. 'tcp://127.0.0.1:8080'
	 */
	public function __construct($proxy = 'tcp://127.0.0.1:8080') {
		$this->proxy = $proxy;

		$this->acsrf = new Acsrf($this);
		$this->ajaxSpider = new AjaxSpider($this);
		$this->ascan = new Ascan($this);
		$this->authentication = new Authentication($this);
		$this->authorization = new Authorization($this);
		$this->autoupdate = new Autoupdate($this);
		$this->brk = new Brk($this);
		$this->context = new Context($this);
		$this->core = new Core($this);
		$this->forcedUser = new ForcedUser($this);
		$this->httpsessions = new HttpSessions($this);
		$this->importLogFiles = new ImportLogFiles($this);
		$this->params = new Params($this);
		$this->pnh = new Pnh($this);
		$this->pscan = new Pscan($this);
		$this->script = new Script($this);
		$this->search = new Search($this);
		$this->sessionManagement = new SessionManagement($this);
		$this->spider = new Spider($this);
		$this->stats = new Stats($this);
		$this->users = new Users($this);
	}

	/**
	 * Overwrite a field
	 *
	 * mainly used for unit test
	 *
	 * @param $name the name of overwritten field
	 * @param $obj  the value of overwritten field
	 */
	public function setFieldByName($name, $obj) {
		$this->{$name} = $obj;
	}

	/**
	 * Checks that we have an OK response, else raises an exception.
	 *
	 * checks the result json data after doing action request
	 *
	 * @param array $json_data the json data to look at.
	 */
	public function expectOk($json_data) {
		if (is_object($json_data) && property_exists($json_data, 'Result') && $json_data->{'Result'} == 'OK') {
			return $json_data;
		}
		//throw new ZapError($json_data->values());
		throw new ZapError(var_export($json_data, true));
	}

	/**
	 * Opens a url
	 *
	 * @param $url
	 */
	public function sendRequest($url) {
		$context = stream_context_create(array('http' => array('proxy' => $this->proxy)));
		return file_get_contents($url, false, $context);
	}

	/**
	 * Open a url
	 *
	 * @param string $url
	 */
	public function statusCode($url) {
		// get the current proxy value
		$sc_before = stream_context_get_default();
		if (isset($sc_before['http']['proxy']) && $sc_before['http']['proxy'] != '') {
			$proxy_before = $sc_before['http']['proxy'];
		} else {
			$proxy_before = null;
		}

		stream_context_set_default(array('http' => array('proxy' => $this->proxy)));
		$headers = get_headers($url);

		// put the proxy value back
		stream_context_set_default(array('http' => array('proxy' => $proxy_before)));

		return substr($headers[0], 9, 3);
	}

	/**
	 * Shortcut for a GET request.
	 *
	 * @param string $url the url to GET at.
	 * @param array $get the disctionary to turn into GET variables.
	 */
	public function request($url, $get=array()) {
		$response = $this->sendRequest($url . '?' . $this->urlencode($get));
		$response = trim($response, '()');
		return json_decode($response);
	}

	/**
	 * Shortcut for an API OTHER GET request.
	 *
	 * @param string $url the url to GET at.
	 * @param array $getParams the disctionary to turn into GET variables.
	 */
	public function requestOther($url, $getParams=array()) {
		return $this->sendRequest($url . '?' . $this->urlencode($getParams));
	}

	private function urlencode($getParams) {
		$param = "";
		foreach ($getParams as $key => $value) {
			if ($param != "") {
				$param .= "&";
			}
			$param .= $key . "=" . urlencode($value);
		}
		return $param;
	}
}
