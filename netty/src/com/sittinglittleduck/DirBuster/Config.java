/*
 * config.java
 *
 * Copyright 2007 James Fisher
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

/**
 * Stores basic configuration detials
 * @author James
 */
public class Config
{

    /**
     * The version number of the program
     */
    public static final String version = "0.12";
    /*
     * 0.12
     * Command line interface added
     * Fixed a bug that caused the "User Agent" to not get set when adding custom headers
     * Updated all api's used
     *  
     * 0.11.1
     * Fixed a bug that caused the check for update not to work correctly
     * 
     * 0.11
     * Fixed a couple of points within the GUI, and spelling mistakes.
     * Added more content to the help section, but it's not finished yet.
     * Improved the way in which DirBuster handles inconsistent fail codes
     * Fixed a bug that caused deadlock due to all the parsing threads exiting
     * Tweaked the content analysis code to reduce false positives, when DirBuster is using that mode 
     * Added code to make sure it display correctly on Vista
     * Fixed a bug that caused files found to not be shown in the report
     * Slight tweak to worker to improve performance
     * Added a windows installer
     * 
     * 
     * 0.10
     * Fixed a bug that caused DirBuster to hang, when deselecting items to scan.
     * Fixed part of the HTML parse worker so it exits correctly
     * More work to finish the treetableview
     * Fixed bug that caused purebrute force mode to not work
     * Fixed bug that caused fuzz based pure brute force to not work correctly
     * Fixed bug that caused part of the code not to work with java 1.5
     * Added content length row into results table
     * Added a feature to check for new versions of DirBuster
     * Fixed bug reported by Ralf Hoelzer, where fuzzing does not correctly check the URL to be fuzzed
     * Fixed bug reported by Ralf Hoelzer, where if you run a "fuzz" and then switch to "list based" things broke
     * Fixed error when first item in the tree view was added
     * Fixed bug reported by Ralf Hoelzer, report generation fails if you tell it to write to directory and not a file
     * Added more icons
     * Added patch supplied by Ralf Hoelzer, to add a back button to the report panel
     * 
     * 0.9.12
     * Changed the look and feel
     * reset all the fonts
     * fixed a bug in the proxy settings, where it did not save the proxy port number
     * fixed bug under osx where the advance options buttons are not shown
     * fixed bug that stop recisive scanning from working
     * fixed bug where the parser workers did not restart
     * 
     * 0.9.11
     * Help section started
     * Fixed bug in advanced options, which caused proxy setting to always get set
     * Add an option to limit the number of requests/sec
     * Improved the way results table works
     * Fixed a bug that caused responce to be displayed incorrectly
     * Fixed bug that selection from the tables to now work correctly
     * Fixed bug that caused blank extentions to stop working at all!
     * 
     *0.9.10
     * + Fixed Bug that prevents it running on below java 1.6
     * 
     *0.9.9
     *+ gui now gives better information into what it is currently processing
     *+ updated gui to allow the setting of target by submitting a URL
     *+ skip function added
     *+ Linked in HTTP clients auth, to it can do NTLM
     *+ Fixed bug caused items in the running to not be removed, when the user selected to do so
     *+ Fixed bug that stopped saved proxy setting from being displayed
     *+ Added The ability to fuzz for valid files from a URL
     *+ Improved and redesigned the setup GUI
     *+ Added row sorting to the results table
     *
     *0.9.8
     *
     *+ Fixed bug in process checker
     *+ Added Error handling when the second GET, returns with a different result than the first HEAD
     *+ Added a few more comments to the code
     *+ Added code to prevent duplicates from getting into the work queue and the results table
     *+ Added second status label for future use
     *+ Added feature to allow the setting of connection timeout, default is 30 seconds
     *+ Improved some parts of the GUI
     *+ Imporved the error checking during start up, it will no longer try to test servers that are down
     *+ Will now scan mutiple file extentions
     *+ Added information about the supplied lists
     *+ Changed links and branding
     *+ Fixed bug with stoped start point from being scanned eg /
     *+ Added HTML parsing of found pages
     *+ Added optins to configure the HTML parsing
     *+ Imporved checking for if the server does not support HEAD requests
     *+ Impoved code, having run used "find bugs" to highlight issues
     *
     *0.9.7
     *+ Improved the the content anylsis mode
     *+ Added copy function to URL from running table
     *+ fixed the prue brute force work generator
     *+ added check to prevent the over writing of report files
     *+ minor layout changes
     *+ better auto detection of when servers dont suport HEAD requets
     *+ Added the ability to display information about the base case for each found directory/file
     *+ Fixed Reporting to remove html code from reports
     *+ Changed dirbuster to lesser GPL
     *+ Added Licence information
     *+ Corrected all jTextAreas, so they now start at the top of the text when they need scroll
     *
     *0.9.6
     *+ Dirbuster can now do file scanning with no extention.
     *+ View responce now shows information about errors as well
     *+ Removed all other imported source code, they will no be done via their own api's
     *
     *0.9.5
     *+ Fix now threads will actually stop!
     *+ Added custom headers when making a get if we had a sucessfull HEAD
     *+ Fix to stop it adding extra threads when restarting
     *
     *0.9.4
     *+ Fix to allow external apis to work with self signed certs
     *+ better calculation of ETA
     *+ Other small bugfixes
     *
     *
     *0.9.3
     *+ added proxy support
     *+ Fixed devide by zero on process checker
     *
     *0.9.2
     *+ Corrected content checking mode when a 200 is returned for the 404 test, so it now works
     *+ minor changes to the interface
     *+ now shows diff of basecase vs responce
     */
    /**
     * Date the version was completed
     */
    public static final String versionDate = "06/05/2008";
    /**
     * User agent that will be used
     */
    public static String userAgent = "DirBuster-" + version + " (http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project)";
    /**
     * Default debug setting
     */
    public static boolean debug = false;
    /**
     * Default setting for following redirects
     */
    public static boolean followRedirects = false;
    /**
     * Default setting for connection timeout, in seconds
     */
    public static int connectionTimeout = 30;
    /**
     * Default setting for connection timeout, in seconds
     */
    public static String failCaseString = "thereIsNoWayThat-You-CanBeThere";
    public static boolean parseHTML = true;
    public static boolean parseHTMLa = true;
    public static boolean parseHTMLimg = true;
    public static boolean parseHTMLform = true;
    /*
     * marker to say if we should treat the server case sensative
     */
    public static boolean caseInsensativeMode = false;
}
