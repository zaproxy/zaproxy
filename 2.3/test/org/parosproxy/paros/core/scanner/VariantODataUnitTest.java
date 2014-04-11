package org.parosproxy.paros.core.scanner;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests VariantODataIdQuery and VariantODataFilterQuery
 */
public class VariantODataUnitTest {

	private static final Variant VARIANT_ODATA_ID_QUERY    = new  VariantODataIdQuery();
	private static final Variant VARIANT_ODATA_FILTER_QUERY= new  VariantODataFilterQuery();

	/**
	 * Test intended to demonstrate a basic use case and help developing the class 
	 * Handling the OData resource ID (simple ID)
	 * 
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldAbleToInjectValueInODataSimpleResourceID() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_ID_QUERY,
				              new URI("http",null,"localhost",50050,"/remoting/servlet.svc/Book('BOOK1')/Summary","%24format=json"),
							  "__ID__Book", "'BOOK1'",
							  "'hacked'",
							  "http://localhost:50050/remoting/servlet.svc/Book('hacked')/Summary?%24format=json"
							   );
	}


	/**
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData resource ID (composite ID)
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldAbleToInjectValueInODataCompositeResourceID() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_ID_QUERY,
				              new URI("http",null,"localhost",50050,"/remoting/servlet.svc/DisplayItem(seqno=576460752035250185L,table='B0A43AEFE9A9FA0441AFE5302EF534A707CF0834F87A6EA884FB425A1C996EB1CA699ADDD2B48F13')"),
				              "seqno", "576460752035250185L",
				              "hacked",
				              "http://localhost:50050/remoting/servlet.svc/DisplayItem(seqno=hacked,table='B0A43AEFE9A9FA0441AFE5302EF534A707CF0834F87A6EA884FB425A1C996EB1CA699ADDD2B48F13')"
				              );
	}

	/**
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData resource ID (composite ID)
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldAbleToInjectValueInODataCompositeResourceID2() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_ID_QUERY,
							  new URI("http",null,"localhost",50050,"/remoting/servlet.svc/DisplayItem(seqno=576460752035250185L,table='B0A43AEFE9A9FA0441AFE5302EF534A707CF0834F87A6EA884FB425A1C996EB1CA699ADDD2B48F13')"),
				              "table", "'B0A43AEFE9A9FA0441AFE5302EF534A707CF0834F87A6EA884FB425A1C996EB1CA699ADDD2B48F13'",
				              "hacked",
				              "http://localhost:50050/remoting/servlet.svc/DisplayItem(seqno=576460752035250185L,table=hacked)"
				              );

	}

	/**
	 *
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData filter expression
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToInjectValueInODataFilterParameters() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_FILTER_QUERY,
							  new URI("http",null,"localhost",15050,"/remoting/servlet.svc/Book","$top=3&$select=name&$filter=(p1 eq 5) and (param2 gt 6) and startswith(code,'Once')$format=json"),
				              "p1", "5",
	                          "hacked",
	                          "http://localhost:15050/remoting/servlet.svc/Book?$top=3&$select=name&$filter=(p1 eq hacked) and (param2 gt 6) and startswith(code,'Once')$format=json"
	              );
	}


	/**
	 *
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData filter expression
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToInjectValueInODataFilterParametersHavingBlanksNearEquals1() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_FILTER_QUERY,
				              new URI("http",null,"localhost",15050,"/remoting/servlet.svc/Book","$top=3&$select=name&$filter =(p1 eq 5) and (param2 gt 6) and startswith(code,'Once')$format=json"),
	                          "param2", "6",
                              "hacked",
                              "http://localhost:15050/remoting/servlet.svc/Book?$top=3&$select=name&$filter =(p1 eq 5) and (param2 gt hacked) and startswith(code,'Once')$format=json"
               );
	}


	/**
	 *
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData filter expression
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToInjectValueInODataFilterParametersHavingBlanksNearEquals2() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_FILTER_QUERY,
				              new URI("http",null,"localhost",15050,"/remoting/servlet.svc/Book","$top=3&$select=name&$filter = (p1 eq 5) and (param2 gt 6) and startswith(code,'Once')$format=json"),
	                          "param2", "6",
                              "hacked",
                              "http://localhost:15050/remoting/servlet.svc/Book?$top=3&$select=name&$filter = (p1 eq 5) and (param2 gt hacked) and startswith(code,'Once')$format=json"
               );
	}

	/**
	 *
	 * Test intended to demonstrate a basic use case and help developing the class
	 * Handling the OData filter expression
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToInjectValueInODataFilterParametersHavingBlanksNearEquals3() throws URIException, NullPointerException, CloneNotSupportedException {
		doTestInjectParameter(VARIANT_ODATA_FILTER_QUERY,
				              new URI("http",null,"localhost",15050,"/remoting/servlet.svc/Book","$top=3&$select=name&$filter= (p1 eq 5) and (param2 gt 6) and startswith(code,'Once')$format=json"),
	                          "param2", "6",
                              "hacked",
                              "http://localhost:15050/remoting/servlet.svc/Book?$top=3&$select=name&$filter= (p1 eq 5) and (param2 gt hacked) and startswith(code,'Once')$format=json"
               );
	}



	/**
	 * Test that the variant handles URLs without query element
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToHandleURIwithoutQuery() throws URIException, NullPointerException, CloneNotSupportedException {
		URI sourceURI = new URI("http",null,"localhost",15050,"/remoting/servlet.svc/Book");
		doTestInjectParameter(VARIANT_ODATA_FILTER_QUERY,
	              sourceURI,
                "param2", "6",
                "hacked",
                "http://localhost:15050/remoting/servlet.svc/Book"
                );

	}


	/**
	 * Test that the variant handles URLs without path element
	 *
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException
	 */
	@Test
	public void shouldBeAbleToHandleURIwithoutPath() throws URIException, NullPointerException, CloneNotSupportedException {
		URI sourceURI = new URI("http",null,"localhost",15050);
		doTestInjectParameter(VARIANT_ODATA_ID_QUERY,
	              sourceURI,
                "param2", "6",
                "hacked",
                "http://localhost:15050"
                );

	}



