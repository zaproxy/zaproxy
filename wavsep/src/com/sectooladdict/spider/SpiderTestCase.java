package com.sectooladdict.spider;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * This class is used to store a Test Case for the Spider tester.
 * 
 * @author Cosmin Stefan-Dobrin
 * 
 */
public class SpiderTestCase {

	/**
	 * The name of this test case.
	 */
	private String name;

	/**
	 * The url where this test can be found.
	 */
	private String url;

	/**
	 * This List is used to store the links that have to be visited by the test
	 * so that it can be considered passed.
	 */
	private LinkedList<String> requirements;

	public SpiderTestCase(String name, String baseUrl) {
		super();
		this.name = name;
		this.url = baseUrl;
		this.requirements = new LinkedList<String>();
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void putRequirement(String url) {
		requirements.add(url);
	}

	public boolean isTestPassed(HashSet<String> visitedPages) {
		for (String req : requirements)
			if (!visitedPages.contains(req))
				return false;
		return true;

	}

}
