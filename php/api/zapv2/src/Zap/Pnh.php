<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 the ZAP development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


namespace Zap;


/**
 * This file was automatically generated.
 */
class Pnh {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function monitor($id, $message, $apikey='') {
		return $this->zap->request($this->zap->base . 'pnh/action/monitor/', array('id' => $id, 'message' => $message, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function oracle($id, $apikey='') {
		return $this->zap->request($this->zap->base . 'pnh/action/oracle/', array('id' => $id, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function startMonitoring($url, $apikey='') {
		return $this->zap->request($this->zap->base . 'pnh/action/startMonitoring/', array('url' => $url, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function stopMonitoring($id, $apikey='') {
		return $this->zap->request($this->zap->base . 'pnh/action/stopMonitoring/', array('id' => $id, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function pnh($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'pnh/other/pnh/', array('apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function manifest($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'pnh/other/manifest/', array('apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function service($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'pnh/other/service/', array('apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function fx_pnhxpi($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'pnh/other/fx_pnh.xpi/', array('apikey' => $apikey));
	}

}
