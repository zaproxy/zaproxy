package org.zaproxy.zap.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class XMLStringUtilUnitTest {

	@Test
	public void shouldNotEscapeXmlWithoutControlCharacters() {
		// given
		String xml = "ABCDEF";
		// when
		String result = XMLStringUtil.escapeControlChrs(xml);
		// then
		assertThat(result, is(xml));
	}

	@Test
	public void shouldNotRemoveAnythingFromXmlWithoutControlCharacters() {
		// given
		String xml = "ABCDEF";
		// when
		String result = XMLStringUtil.removeControlChrs(xml);
		// then
		assertThat(result, is(xml));
	}
	
	@Test
	public void shouldEscapeControlCharacters() {
		// given
		String xml = "A\u0000B\u0001C\u0010D\uFFFEE\uFFFFF";
		// when
		String result = XMLStringUtil.escapeControlChrs(xml);
		// then
		assertThat(result, is("A\\x0000B\\x0001C\\x0010D\\xfffeE\\xffffF"));
	}

	@Test
	public void shouldRemoveControlCharacters() {
		// given
		String xml = "A\u0000B\u0001C\u0010D\uFFFEE\uFFFFF";
		// when
		String result = XMLStringUtil.removeControlChrs(xml);
		// then
		assertThat(result, is("ABCDEF"));
	}
}
