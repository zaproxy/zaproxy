package org.zaproxy.zap.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class VulnerabilitiesLoaderUnitTest {
	
	public VulnerabilitiesLoader loader;
	
	@Test
	public void LoadVulnerabilities() {
		loader = new VulnerabilitiesLoader("test/resources/vulnerabilities/", "vulnerabilities-test");
		VulnerabilitiesI18NMap map = loader.load();	
		
		List<Vulnerability> list_default = map.getVulnerabilityList("not important");
		assertNotNull(list_default);
		assertEquals("Insufficient Authorization", list_default.get(1).getAlert());
		
		List<Vulnerability> list_nl_NL = map.getVulnerabilityList("nl_NL");	
		assertNotNull(list_nl_NL);
		assertEquals("NL_1", list_nl_NL.get(0).getAlert());
	}
	
	public void LoadVulnerabilitiesWithError() {
		loader = new VulnerabilitiesLoader("test/resources/vulnerabilities/", "vulnerabilities-error");
		VulnerabilitiesI18NMap map = loader.load();
	}
}
