/*
 * WorkerGenerator.java
 *
 * Created on 11 November 2005, 20:33
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
package com.sittinglittleduck.DirBuster.workGenerators;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import com.sittinglittleduck.DirBuster.BaseCase;
import com.sittinglittleduck.DirBuster.Config;
import com.sittinglittleduck.DirBuster.DirToCheck;
import com.sittinglittleduck.DirBuster.ExtToCheck;
import com.sittinglittleduck.DirBuster.GenBaseCase;
import com.sittinglittleduck.DirBuster.HTTPHeader;
import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.WorkUnit;

/**
 * Produces the work to be done, when we are reading from a list
 */
public class WorkerGenerator implements Runnable
{

    private Manager manager;
    private BlockingQueue<WorkUnit> workQueue;
    private BlockingQueue<DirToCheck> dirQueue;
    private String inputFile;
    private String firstPart;
    private String fileExtention;
    private String finished;
    private String started;
    private boolean stopMe = false;
    private boolean skipCurrent = false;
    HttpClient httpclient;

    /**
     * Creates a new instance of WorkerGenerator
     * @param manager Manager object
     */
    public WorkerGenerator()
    {
        manager = Manager.getInstance();
        workQueue = manager.workQueue;
        dirQueue = manager.dirQueue;
        if(manager.isBlankExt())
        {
            fileExtention = "";
        }
        else
        {
            fileExtention = "." + manager.getFileExtention();
        }

        //get the vector of all the file extention we need to use
        //extToCheck = manager.getExtToUse();
        inputFile = manager.getInputFile();
        firstPart = manager.getFirstPartOfURL();

        httpclient = manager.getHttpclient();
    }

