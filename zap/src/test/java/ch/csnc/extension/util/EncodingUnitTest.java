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
