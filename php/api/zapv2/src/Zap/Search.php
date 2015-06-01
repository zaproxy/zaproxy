<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 the ZAP development team
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
class Search {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function urlsByUrlRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/urlsByUrlRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function urlsByRequestRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/urlsByRequestRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function urlsByResponseRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/urlsByResponseRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function urlsByHeaderRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/urlsByHeaderRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function messagesByUrlRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/messagesByUrlRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function messagesByRequestRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/messagesByRequestRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function messagesByResponseRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/messagesByResponseRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function messagesByHeaderRegex($regex, $baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'search/view/messagesByHeaderRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	public function harByUrlRegex($regex, $baseurl='', $start='', $count='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'search/other/harByUrlRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count, 'apikey' => $apikey));
	}

	public function harByRequestRegex($regex, $baseurl='', $start='', $count='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'search/other/harByRequestRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count, 'apikey' => $apikey));
	}

	public function harByResponseRegex($regex, $baseurl='', $start='', $count='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'search/other/harByResponseRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count, 'apikey' => $apikey));
	}

	public function harByHeaderRegex($regex, $baseurl='', $start='', $count='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'search/other/harByHeaderRegex/', array('regex' => $regex, 'baseurl' => $baseurl, 'start' => $start, 'count' => $count, 'apikey' => $apikey));
	}

}
