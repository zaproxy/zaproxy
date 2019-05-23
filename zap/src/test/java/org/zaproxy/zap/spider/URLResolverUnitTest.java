package org.zaproxy.zap.spider;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link org.zaproxy.zap.spider.URLResolver}.
 *
 * @author bjoern.kimminich@gmx.de
 */
public class URLResolverUnitTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnMissingBaseUrl() {
		URLResolver.resolveUrl(null, "notNull");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnMissingRelativeUrl() {
		URLResolver.resolveUrl("notNull", null);
	}

	@Test
	public void shouldAppendRelativeUrlToBaseUrlHost() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de", "/xy/z"), is("http://www.abc.de/xy/z"));
	}

	@Test
	public void shouldInsertSlashBetweenBaseUrlAndRelativeUrlIfMissing() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de", "xyz"), is("http://www.abc.de/xyz"));
	}

	@Test
	public void shouldReplaceLastPartOfUrlPathFromBaseUrlWithRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de/w/x", "y/z"), is("http://www.abc.de/w/y/z"));
	}

	@Test
	public void shouldRemoveFragmentFromBaseUrlBeforeAppendingRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de#anchor", "y"), is("http://www.abc.de/y"));
	}

	@Test
	public void shouldRemoveQueryFromBaseUrlBeforeAppendingRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de?y=z", "test"), is("http://www.abc.de/test"));
	}

	@Test
	public void shouldRemoveParametersFromBaseUrlBeforeAppendingRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de;y;z", "test"), is("http://www.abc.de/test"));
	}

	@Test
	public void shouldReturnOriginalBaseUrlForGivenEmptyRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://www.abc.de/x?y=z&u=v#123", ""), is("http://www.abc.de/x?y=z&u=v#123"));
	}

	@Test
	public void shouldReturnOriginalRelativeUrlForGivenAbsoluteUrlAsRelativeUrl() {
		assertThat(URLResolver.resolveUrl("http://base.url", "http://www.abc.de/x?y=z&u=v#123"), is("http://www.abc.de/x?y=z&u=v#123"));
	}

	@Test
	public void shouldUseSchemeOfBaseUrlForGivenUrlWithHostAsRelativeUrl() {
		assertThat(URLResolver.resolveUrl("https://base.url", "//www.test.com"), is("https://www.test.com"));
	}

	@Test
	public void shouldAppendQueryGivenAsRelativeUrlToBaseUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/123", "?x=y"), is("http://abc.de/123?x=y"));
	}

	@Test
	public void shouldAppendParametersGivenAsRelativeUrlToBaseUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/123", ";x=y"), is("http://abc.de/123;x=y"));
	}

	@Test
	public void shouldAppendFragmentGivenAsRelativeUrlToBaseUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/123", "#test"), is("http://abc.de/123#test"));
	}

	@Test
	public void shouldRemoveLeadingSlashPointsFromRelativeUrlBeforeAppendingToBaseUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/123/xyz", "../test"), is("http://abc.de/test"));
	}

	@Test
	public void shouldRemoveAllSlashPointSlashOccurencesFromResolvedUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/./", "test/./xyz/./123"), is("http://abc.de/test/xyz/123"));
	}

	@Test
	public void shouldRemoveTrailingPointFromResolvedUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de", "test/."), is("http://abc.de/test/"));
	}

	@Test
	public void shouldApplyDirectoryTraversalWithSlashPointsInResolvedUrl() {
		assertThat(URLResolver.resolveUrl("http://abc.de/x/../", "y/../z/../test/123/.."), is("http://abc.de/test/"));
	}

}
