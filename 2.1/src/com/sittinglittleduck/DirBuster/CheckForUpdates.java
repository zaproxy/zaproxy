/*
 * CheckForUpdates.java
 *
 * Copyright 2008 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */
package com.sittinglittleduck.DirBuster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * @author James
 */
public class CheckForUpdates implements Runnable {

	/*
	 * Format for the expected XML:
	 * <version current="x.x.x-xxxx"/><changelog>text...........</changelog>
	 */
	private static final Pattern VERSION_PATTERN = Pattern
			.compile("<version current=\\\"(.*?)\\\"/>");
	private static final Pattern CHANGELOG_PATTERN = Pattern.compile(
			"<changelog>(.*?)</changelog>", Pattern.DOTALL);

	private static final String DIRBUSTER_SOURCEFORGE_URL = "https://sourceforge.net/project/showfiles.php?group_id=199126";

	private static final String RETRY_MESSAGE = "Please try again later";
	private static final String DEFAULT_ERROR_MESSAGE = "Sorry there has been an error while checking for the latest version\n\n";

	private static final String CHANGELOG_ERROR_MESSAGE = "Sorry it has not been possible to check if this the latest version\n\n"
			+ RETRY_MESSAGE;
	private static final String BROWSER_ERROR_MESSAGE = "Sorry it has not been possible to open your browser to get the latest version\n\n";
	private static final String VERSION_ERROR_MESSAGE = DEFAULT_ERROR_MESSAGE
			+ RETRY_MESSAGE;
	private static final String OTHER_ERROR_MESSAGE = DEFAULT_ERROR_MESSAGE
			+ RETRY_MESSAGE;
	private static final String SERVER_ERROR_MESSAGE = DEFAULT_ERROR_MESSAGE
			+ RETRY_MESSAGE;

	private Manager manager;
	private HttpClient httpclient;
	String updateURL = "http://www.sittinglittleduck.com/DirBuster/checkForUpdate.php?version="
			+ Config.version;
	boolean informUser = false;

	public CheckForUpdates(boolean informUser) {
		manager = Manager.getInstance();
		httpclient = manager.getHttpclient();
		this.informUser = informUser;
	}

	public void run() {
		try {
			GetMethod httpget = new GetMethod(updateURL);
			int responseCode = httpclient.executeMethod(httpget);

			if (responseCode == 200) {
				if (httpget.getResponseContentLength() > 0) {

					// get the http body
					String response = "";
					try (BufferedReader input = new BufferedReader(
							new InputStreamReader(
									httpget.getResponseBodyAsStream()))) {
						String line;

						StringBuffer buf = new StringBuffer();
						while ((line = input.readLine()) != null) {
							buf.append("\r\n" + line);
						}
						response = buf.toString();
					}

					Matcher versionMatcher = VERSION_PATTERN.matcher(response);
					if (versionMatcher.find()) {
						String latestversion = versionMatcher.group(1);
						if (latestversion.equalsIgnoreCase("Running - latest")) {
							showAlreadyLatestVersionMessage();
						} else {
							Matcher changelogMatcher = CHANGELOG_PATTERN
									.matcher(response);
							if (changelogMatcher.find()) {
								String changelog = changelogMatcher.group(1);

								if (!manager.isHeadLessMode()) {
									if (showUpdateToLatestVersionConfirmDialog(
											latestversion, changelog) == JOptionPane.OK_OPTION) {
										BrowserLauncher launcher;
										try {
											launcher = new BrowserLauncher(null);
											launcher.openURLinBrowser(DIRBUSTER_SOURCEFORGE_URL);
										} catch (
												BrowserLaunchingInitializingException
												| UnsupportedOperatingSystemException ex) {
											showErrorMessage(BROWSER_ERROR_MESSAGE
													+ ex.getMessage());
										}
									}
								} else {
									printUpdateMessageOnConsole(latestversion);
								}
							} else {
								showErrorMessage(CHANGELOG_ERROR_MESSAGE);
							}

						}
					} else {
						showErrorMessage(VERSION_ERROR_MESSAGE);
					}

				} else {
					showErrorMessage(OTHER_ERROR_MESSAGE);
				}

			}
			else {
				showErrorMessage(SERVER_ERROR_MESSAGE);
			}
		} catch (IOException ex) {
			showErrorMessage(DEFAULT_ERROR_MESSAGE + ex.getMessage());
		}
	}

	private int showUpdateToLatestVersionConfirmDialog(String latestversion,
			String changelog) {
		return JOptionPane
				.showConfirmDialog(
						manager.gui,
						"A new version of DirBuster ("
								+ latestversion
								+ ") is available\n\n"
								+ "Change log:\n\n"
								+ changelog
								+ "\n\n"
								+ "Do you wish to get the new version now?\n\n"
								+ "(Auto checking can be disabled from 'Advanced Options -> DirBuster Options')",
						"A new version of DirBuster is Avaliable",
						JOptionPane.OK_CANCEL_OPTION);
	}

	private void showAlreadyLatestVersionMessage() {
		if (informUser) {
			JOptionPane.showMessageDialog(manager.gui,
					"You are running the latest version",
					"You are running the latest version",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void printUpdateMessageOnConsole(String latestversion) {
		System.out.println("@@@@@@@@@@@@@@@@@@@");
		System.out.println("Version " + latestversion
				+ " of DirBuster is available");
		System.out.println("Download it from: " + DIRBUSTER_SOURCEFORGE_URL);
		System.out.println("@@@@@@@@@@@@@@@@@@@");
	}

	private void showErrorMessage(String message) {
		if (informUser) {
			JOptionPane.showMessageDialog(manager.gui, message, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
