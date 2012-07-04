package com.sectooladdict.spider;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * This class is used to hold statistics regarding what pages have been visited
 * by a spider using the Wavsep.
 * 
 * @author cosmin
 * 
 */
public class SpiderStatistics {

	private static final LinkedList<SpiderTestCase> tests = new LinkedList<SpiderTestCase>();

	private static final HashSet<String> visited = new HashSet<String>();

	static {
		SpiderStatistics.init();
	}

	/**
	 * Initialize the Test Cases for the Spider Tester.
	 */
	private static void init() {

		// Clear stuff
		reset();
		tests.clear();

		// Add the test cases
		SpiderTestCase test;

		test = new SpiderTestCase("Basic Test 1 - Simple Absolute Link");
		test.putRequirement("/spider/SpiderBasicTest1SimpleAbsoluteLink/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest1SimpleAbsoluteLink/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 2 - Simple Relative Links");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/b.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/c.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 3 - Link chain of size 5");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/b.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/c.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/d.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/e.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 4 - Multi Folder Link");
		test.putRequirement("/spider/SpiderBasicTest4MultiFolder/1/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest4MultiFolder/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 5 - Link with parameters");
		test.putRequirement("/spider/SpiderBasicTest5Parameters/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest5Parameters/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 6 - Img Elements");
		test.putRequirement("/spider/SpiderBasicTest6Img/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest6Img/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest6Img/image.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 1 - Page with simple frames");
		test.putRequirement("/spider/SpiderMediumTest1Frames/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/b.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/c.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 2 - Page with multiple frames");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/b.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/c.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/d.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 3 - Page with simple iframe");
		test.putRequirement("/spider/SpiderMediumTest3IFrames/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest3IFrames/b.jsp");
		test.putRequirement("/spider/SpiderMediumTest3IFrames/c.jsp");
		test.putRequirement("/spider/SpiderMediumTest3IFrames/a.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Medium Test 4 - Page which sets simple cookies");
		test.putRequirement("/spider/SpiderMediumTest4BasicCookies/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest4BasicCookies/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest4BasicCookies/b.jsp");
		tests.add(test);
		
		test = new SpiderTestCase(
				"Medium Test 5 - Page containing simple HTML form with GET method.");
		test.putRequirement("/spider/SpiderMediumTest5FormGet/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest5FormGet/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest5FormGet/b.jsp");
		tests.add(test);
		
		test = new SpiderTestCase(
				"Advanced Test 1 - Page which sets cookies with Path attribute");
		test.putRequirement("/spider/SpiderAdvancedTest1CookieAttributes/index.jsp");
		test.putRequirement("/spider/SpiderAdvancedTest1CookieAttributes/b.jsp");
		test.putRequirement("/spider/SpiderAdvancedTest1CookieAttributes/a.jsp");
		test.putRequirement("/spider/SpiderAdvancedTest1CookieAttributes/c.jsp");
		tests.add(test);
	}

	public static void addVisited(String url) {
		visited.add(url);
		System.out.println("Visited: " + url);

	}

	public static void reset() {
		visited.clear();
	}

	public static String getStatistics() {
		StringBuffer out = new StringBuffer();

		out.append("<table border='1'>\n");
		out.append("<tr><th class='tname'>Test name</th><th class='tstatus'>Test Status</th></tr>\n");

		for (SpiderTestCase test : tests) {
			out.append("<tr>");

			out.append("<td>" + test.getName() + "</td>");
			if (test.isTestPassed(visited))
				out.append("<td class='passed'>PASSED</td>");
			else
				out.append("<td class='failed'>FAILED</td>");

			out.append("</tr>\n");
		}

		out.append("</table>");

		return out.toString();
	}
}
