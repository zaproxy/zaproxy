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
class Params {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * Shows the parameters for the specified site, or for all sites if the site is not specified
	 */
	public function params($site=NULL) {
		$params = array();
		if ($site !== NULL) {
			$params['site'] = $site;
		}
		return $this->zap->request($this->zap->base . 'params/view/params/', $params)->{'params'};
	}

}
