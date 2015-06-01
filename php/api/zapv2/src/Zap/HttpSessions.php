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
class HttpSessions {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function sessions($site, $session='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/view/sessions/', array('site' => $site, 'session' => $session));
		return reset($res);
	}

	public function activeSession($site) {
		$res = $this->zap->request($this->zap->base . 'httpSessions/view/activeSession/', array('site' => $site));
		return reset($res);
	}

	public function sessionTokens($site) {
		$res = $this->zap->request($this->zap->base . 'httpSessions/view/sessionTokens/', array('site' => $site));
		return reset($res);
	}

	public function createEmptySession($site, $session='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/createEmptySession/', array('site' => $site, 'session' => $session, 'apikey' => $apikey));
		return reset($res);
	}

	public function removeSession($site, $session, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/removeSession/', array('site' => $site, 'session' => $session, 'apikey' => $apikey));
		return reset($res);
	}

	public function setActiveSession($site, $session, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/setActiveSession/', array('site' => $site, 'session' => $session, 'apikey' => $apikey));
		return reset($res);
	}

	public function unsetActiveSession($site, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/unsetActiveSession/', array('site' => $site, 'apikey' => $apikey));
		return reset($res);
	}

	public function addSessionToken($site, $sessiontoken, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/addSessionToken/', array('site' => $site, 'sessionToken' => $sessiontoken, 'apikey' => $apikey));
		return reset($res);
	}

	public function removeSessionToken($site, $sessiontoken, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/removeSessionToken/', array('site' => $site, 'sessionToken' => $sessiontoken, 'apikey' => $apikey));
		return reset($res);
	}

	public function setSessionTokenValue($site, $session, $sessiontoken, $tokenvalue, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/setSessionTokenValue/', array('site' => $site, 'session' => $session, 'sessionToken' => $sessiontoken, 'tokenValue' => $tokenvalue, 'apikey' => $apikey));
		return reset($res);
	}

	public function renameSession($site, $oldsessionname, $newsessionname, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'httpSessions/action/renameSession/', array('site' => $site, 'oldSessionName' => $oldsessionname, 'newSessionName' => $newsessionname, 'apikey' => $apikey));
		return reset($res);
	}

}
