/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.csnc.extension.util;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ch.csnc.extension.util.Encoding}.
 *
 * @author bjoern.kimminich@gmx.de
 */
public class EncodingUnitTest {

	@Test
	public void shouldConvertDataIntoCorrectBase64String() {
		assertThat(Encoding.base64encode("Hello World".getBytes()), is(equalTo("SGVsbG8gV29ybGQ=")));
	}

	@Test
	public void shouldConvertBase64StringIntoCorrectData() {
		assertThat(Encoding.base64decode("SGVsbG8gV29ybGQ="), is(equalTo("Hello World".getBytes())));
	}

	@Test
	public void shouldConvertDataIntoCorrectHexString() {
		assertThat(Encoding.toHexString("Hello World".getBytes()), is(equalTo("48656c6c6f20576f726c64")));
	}

	@Test
	public void shouldConvertStringIntoCorrectMD5Hash() {
		assertThat(Encoding.hashMD5("Hello World"), is(equalTo("b10a8db164e0754105b7a99be72e3fe5")));
	}

	@Test
	public void shouldConvertStringIntoCorrectSHAHash() {
		assertThat(Encoding.hashSHA("Hello World"), is(equalTo("0a4d55a8d778e5022fab701977c5d840bbc486d0")));
	}

	@Test
	public void shouldConvertStringIntoCorrectRot13Cipher() {
		assertThat(Encoding.rot13("Hello World"), is(equalTo("Uryyb Jbeyq")));
	}

	@Test
	public void shouldEncodeStringIntoCorrectUrlString() {
		assertThat(Encoding.urlEncode("He//o Wor/d"), is(equalTo("He%2F%2Fo+Wor%2Fd")));
	}

	@Test
	public void shouldDecodeUrlStringIntoCorrectString() {
		assertThat(Encoding.urlDecode("He%2F%2Fo+Wor%2Fd"), is(equalTo("He//o Wor/d")));
	}

}
