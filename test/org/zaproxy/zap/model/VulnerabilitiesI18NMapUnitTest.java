package org.zaproxy.zap.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class VulnerabilitiesI18NMapUnitTest {
	
	private VulnerabilitiesI18NMap vulnerabilitiesMap;
	private List<Vulnerability> list_default;
	private List<Vulnerability> list_de_DE;
	private List<Vulnerability> list_fr_FR;
	
	@Before
	public void before() {
		vulnerabilitiesMap = new VulnerabilitiesI18NMap();
		list_default = getVulnerabilityList("item_1", "item_2", "item_3");
		list_de_DE = getVulnerabilityList("Artikel_1", "Artikel_2", "Artikel_3");
		list_fr_FR = getVulnerabilityList("article_1", "article_2", "article_3");
	}
	
	@Test
	public void testGetVulnerabilityByNameOnyDefaultLocale() {
		loadOnlyDefault();
		assertSame(list_default.get(2), vulnerabilitiesMap.getVulnerabilityByName("item_3", "not important"));
	}
	

	private void loadOnlyDefault() {
		vulnerabilitiesMap.putVulnerabilitiesList(null, list_default);
	}

	@Test
	public void testGetVulnerabilityByNameOnyDefaultLocaleAndOneSpefic() {
		loadOnlyDefault();
		load_fr_FR();
		assertSame(list_default.get(0), vulnerabilitiesMap.getVulnerabilityByName("item_1", "not important"));
		assertSame(list_fr_FR.get(1), vulnerabilitiesMap.getVulnerabilityByName("article_2", "fr_FR"));
	}
	
	@Test
	public void testGetVulnerabilityByNameOnyDefaultLocaleAndTwoSpefic() {
		loadAll();
		assertSame(list_default.get(2), vulnerabilitiesMap.getVulnerabilityByName("item_3", "not important"));
		assertSame(list_de_DE.get(2), vulnerabilitiesMap.getVulnerabilityByName("Artikel_3", "de_DE"));
		assertSame(list_fr_FR.get(1), vulnerabilitiesMap.getVulnerabilityByName("article_2", "fr_FR"));

	}
	
	@Test
	public void testGetVulnerabilitiesListOnlyDefaultLocale() {
		loadOnlyDefault();
		assertSame(list_default, vulnerabilitiesMap.getVulnerabilityList("not_important"));
	}

	@Test
	public void testGetVulnerabilitiesListWithDefaultLocaleAndOneSpecfic() {
		loadOnlyDefault();
		load_de_DE();
		
		assertSame("Unknown locale should return default list", list_default, vulnerabilitiesMap.getVulnerabilityList("not_important"));
		assertSame("Locale de_DE should return the de_DE list", list_de_DE, vulnerabilitiesMap.getVulnerabilityList("de_DE"));
	}

	

	@Test
	public void testGetVulnerabilitiesListWithDefaultLocaleAndTwoSpecfic() {
		loadAll();
		
		assertSame("Unknown locale should return default list", list_default, vulnerabilitiesMap.getVulnerabilityList("not_important"));
		assertSame("Locale de_DE should return the de_DE list", list_de_DE, vulnerabilitiesMap.getVulnerabilityList("de_DE"));
		assertSame("Locale fr_FR should return the fr_FR list", list_fr_FR, vulnerabilitiesMap.getVulnerabilityList("fr_FR"));
	}

	private void load_de_DE() {
		vulnerabilitiesMap.putVulnerabilitiesList("de_DE", list_de_DE);
	}
	
	private void loadAll() {
		loadOnlyDefault();
		load_de_DE();
		load_fr_FR();
	}

	private void load_fr_FR() {
		vulnerabilitiesMap.putVulnerabilitiesList("fr_FR", list_fr_FR);
	}	
	
	private List<Vulnerability> getVulnerabilityList(String item1,
			String item2, String item3) {
		ArrayList<Vulnerability> list = new ArrayList<>();
		list.add(createVulnerability(item1));
		list.add(createVulnerability(item2));
		list.add(createVulnerability(item3));
				
		return list;
	}
	private Vulnerability createVulnerability(String id) {
		List<String> reference = Collections.emptyList();
		return new Vulnerability(id, "alert for" + id, "description for " + id, "solution for" + id, reference);
	}

}
