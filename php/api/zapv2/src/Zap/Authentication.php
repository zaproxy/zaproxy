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
class Authentication {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function getSupportedAuthenticationMethods() {
		return $this->zap->request($this->zap->base . 'authentication/view/getSupportedAuthenticationMethods/')->{'getSupportedAuthenticationMethods'};
	}

	public function getAuthenticationMethodConfigParams($authmethodname) {
		return $this->zap->request($this->zap->base . 'authentication/view/getAuthenticationMethodConfigParams/', array('authMethodName' => $authmethodname))->{'getAuthenticationMethodConfigParams'};
	}

	public function getAuthenticationMethod($contextid) {
		return $this->zap->request($this->zap->base . 'authentication/view/getAuthenticationMethod/', array('contextId' => $contextid))->{'getAuthenticationMethod'};
	}

	public function getLoggedInIndicator($contextid) {
		return $this->zap->request($this->zap->base . 'authentication/view/getLoggedInIndicator/', array('contextId' => $contextid))->{'getLoggedInIndicator'};
	}

	public function getLoggedOutIndicator($contextid) {
		return $this->zap->request($this->zap->base . 'authentication/view/getLoggedOutIndicator/', array('contextId' => $contextid))->{'getLoggedOutIndicator'};
	}

	public function setAuthenticationMethod($contextid, $authmethodname, $authmethodconfigparams=NULL, $apikey='') {
		$params = array('contextId' => $contextid, 'authMethodName' => $authmethodname, 'apikey' => $apikey);
		if ($authmethodconfigparams !== NULL) {
			$params['authMethodConfigParams'] = $authmethodconfigparams;
		}
		return $this->zap->request($this->zap->base . 'authentication/action/setAuthenticationMethod/', $params);
	}

	public function setLoggedInIndicator($contextid, $loggedinindicatorregex, $apikey='') {
		return $this->zap->request($this->zap->base . 'authentication/action/setLoggedInIndicator/', array('contextId' => $contextid, 'loggedInIndicatorRegex' => $loggedinindicatorregex, 'apikey' => $apikey));
	}

	public function setLoggedOutIndicator($contextid, $loggedoutindicatorregex, $apikey='') {
		return $this->zap->request($this->zap->base . 'authentication/action/setLoggedOutIndicator/', array('contextId' => $contextid, 'loggedOutIndicatorRegex' => $loggedoutindicatorregex, 'apikey' => $apikey));
	}

}
