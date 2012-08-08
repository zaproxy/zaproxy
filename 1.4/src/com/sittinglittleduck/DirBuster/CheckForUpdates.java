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
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 *
 * @author James
 */
public class CheckForUpdates implements Runnable
{

    /*
     * Format for the expected xml
     * 
     * <version current="x.x.x-xxxx"/>
     * <changelog>
     *      text...................
     * <changelog>
     * 
     */

    /*
     * instance of the manager
     */
    private Manager manager;
    private HttpClient httpclient;
    String updateURL = "http://www.sittinglittleduck.com/DirBuster/checkForUpdate.php?version=" + Config.version;
    //String updateURL = "http://127.0.0.1/checkForUpdate.php?version=" + Config.version;
    boolean informUser = false;

    public CheckForUpdates(boolean informUser)
    {
        manager = Manager.getInstance();
        httpclient = manager.getHttpclient();
        this.informUser = informUser;
    }

    public void run()
    {
        try
        {
            /*
             * query the site to find out is there any updates
             */
            GetMethod httpget = new GetMethod(updateURL);
            int responceCode = httpclient.executeMethod(httpget);

            if(responceCode == 200)
            {
                if(httpget.getResponseContentLength() > 0)
                {

                    //get the http body
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpget.getResponseBodyAsStream()));

                    String line;

                    String responce = "";

                    StringBuffer buf = new StringBuffer();
                    while((line = input.readLine()) != null)
                    {
                        buf.append("\r\n" + line);
                    }
                    responce = buf.toString();
                    input.close();

                    //System.out.println("Got a responce form the update server");
                    //System.out.println("-------------------------------------");
                    //System.out.println(responce);
                    //System.out.println("-------------------------------------");
                    /*
                     * extract the data from the responce
                     */
                    String versionRegex = "<version current=\\\"(.*?)\\\"/>";
                    String changeLogRegex = "<changelog>(.*?)</changelog>";

                    Pattern regexFindFile = Pattern.compile(versionRegex);

                    Matcher m = regexFindFile.matcher(responce);

                    if(m.find())
                    {
                        String latestversion = m.group(1);
                        if(latestversion.equalsIgnoreCase("Running - latest"))
                        {
                            /*
                             * no updates needed
                             */
                            if(informUser)
                            {
                                JOptionPane.showMessageDialog(manager.gui, "You are running the latest version", "You are running the latest version", JOptionPane.INFORMATION_MESSAGE);
                            }
                            return;
                        }
                        else
                        {
                            //System.out.println("You are not running the latest version");
                            /*
                             * This is not the latest version of Dirbuster
                             */
                            Pattern regexFindChangeLog = Pattern.compile(changeLogRegex, Pattern.DOTALL);

                            m = regexFindChangeLog.matcher(responce);

                            if(m.find())
                            {
                                String changelog = m.group(1);
                                //System.out.println("found changelog: " + changelog);


                                if(!manager.isHeadLessMode())
                                {
                                    /*
                                     * display pop to user about the fact there is a new version
                                     */
                                    int n = JOptionPane.showConfirmDialog(
                                            manager.gui,
                                            "A new version of DirBuster (" + latestversion + ") is available\n\n" +
                                            "Change log:\n\n" +
                                            changelog + "\n\n" +
                                            "Do you wish to get the new version now?\n\n" +
                                            "(Auto checking can be disabled from 'Advanced Options -> DirBuster Options')",
                                            "A new version of DirBuster is Avaliable",
                                            JOptionPane.OK_CANCEL_OPTION);
                                    //if the anwser is ok
                                    if(n == 0)
                                    {
                                        //System.out.println("User clicked ok");
                                        BrowserLauncher launcher;
                                        try
                                        {
                                            /*
                                             * open the sourceforge page
                                             */
                                            launcher = new BrowserLauncher(null);
                                            launcher.openURLinBrowser("https://sourceforge.net/project/showfiles.php?group_id=199126");
                                        }
                                        catch(BrowserLaunchingInitializingException ex)
                                        {
                                            if(informUser)
                                            {
                                                JOptionPane.showMessageDialog(manager.gui, "Sorry it has not been possible to open your browser to get the latest version\n\n" +
                                                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                            }
                                            return;
                                        }
                                        catch(UnsupportedOperatingSystemException ex)
                                        {
                                            if(informUser)
                                            {
                                                JOptionPane.showMessageDialog(manager.gui, "Sorry it has not been possible to open your browser to get the latest version\n\n" +
                                                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                            }
                                            return;
                                        }

                                    }
                                }
                                else
                                {
                                    /*
                                     * inform user when we are in headles mode
                                     */
                                    System.out.println("@@@@@@@@@@@@@@@@@@@");
                                    System.out.println("Version " + latestversion + " of DirBuster is available");
                                    System.out.println("Download it from: https://sourceforge.net/project/showfiles.php?group_id=199126");
                                    System.out.println("@@@@@@@@@@@@@@@@@@@");
                                }
                            }
                            else
                            {
                                /*
                                 * error cant find the changelog
                                 */
                                if(informUser)
                                {
                                    JOptionPane.showMessageDialog(manager.gui, "Sorry it has not been possible to check for the if this the latest version\n\n" +
                                            "Please try again later", "Error", JOptionPane.ERROR_MESSAGE);
                                }

                            }

                        }
                    }
                    else
                    {
                        /*
                         * error can't find the version
                         */
                        if(informUser)
                        {
                            JOptionPane.showMessageDialog(manager.gui, "Sorry there has been an error while checking for the latest version\n\n" +
                                    "Please try again later", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                }
                else
                {
                    /*
                     * another error!
                     */

                    if(informUser)
                    {
                        JOptionPane.showMessageDialog(manager.gui, "Sorry there has been an error while checking for the latest version\n\n" +
                                "Please try again later", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
            /*
             * there is a problem with the server
             */
            else
            {
                if(informUser)
                {
                    JOptionPane.showMessageDialog(manager.gui, "Sorry there has been an error while checking for the latest version\n\n" +
                            "Please try again later", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        catch(HttpException ex)
        {
            if(informUser)
            {
                JOptionPane.showMessageDialog(manager.gui, "Sorry there has been an error while checking for the latest version\n\n" +
                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        catch(IOException ex)
        {
            if(informUser)
            {
                JOptionPane.showMessageDialog(manager.gui, "Sorry there has been an error while checking for the latest version\n\n" +
                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }
}