	protected String setParameter(HttpMessage msg, String param, String value,
			                      Variant variant, NameValuePair originalPair /* in standard code these params are attributes of the class */){
		return variant.setParameter(msg, originalPair, param, value);
	}


	/**
	 * Test that we can properly inject a new value to the sourceURI
	 *
	 * @param sourceURI
	 * @param paramName
	 * @param originalValue
	 * @param hackValue
	 * @param expectedHackedURI
	 * @throws org.apache.commons.httpclient.URIException
	 * @throws NullPointerException
	 * @throws CloneNotSupportedException 
	 */
	private void doTestInjectParameter(Variant variant, URI sourceURI, String paramName, String originalValue, String hackValue, String expectedHackedURI) throws URIException, NullPointerException, CloneNotSupportedException {
		
		// Given
		HttpMessage msg = new HttpMessage();
		msg.setRequestHeader(new HttpRequestHeader());
		
		
		//When
		NameValuePair originalPair = new NameValuePair(NameValuePair.TYPE_URL_PATH, paramName, originalValue, 1); 
		msg.getRequestHeader().setURI((URI) sourceURI.clone());		
		variant.setMessage(msg);

		String param = originalPair.getName(); // implicit parameter name for the entity Book		
		setParameter(msg, param, hackValue ,variant,originalPair);
		
		//Then
		// Check that the msg contains now well formated URI with the injected parameter
		URI hackedURI = msg.getRequestHeader().getURI();
		String hackedURIasStr = hackedURI.getURI();
		assertThat("RequestHeader.uri",hackedURIasStr, is(expectedHackedURI));

	}

	
	
}
