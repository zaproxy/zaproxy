package org.zaproxy.zap.utils;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteBuilderUnitTest {

	private ByteBuilder byteBuilder;

	private static final byte[] TEST_ARRAY = {(byte)1, (byte)2, (byte)3 };

	@Before
	public void setUp() throws Exception {
		byteBuilder = new ByteBuilder();
	}

	@Test
	public void shouldHaveADefaultCapacityOf10() {
		// given
		byteBuilder = new ByteBuilder();
		// when
		int defaultCapacity = byteBuilder.capacity();
		// then
		assertThat(defaultCapacity, is(10));
	}

	@Test
	public void shouldBeInitializedWithGivenCapacity() {
		// given
		byteBuilder = new ByteBuilder(42);
		// when
		int capacity = byteBuilder.capacity();
		// then
		assertThat(capacity, is(42));
	}

	@Test
	public void shouldBeInitializedWithDoubleCapacityOfGivenArray() {
		// given
		byteBuilder = new ByteBuilder(TEST_ARRAY);
		// when
		int capacity = byteBuilder.capacity();
		// then
		assertThat(capacity, is(2*TEST_ARRAY.length));
	}

}
