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
class Context {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * List context names of current session
	 */
	public function contextList() {
		$res = $this->zap->request($this->zap->base . 'context/view/contextList/');
		return reset($res);
	}

	/**
	 * List excluded regexs for context
	 */
	public function excludeRegexs($contextname) {
		$res = $this->zap->request($this->zap->base . 'context/view/excludeRegexs/', array('contextName' => $contextname));
		return reset($res);
	}

	/**
	 * List included regexs for context
	 */
	public function includeRegexs($contextname) {
		$res = $this->zap->request($this->zap->base . 'context/view/includeRegexs/', array('contextName' => $contextname));
		return reset($res);
	}

	/**
	 * List the information about the named context
	 */
	public function context($contextname) {
		$res = $this->zap->request($this->zap->base . 'context/view/context/', array('contextName' => $contextname));
		return reset($res);
	}

	/**
	 * Add exclude regex to context
	 */
	public function excludeFromContext($contextname, $regex, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/excludeFromContext/', array('contextName' => $contextname, 'regex' => $regex, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Add include regex to context
	 */
	public function includeInContext($contextname, $regex, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/includeInContext/', array('contextName' => $contextname, 'regex' => $regex, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Creates a new context in the current session
	 */
	public function newContext($contextname='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/newContext/', array('contextName' => $contextname, 'apikey' => $apikey));
		return reset($res);
	}

	public function exportContext($contextname, $contextfile, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/exportContext/', array('contextName' => $contextname, 'contextFile' => $contextfile, 'apikey' => $apikey));
		return reset($res);
	}

	public function importContext($contextfile, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/importContext/', array('contextFile' => $contextfile, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Sets a context to in scope (contexts are in scope by default)
	 */
	public function setContextInScope($contextname, $booleaninscope, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'context/action/setContextInScope/', array('contextName' => $contextname, 'booleanInScope' => $booleaninscope, 'apikey' => $apikey));
		return reset($res);
	}

}
