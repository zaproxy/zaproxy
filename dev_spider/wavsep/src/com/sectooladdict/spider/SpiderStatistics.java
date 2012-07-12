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

	private static final String APP_FOLDER = "/Wavsep";

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

		test = new SpiderTestCase("Basic Test 1 - Simple Absolute Link",
				"/spider/SpiderBasicTest1SimpleAbsoluteLink/");
		test.putRequirement("/spider/SpiderBasicTest1SimpleAbsoluteLink/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest1SimpleAbsoluteLink/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 2 - Simple Relative Links",
				"/spider/SpiderBasicTest2SimpleRelativeLinks/");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/b.jsp");
		test.putRequirement("/spider/SpiderBasicTest2SimpleRelativeLinks/c.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 3 - Link chain of size 5",
				"/spider/SpiderBasicTest3LinkChain/");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/b.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/c.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/d.jsp");
		test.putRequirement("/spider/SpiderBasicTest3LinkChain/e.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 4 - Multi Folder Link",
				"/spider/SpiderBasicTest4MultiFolder/");
		test.putRequirement("/spider/SpiderBasicTest4MultiFolder/1/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest4MultiFolder/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 5 - Link with parameters",
				"/spider/SpiderBasicTest5Parameters/");
		test.putRequirement("/spider/SpiderBasicTest5Parameters/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest5Parameters/index.jsp");
		tests.add(test);

		test = new SpiderTestCase("Basic Test 6 - Img Elements",
				"/spider/SpiderBasicTest6Img/");
		test.putRequirement("/spider/SpiderBasicTest6Img/index.jsp");
		test.putRequirement("/spider/SpiderBasicTest6Img/a.jsp");
		test.putRequirement("/spider/SpiderBasicTest6Img/image.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 1 - Page with simple frames",
				"/spider/SpiderMediumTest1Frames/");
		test.putRequirement("/spider/SpiderMediumTest1Frames/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/b.jsp");
		test.putRequirement("/spider/SpiderMediumTest1Frames/c.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 2 - Page with multiple frames",
				"/spider/SpiderMediumTest2MultipleFrames/");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/index.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/a.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/b.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/c.jsp");
		test.putRequirement("/spider/SpiderMediumTest2MultipleFrames/d.jsp");
		tests.add(test);

		// From now on put the requirements relative to the test base url
		test = new SpiderTestCase("Medium Test 3 - Page with simple iframe",
				"/spider/SpiderMediumTest3IFrames/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "b.jsp");
		test.putRequirement(test.getUrl() + "c.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Medium Test 4 - Page which sets simple cookies",
				"/spider/SpiderMediumTest4BasicCookies/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		test.putRequirement(test.getUrl() + "b.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Medium Test 5 - Page containing simple HTML form with GET method.",
				"/spider/SpiderMediumTest5FormGet/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		test.putRequirement(test.getUrl() + "b.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Medium Test 6 - Page containing simple HTML form with POST method.",
				"/spider/SpiderMediumTest6FormPost/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Medium Test 7 - Page containing unescaped characters (i.e. NON-ASCII) in URIs.",
				"/spider/SpiderMediumTest7NonEscapedPaths/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "Liste_des_Wikipédias.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		tests.add(test);

		test = new SpiderTestCase("Medium Test 8 - Non HTML text files",
				"/spider/SpiderMediumTest8NonHTMLFiles/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "txt_file.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Advanced Test 1 - Page which sets cookies with Path attribute",
				"/spider/SpiderAdvancedTest1CookieAttributes/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "b.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		test.putRequirement(test.getUrl() + "c.jsp");
		tests.add(test);

		test = new SpiderTestCase(
				"Advanced Test 2 - Form with all available HTML5 input types",
				"/spider/SpiderAdvancedTest2FormControls/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		tests.add(test);

		test = new SpiderTestCase("Advanced Test 3 - Links in Comments",
				"/spider/SpiderAdvancedTest3LinksInComments/");
		test.putRequirement(test.getUrl() + "index.jsp");
		test.putRequirement(test.getUrl() + "a.jsp");
		test.putRequirement(test.getUrl() + "b.jsp");
		tests.add(test);
	}

	/**
	 * Marks the url provided as parameter as visited in the corresponding test
	 * 
	 * @param url
	 */
	public static void addVisited(String url) {
		visited.add(url);
		System.out.println("Visited: " + url);

	}

	/**
	 * Resets all the tests, clearing all visited pages so far.
	 */
	public static void reset() {
		visited.clear();
	}

	/**
	 * Build the HTML table representation for all the results of tests so far.
	 * 
	 * @return the HTML table representation
	 */
	public static String buildStatisticsTable() {
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

	/**
	 * Build the HTML table representation for all the links to the start pages
	 * of each test.
	 * 
	 * @return the HTML table representation
	 */
	public static String buildLinkTable() {
		StringBuffer out = new StringBuffer();

		out.append("<table border='1'>\n");
		out.append("<tr><th class='tname'>Test name</th><th>Test Start Link</th></tr>\n");

		for (SpiderTestCase test : tests) {
			out.append("<tr>");
			out.append("<td>" + test.getName() + "</td>");
			out.append("<td align='center'><a href='" + APP_FOLDER
					+ test.getUrl() + "'> Start Link </a></td>");
			out.append("</tr>\n");
		}

		out.append("</table>");

		return out.toString();
	}
}
