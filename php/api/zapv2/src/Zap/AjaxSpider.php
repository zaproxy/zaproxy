<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP development team
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
class AjaxSpider {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function status() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/status/')->{'status'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function results($start='', $count='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/results/', array('start' => $start, 'count' => $count))->{'results'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function numberOfResults() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/numberOfResults/')->{'numberOfResults'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function scan($url, $inscope='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/scan/', array('url' => $url, 'inScope' => $inscope, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function stop($apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/stop/', array('apikey' => $apikey));
	}

}