    /**
     * Thread run method
     */
    public void run()
    {
        String currentDir = "/";
        int failcode = 404;
        String line;
        Vector extToCheck = new Vector(10, 5);
        boolean recursive = true;
        int passTotal = 0;

        //--------------------------------------------------
        try
        {

            //find the total number of requests to be made, per pass
            //based on the fact there is a single entry per line
            BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            passTotal = 0;
            while((line = d.readLine()) != null)
            {
                if(!line.startsWith("#"))
                {
                    passTotal++;
                }
            }

            manager.setTotalPass(passTotal);
        }
        catch(FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        //-------------------------------------------------

        //checks if the server surports heads requests
        if(manager.getAuto())
        {
            try
            {
                URL headurl = new URL(firstPart);

                HeadMethod httphead = new HeadMethod(headurl.toString());

                //set the custom HTTP headers
                Vector HTTPheaders = manager.getHTTPHeaders();
                for(int a = 0; a < HTTPheaders.size(); a++)
                {
                    HTTPHeader httpHeader = (HTTPHeader) HTTPheaders.elementAt(a);
                    /*
                     * Host header has to be set in a different way!
                     */
                    if(httpHeader.getHeader().startsWith("Host:"))
                    {
                        httphead.getParams().setVirtualHost(httpHeader.getValue());
                    }
                    else
                    {
                        httphead.setRequestHeader(httpHeader.getHeader(), httpHeader.getValue());
                    }

                }

                httphead.setFollowRedirects(Config.followRedirects);
                int responceCode = httpclient.executeMethod(httphead);

                if(Config.debug)
                {
                    System.out.println("DEBUG WokerGen: responce code for head check = " + responceCode);
                }

                //if the responce code is method not implemented or if the head requests return 400!
                if(responceCode == 501 || responceCode == 400 || responceCode == 405)
                {
                    if(Config.debug)
                    {
                        System.out.println("DEBUG WokerGen: Changing to GET only HEAD test returned 501(method no implmented) or a 400");
                    }
                    //switch the mode to just GET requests
                    manager.setAuto(false);
                }
            }
            catch(MalformedURLException e)
            {
                //TODO deal with error
            }
            catch(IOException e)
            {
                //TODO deal with error
            }
        }

        //end of checks to see if server surpports head requests
        int counter = 0;

        while((!dirQueue.isEmpty() || !workQueue.isEmpty() || !manager.areWorkersAlive()) && recursive)
        {
            //get the dir we are about to process
            String baseResponce = null;
            recursive = manager.isRecursive();
            BaseCase baseCaseObj = null;

            //rest the skip
            skipCurrent = false;

            //deal with the dirs
            try
            {
                //get item from  queue
                //System.out.println("gen about to take");
                DirToCheck tempDirToCheck = dirQueue.take();
                //System.out.println("gen taken");
                //get dir name
                currentDir = tempDirToCheck.getName();
                //get any extention that need to be checked
                extToCheck = tempDirToCheck.getExts();

                manager.setCurrentlyProcessing(currentDir);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            manager.setStatus("Starting Brute force of dirs in " + currentDir);
            started = currentDir;
            manager.updateTable(finished, started);

            //generate the list of dirs
            if(manager.getDoDirs())
            {
                //find the fail case for the dir
                URL failurl = null;

                try
                {
                    baseResponce = null;
                    manager.setStatus("Getting fail case for " + currentDir);

                    baseCaseObj = GenBaseCase.genBaseCase(firstPart + currentDir, true, null);
                }
                catch(MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }


                //end of dir fail case
                if(stopMe)
                {
                    return;
                }

                //generate work links
                try
                {
                    manager.setStatus("Brute forceing dirs in " + currentDir);
                    //readin dir names
                    BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

                    if(Config.debug)
                    {
                        System.out.println("DEBUG WokerGen: Generating dir list for " + firstPart);
                    }

                    URL currentURL;

                    //add the first item while doing dir's
                    if(counter == 0)
                    {
                        try
                        {
                            String method;
                            if(manager.getAuto() && !baseCaseObj.useContentAnalysisMode() && !baseCaseObj.isUseRegexInstead())
                            {
                                method = "HEAD";
                            }
                            else
                            {
                                method = "GET";
                            }
                            currentURL = new URL(firstPart + currentDir);
                            //System.out.println("first part = " + firstPart);
                            //System.out.println("current dir = " + currentDir);
                            if(!manager.isHeadLessMode())
                            {
                                if(currentDir.equals(manager.gui.jPanelSetup.jTextFieldDirToStart.getText()))
                                {
                                    manager.updateTable("", currentDir);
                                }
                            }
                            workQueue.put(new WorkUnit(currentURL, true, "GET", baseCaseObj, null));
                            if(Config.debug)
                            {
                                System.out.println("DEBUG WokerGen: 1 adding dir to work list " + method + " " + currentDir.toString());
                            }
                        }
                        catch(MalformedURLException ex)
                        {
                            ex.printStackTrace();
                        }
                        catch(InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }
                    } //end of dealing with first item
                    int dirsProcessed = 0;

                    //add the rest of the dirs
                    while((line = d.readLine()) != null)
                    {
                        //code to skip the current work load
                        if(skipCurrent)
                        {
                            //add the totalnumber per pass - the amount process this pass to the work correction total
                            manager.addToWorkCorrection(passTotal - dirsProcessed);
                            break;
                        }

                        //if the line is not empty or starts with a #
                        if(!line.equalsIgnoreCase("") && !line.startsWith("#"))
                        {
                            line = line.trim();
                            line = makeItemsafe(line);
                            try
                            {
                                String method;
                                if(manager.getAuto() && !baseCaseObj.useContentAnalysisMode() && !baseCaseObj.isUseRegexInstead())
                                {
                                    method = "HEAD";
                                }
                                else
                                {
                                    method = "GET";
                                }

                                currentURL = new URL(firstPart + currentDir + line + "/");
                                //BaseCase baseCaseObj = new BaseCase(currentURL, failcode, true, failurl, baseResponce);
                                //if the base case is null then we need to switch to content anylsis mode

                                //System.out.println("Gen about to add to queue");
                                workQueue.put(new WorkUnit(currentURL, true, method, baseCaseObj, line));
                                //System.out.println("Gen finshed adding to queue");
                                if(Config.debug)
                                {
                                    System.out.println("DEBUG WokerGen: 2 adding dir to work list " + method + " " + currentURL.toString());
                                }
                            }
                            catch(MalformedURLException e)
                            {
                                //TODO deal with bad line
                                //e.printStackTrace();
                                //do nothing if it's malformed, I dont care about them!
                            }
                            catch(InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            //if there is a call to stop the work gen then stop!
                            if(stopMe)
                            {
                                return;
                            }
                            dirsProcessed++;
                        }
                    } //end of while
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }

            //generate the list of files
            if(manager.getDoFiles())
            {

                baseResponce = null;
                URL failurl = null;

                //loop for all the different file extentions
                for(int b = 0; b < extToCheck.size(); b++)
                {
                    //only test if we are surposed to
                    ExtToCheck extTemp = (ExtToCheck) extToCheck.elementAt(b);

                    if(extTemp.toCheck())
                    {

                        fileExtention = "";
                        if(extTemp.getName().equals(ExtToCheck.BLANK_EXT))
                        {
                            fileExtention = "";
                        }
                        else
                        {
                            fileExtention = "." + extTemp.getName();
                        }

                        try
                        {
                            manager.setStatus("Starting brute force for files in " + currentDir + " with extention " + fileExtention);

                            //get the base for this extention
                            baseCaseObj = GenBaseCase.genBaseCase(firstPart + currentDir, false, fileExtention);
                        }
                        catch(MalformedURLException e)
                        {
                            e.printStackTrace();
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }

                        //if the manager has sent the stop command then exit
                        if(stopMe)
                        {
                            return;
                        }

                        try
                        {
                            manager.setStatus("Brute forceing files in " + currentDir);
                            BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
                            //if(failcode != 200)
                            //{
                            int filesProcessed = 0;

                            while((line = d.readLine()) != null)
                            {
                                //code to skip the current work load
                                if(skipCurrent)
                                {
                                    manager.addToWorkCorrection(passTotal - filesProcessed);
                                    break;
                                }
                                //dont process is the line empty for starts with a #
                                if(!line.equalsIgnoreCase("") && !line.startsWith("#"))
                                {
                                    line = line.trim();
                                    line = makeItemsafe(line);
                                    try
                                    {
                                        String method;
                                        if(manager.getAuto() && !baseCaseObj.useContentAnalysisMode() && !baseCaseObj.isUseRegexInstead())
                                        {
                                            method = "HEAD";
                                        }
                                        else
                                        {
                                            method = "GET";
                                        }

                                        URL currentURL = new URL(firstPart + currentDir + line + fileExtention);
                                        //BaseCase baseCaseObj = new BaseCase(currentURL, true, failurl, baseResponce);
                                        workQueue.put(new WorkUnit(currentURL, false, method, baseCaseObj, line));
                                        if(Config.debug)
                                        {
                                            System.out.println("DEBUG WokerGen: adding file to work list " + method + " " + currentURL.toString());
                                        }
                                    }
                                    catch(MalformedURLException e)
                                    {
                                        //e.printStackTrace();
                                        //again do nothing as I dont care
                                    }
                                    catch(InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if(stopMe)
                                    {
                                        return;
                                    }
                                    filesProcessed++;
                                }
                            } //end of while
                        //}
                        }
                        catch(FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                } //end of file ext loop
            } //end of if files
            finished = started;

            counter++;
            try
            {
                Thread.sleep(200);
            }
            catch(InterruptedException ex)
            {
                ex.printStackTrace();
            }
        } //end of main while
    //System.out.println("Gen FINISHED!");
    //manager.youAreFinished();
    }

    private String makeItemsafe(String item)
    {
        //covert spaces
        item = item.replaceAll(" ", "%20");
        //remove "
        item = item.replaceAll("\"", "");
        //convert \ into /
        item = item.replaceAll("\\\\", "");


        if(item.length() > 2)
        {
            //remove / from the end
            if(item.endsWith("/"))
            {
                item = item.substring(1, item.length() - 1);
            }
            //remove / from the front
            if(item.startsWith("/"))
            {
                item = item.substring(2, item.length());
            }
        }
        else
        {
            //change a single / for DirBuster -> this stops errors and recursive loops
            if(item.startsWith("/"))
            {
                item = "DirBuster";
            }
        }
        return item;
    }

    /**
     * Method to stop the manager while it is working
     */
    public void stopMe()
    {
        stopMe = true;
    }

    public void skipCurrent()
    {
        skipCurrent = true;
    }
}