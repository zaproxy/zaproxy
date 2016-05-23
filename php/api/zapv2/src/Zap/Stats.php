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
class Stats {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * Statistics
	 */
	public function stats($keyprefix=NULL) {
		$params = array();
		if ($keyprefix !== NULL) {
			$params['keyPrefix'] = $keyprefix;
		}
		return $this->zap->request($this->zap->base . 'stats/view/stats/', $params)->{'stats'};
	}

	/**
	 * Gets all of the site based statistics, optionally filtered by a key prefix
	 */
	public function allSitesStats($keyprefix=NULL) {
		$params = array();
		if ($keyprefix !== NULL) {
			$params['keyPrefix'] = $keyprefix;
		}
		return $this->zap->request($this->zap->base . 'stats/view/allSitesStats/', $params)->{'allSitesStats'};
	}

	/**
	 * Gets all of the global statistics, optionally filtered by a key prefix
	 */
	public function siteStats($site, $keyprefix=NULL) {
		$params = array('site' => $site);
		if ($keyprefix !== NULL) {
			$params['keyPrefix'] = $keyprefix;
		}
		return $this->zap->request($this->zap->base . 'stats/view/siteStats/', $params)->{'siteStats'};
	}

	/**
	 * Gets the Statsd service hostname
	 */
	public function optionStatsdHost() {
		return $this->zap->request($this->zap->base . 'stats/view/optionStatsdHost/')->{'StatsdHost'};
	}

	/**
	 * Gets the Statsd service port
	 */
	public function optionStatsdPort() {
		return $this->zap->request($this->zap->base . 'stats/view/optionStatsdPort/')->{'StatsdPort'};
	}

	/**
	 * Gets the prefix to be applied to all stats sent to the configured Statsd service
	 */
	public function optionStatsdPrefix() {
		return $this->zap->request($this->zap->base . 'stats/view/optionStatsdPrefix/')->{'StatsdPrefix'};
	}

	/**
	 * Returns 'true' if in memory statistics are enabled, otherwise returns 'false'
	 */
	public function optionInMemoryEnabled() {
		return $this->zap->request($this->zap->base . 'stats/view/optionInMemoryEnabled/')->{'InMemoryEnabled'};
	}

	/**
	 * Returns 'true' if a Statsd server has been correctly configured, otherwise returns 'false'
	 */
	public function optionStatsdEnabled() {
		return $this->zap->request($this->zap->base . 'stats/view/optionStatsdEnabled/')->{'StatsdEnabled'};
	}

	/**
	 * Clears all of the statistics
	 */
	public function clearStats($keyprefix=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($keyprefix !== NULL) {
			$params['keyPrefix'] = $keyprefix;
		}
		return $this->zap->request($this->zap->base . 'stats/action/clearStats/', $params);
	}

	/**
	 * Sets the Statsd service hostname, supply an empty string to stop using a Statsd service
	 */
	public function setOptionStatsdHost($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'stats/action/setOptionStatsdHost/', array('String' => $string, 'apikey' => $apikey));
	}

	/**
	 * Sets the prefix to be applied to all stats sent to the configured Statsd service
	 */
	public function setOptionStatsdPrefix($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'stats/action/setOptionStatsdPrefix/', array('String' => $string, 'apikey' => $apikey));
	}

	/**
	 * Sets whether in memory statistics are enabled
	 */
	public function setOptionInMemoryEnabled($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'stats/action/setOptionInMemoryEnabled/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	/**
	 * Sets the Statsd service port
	 */
	public function setOptionStatsdPort($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'stats/action/setOptionStatsdPort/', array('Integer' => $integer, 'apikey' => $apikey));
	}

}
