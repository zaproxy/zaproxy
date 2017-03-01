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
class Search {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function urlsByUrlRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/urlsByUrlRegex/', $params)->{'urlsByUrlRegex'};
	}

	public function urlsByRequestRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/urlsByRequestRegex/', $params)->{'urlsByRequestRegex'};
	}

	public function urlsByResponseRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/urlsByResponseRegex/', $params)->{'urlsByResponseRegex'};
	}

	public function urlsByHeaderRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/urlsByHeaderRegex/', $params)->{'urlsByHeaderRegex'};
	}

	public function messagesByUrlRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/messagesByUrlRegex/', $params)->{'messagesByUrlRegex'};
	}

	public function messagesByRequestRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/messagesByRequestRegex/', $params)->{'messagesByRequestRegex'};
	}

	public function messagesByResponseRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/messagesByResponseRegex/', $params)->{'messagesByResponseRegex'};
	}

	public function messagesByHeaderRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array('regex' => $regex);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'search/view/messagesByHeaderRegex/', $params)->{'messagesByHeaderRegex'};
	}

	public function harByUrlRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL, $apikey='') {
		$params = array('regex' => $regex, 'apikey' => $apikey);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->requestother($this->zap->base_other . 'search/other/harByUrlRegex/', $params);
	}

	public function harByRequestRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL, $apikey='') {
		$params = array('regex' => $regex, 'apikey' => $apikey);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->requestother($this->zap->base_other . 'search/other/harByRequestRegex/', $params);
	}

	public function harByResponseRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL, $apikey='') {
		$params = array('regex' => $regex, 'apikey' => $apikey);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->requestother($this->zap->base_other . 'search/other/harByResponseRegex/', $params);
	}

	public function harByHeaderRegex($regex, $baseurl=NULL, $start=NULL, $count=NULL, $apikey='') {
		$params = array('regex' => $regex, 'apikey' => $apikey);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->requestother($this->zap->base_other . 'search/other/harByHeaderRegex/', $params);
	}

}
