/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
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
package org.parosproxy.paros.security;

/**
 * Signal not initialized root CA certificate store.
 */
public class MissingRootCertificateException extends IllegalStateException {

	private static final long serialVersionUID = -9087082417871920302L;

	public MissingRootCertificateException() {
		super();
	}

	public MissingRootCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingRootCertificateException(String s) {
		super(s);
	}

	public MissingRootCertificateException(Throwable cause) {
		super(cause);
	}

}
