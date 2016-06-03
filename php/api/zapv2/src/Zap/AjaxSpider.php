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
	public function results($start=NULL, $count=NULL) {
		$params = array();
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/results/', $params)->{'results'};
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
	public function optionBrowserId() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionBrowserId/')->{'BrowserId'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionEventWait() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionEventWait/')->{'EventWait'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionMaxCrawlDepth() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionMaxCrawlDepth/')->{'MaxCrawlDepth'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionMaxCrawlStates() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionMaxCrawlStates/')->{'MaxCrawlStates'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionMaxDuration() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionMaxDuration/')->{'MaxDuration'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionNumberOfBrowsers() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionNumberOfBrowsers/')->{'NumberOfBrowsers'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionReloadWait() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionReloadWait/')->{'ReloadWait'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionClickDefaultElems() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionClickDefaultElems/')->{'ClickDefaultElems'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionClickElemsOnce() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionClickElemsOnce/')->{'ClickElemsOnce'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function optionRandomInputs() {
		return $this->zap->request($this->zap->base . 'ajaxSpider/view/optionRandomInputs/')->{'RandomInputs'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function scan($url, $inscope=NULL, $apikey='') {
		$params = array('url' => $url, 'apikey' => $apikey);
		if ($inscope !== NULL) {
			$params['inScope'] = $inscope;
		}
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/scan/', $params);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function stop($apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/stop/', array('apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionBrowserId($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionBrowserId/', array('String' => $string, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionClickDefaultElems($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionClickDefaultElems/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionClickElemsOnce($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionClickElemsOnce/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionEventWait($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionEventWait/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionMaxCrawlDepth($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionMaxCrawlDepth/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionMaxCrawlStates($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionMaxCrawlStates/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionMaxDuration($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionMaxDuration/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionNumberOfBrowsers($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionNumberOfBrowsers/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionRandomInputs($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionRandomInputs/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function setOptionReloadWait($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ajaxSpider/action/setOptionReloadWait/', array('Integer' => $integer, 'apikey' => $apikey));
	}

}
