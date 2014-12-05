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
class Context {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * List context names of current session
	 */
	public function contextList() {
		return $this->zap->request($this->zap->base . 'context/view/contextList/')->{'contextList'};
	}

	/**
	 * List excluded regexs for context
	 */
	public function excludeRegexs($contextname) {
		return $this->zap->request($this->zap->base . 'context/view/excludeRegexs/', array('contextName' => $contextname))->{'excludeRegexs'};
	}

	/**
	 * List included regexs for context
	 */
	public function includeRegexs($contextname) {
		return $this->zap->request($this->zap->base . 'context/view/includeRegexs/', array('contextName' => $contextname))->{'includeRegexs'};
	}

	/**
	 * List the information about the named context
	 */
	public function context($contextname) {
		return $this->zap->request($this->zap->base . 'context/view/context/', array('contextName' => $contextname))->{'context'};
	}

	/**
	 * Add exclude regex to context
	 */
	public function excludeFromContext($contextname, $regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/excludeFromContext/', array('contextName' => $contextname, 'regex' => $regex, 'apikey' => $apikey));
	}

	/**
	 * Add include regex to context
	 */
	public function includeInContext($contextname, $regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/includeInContext/', array('contextName' => $contextname, 'regex' => $regex, 'apikey' => $apikey));
	}

	/**
	 * Creates a new context in the current session
	 */
	public function newContext($contextname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/newContext/', array('contextName' => $contextname, 'apikey' => $apikey));
	}

	public function exportContext($contextname, $contextfile, $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/exportContext/', array('contextName' => $contextname, 'contextFile' => $contextfile, 'apikey' => $apikey));
	}

	public function importContext($contextfile, $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/importContext/', array('contextFile' => $contextfile, 'apikey' => $apikey));
	}

	/**
	 * Sets a context to in scope (contexts are in scope by default)
	 */
	public function setContextInScope($contextname, $booleaninscope, $apikey='') {
		return $this->zap->request($this->zap->base . 'context/action/setContextInScope/', array('contextName' => $contextname, 'booleanInScope' => $booleaninscope, 'apikey' => $apikey));
	}

}
