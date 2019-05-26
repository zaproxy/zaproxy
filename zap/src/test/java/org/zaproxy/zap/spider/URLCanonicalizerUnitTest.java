package org.zaproxy.zap.spider;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zaproxy.zap.spider.SpiderParam.HandleParametersOption;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * This test ensure that nothing was broken in the handling of normal URLs during
 * the implementation of OData support as well as ensure that the OData support is correct.
 * 
 * It checks the canonicalization mechanism used to verify if a URL has already 
 * been visited before during the spider phase.
 */
public class URLCanonicalizerUnitTest {

    @BeforeClass
    public static void suppressLogging() {
        Logger.getLogger(URLCanonicalizer.class).addAppender(new NullAppender());
    }

    @Test
    public void shouldRemoveDefaultPortOfHttpUriWhenCanonicalizing() {
        // Given
        String uri = "http://example.com:80/";
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/")));
    }

    @Test
    public void shouldNotRemoveNonDefaultPortOfHttpUriWhenCanonicalizing() {
        // Given
        String uri = "http://example.com:443/";
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com:443/")));
    }

    @Test
    public void shouldRemoveDefaultPortOfHttpsUriWhenCanonicalizing() {
        // Given
        String uri = "https://example.com:443/";
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("https://example.com/")));
    }

    @Test
    public void shouldNotRemoveNonDefaultPortOfHttpsUriWhenCanonicalizing() {
        // Given
        String uri = "https://example.com:80/";
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("https://example.com:80/")));
    }

    @Test
    public void shouldAddEmptyPathIfUriHasNoPathWhenCanonicalizing() {
        // Given
        String uri = "http://example.com";
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/")));
    }

    @Test
    public void shouldCanonicalizeURIsWithAuthority() {
        // Given
        String[] uris = { "http://example.com/", "https://example.com/", "ftp://example.com/" };
        for (String uri : uris) {
            // When
            String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(uri)));
        }
    }

    @Test
    public void shouldUseBaseURIToResolveRelativeURIsWhenCanonicalizing() {
        // Given
        String baseURI = "http://example.com/path/";
        String[] relativeURIs = { "relative", "a/b/c", "../", "/absolute/path", "" };
        String[] expectedCanonicalURIs = {
                "http://example.com/path/relative",
                "http://example.com/path/a/b/c",
                "http://example.com/",
                "http://example.com/absolute/path",
                "http://example.com/path/", };
        for (int i = 0; i < relativeURIs.length; i++) {
            // When
            String canonicalizedUri = URLCanonicalizer.getCanonicalURL(relativeURIs[i], baseURI);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(expectedCanonicalURIs[i])));
        }
    }

    @Test
    public void shouldNormaliseEmptyAndDotPathSegmentsWhenCanonicalizing() {
        // Given
        String[] uris = {
                "http://example.com/../../x",
                "http://example.com/a//b/c//",
                "http://example.com/a/./b/./c",
                "http://example.com/.." };
        String[] expectedCanonicalURIs = {
                "http://example.com/x",
                "http://example.com/a/b/c/",
                "http://example.com/a/b/c",
                "http://example.com/.." };
        for (int i = 0; i < uris.length; i++) {
            // When
            String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uris[i]);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(expectedCanonicalURIs[i])));
        }
    }

    @Test
    public void shouldIgnoreURIsWithNoAuthority() {
        // Given
        String[] uris = { "javascript:ignore()", "mailto:ignore@example.com" };
        for (String uri : uris) {
            // When
            String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
            // Then
            assertThat(canonicalizedUri, canonicalizedUri, is(equalTo(null)));
        }
    }

    @Test
    public void shouldReturnCanonicalUriWithPercentEncodedPath() throws URIException {
        // Given
        String uri = new URI("http://example.com/path/%C3%A1/", true).toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/path/%C3%A1/")));
    }

    @Test
    public void shouldReturnCanonicalUriWithPercentEncodedQuery() throws URIException {
        // Given
        String uri = new URI("http://example.com/path/?par%C3%A2m=v%C3%A3lue", true).toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/path/?par%C3%A2m=v%C3%A3lue")));
    }

    @Test
    public void shouldCorrectlyParseQueryParameterNamesAndValuesWithAmpersandsAndEqualsWhenCanonicalizing() throws URIException {
        // Given
        String uri = new URI("http://example.com/?par%26am%3D1=val%26u%3De1", true).toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/?par%26am%3D1=val%26u%3De1")));
    }

    @Test
    public void shouldPreserveQueryParametersWithSameNameWhenCanonicalizing() throws URIException {
        // Given
        String uri = new URI("http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name2=value3", true).toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name2=value3")));
    }

    @Test
    public void shouldSortQueryParametersByNameAndValueWhenCanonicalizing() throws URIException {
        // Given
        String uri = new URI("http://example.com/?&name2=value2&name3=value3&name1=value1.2&name1=value1.1", true).toString();
        // When
        String canonicalizedUri = URLCanonicalizer.getCanonicalURL(uri);
        // Then
        assertThat(canonicalizedUri, is(equalTo("http://example.com/?name1=value1.1&name1=value1.2&name2=value2&name3=value3")));
    }

    @Test
    public void shouldReturnPercentEncodedUriWhenCleaningParametersIn_USE_ALL_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.USE_ALL,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue")));
    }

    @Test
    public void shouldReturnPercentEncodedUriWhenCleaningParametersIn_IGNORE_VALUE_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue1", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.IGNORE_VALUE,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/%C3%A1/?par%C3%A2m")));
    }

    @Test
    public void shouldReturnPercentEncodedUriWhenCleaningParametersIn_IGNORE_COMPLETELY_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/%C3%A1/?par%C3%A2m=v%C3%A3lue", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.IGNORE_COMPLETELY,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/%C3%A1/")));
    }

    @Test
    public void shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_USE_ALL_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.USE_ALL,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2")));
    }

    @Test
    public void shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_IGNORE_VALUE_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.IGNORE_VALUE,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?par%26am2&par%3Dam1")));
    }

    @Test
    public void shouldCorrectlyParseQueryParamNamesAndValuesWithAmpersandsAndEqualsWhenCleaningParametersIn_IGNORE_COMPLETELY_mode()
            throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?par%3Dam1=val%26ue1&par%26am2=val%3Due2", true);
        // When
        String cleanedUri = URLCanonicalizer.buildCleanedParametersURIRepresentation(
                uri,
                HandleParametersOption.IGNORE_COMPLETELY,
                false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/")));
    }

    @Test
    public void shouldPreserveQueryParametersWithSameNameWhenCleaningParametersIn_USE_ALL_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?param%5B%5D=value1.1&param%5B%5D=value1.2&param2=value2", true);
        // When
        String cleanedUri = URLCanonicalizer
                .buildCleanedParametersURIRepresentation(uri, HandleParametersOption.USE_ALL, false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?param%5B%5D=value1.1&param%5B%5D=value1.2&param2=value2")));
    }

    @Test
    public void shouldKeepJustOneQueryParameterWithSameNameWhenCleaningParametersIn_IGNORE_VALUE_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?param1=value1.1&param1=value1.2&param2=value2", true);
        // When
        String cleanedUri = URLCanonicalizer
                .buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_VALUE, false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/?param1&param2")));
    }

    @Test
    public void shouldRemoveAllQueryParametersWhenCleaningParametersIn_IGNORE_COMPLETELY_mode() throws URIException {
        // Given
        URI uri = new URI("http://example.com/path/?param1=value1.1&param1=value1.2&param2=value2", true);
        // When
        String cleanedUri = URLCanonicalizer
                .buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_COMPLETELY, false);
        // Then
        assertThat(cleanedUri, is(equalTo("http://example.com/path/")));
    }

	// Test of the legacy behavior

	@Test
	public void shouldCanonicalizeNormalURLWithoutParametersIn_USE_ALL_mode() throws URIException {
		URI uri = new URI("http", null,"host", 9001, "/myservlet");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.USE_ALL, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet"));
	}


	@Test
	public void shouldCanonicalizeNormalURLWithoutParametersIn_IGNORE_COMPLETELY_mode() throws URIException {
		URI uri = new URI("http", null,"host", 9001, "/myservlet");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_COMPLETELY, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet"));
	}

	@Test
	public void shouldCanonicalizeNormalURLWithoutParametersIn_IGNORE_VALUE_mode() throws URIException {
		URI uri = new URI("http", null,"host", 9001, "/myservlet");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_VALUE, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet"));
	}

	@Test
	public void shouldCanonicalizeNormalURLWithParametersIn_USE_ALL_mode() throws URIException {

		URI uri = new URI("http", null,"host", 9001, "/myservlet","p1=2&p2=myparam");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.USE_ALL, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet?p1=2&p2=myparam"));
	}

	@Test
	public void shouldCanonicalizeNormalURLWithParametersIn_IGNORE_COMPLETELY_mode() throws URIException {
		URI uri = new URI("http", null,"host", 9001, "/myservlet","p1=2&p2=myparam");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_COMPLETELY, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet"));
	}

	@Test
	public void shouldCanonicalizeNormalURLWithParametersIn_IGNORE_VALUE_mode() throws URIException {
		URI uri = new URI("http", null,"host", 9001, "/myservlet","p1=2&p2=myparam");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, HandleParametersOption.IGNORE_VALUE, false /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/myservlet?p1&p2"));
	}


	// Test the OData behavior

	@Test
	public void shouldCanonicalizeODataIDSimpleIn_USE_ALL_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.USE_ALL;

		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(1)"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(1)/Author"));
	}

	@Test
	public void shouldCanonicalizeODataIDSimpleIn_IGNORE_COMPLETELY_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.IGNORE_COMPLETELY;

		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
	}

	@Test
	public void shouldCanonicalizeODataIDSimpleIn_IGNORE_VALUE_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.IGNORE_VALUE;

		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(1)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
	}

	@Test
	public void shouldCanonicalizeODataIDMultipleIn_USE_ALL_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.USE_ALL;

		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(title='dummy',year=2012)"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(title='dummy',year=2012)/Author"));
	}

	@Test
	public void shouldCanonicalizeODataIDMultipleIn_IGNORE_COMPLETELY_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.IGNORE_COMPLETELY;

		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book()/Author"));
	}

	@Test
	public void shouldCanonicalizeODataIDMultipleIn_IGNORE_VALUE_mode() throws URIException {
		HandleParametersOption spiderOpion = HandleParametersOption.IGNORE_VALUE;
		
		URI uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)");
		String 	visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(title,year)"));

		uri = new URI("http", null,"host", 9001, "/app.svc/Book(title='dummy',year=2012)/Author");
		visitedURI = URLCanonicalizer.buildCleanedParametersURIRepresentation(uri, spiderOpion, true /* handleODataParametersVisited */);
		assertThat(visitedURI, is("http://host:9001/app.svc/Book(title,year)/Author"));
	}
	
}
