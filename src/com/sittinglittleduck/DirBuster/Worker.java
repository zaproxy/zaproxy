/*
 * Worker.java
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
package com.sittinglittleduck.DirBuster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 * This class process workunit and determines if the link has been found or not
 */
public class Worker implements Runnable
{

    private BlockingQueue<WorkUnit> queue;
    private URL url;
    private WorkUnit work;
    private Manager manager;
    private HttpClient httpclient;
    private boolean pleaseWait = false;
    private int threadId;
    private boolean working;
    private boolean stop = false;

    /**
     * Creates a new instance of Worker
     * @param threadId Unique thread id for the worker
     * @param manager The manager class the worker thread reports to
     */
    public Worker(int threadId)
    {
        //get the manager instance
        manager = Manager.getInstance();

        //get the work queue from, the manager
        queue = manager.workQueue;

        //get the httpclient
        httpclient = manager.getHttpclient();

        //set the thread id
        this.threadId = threadId;

    }

    /**
     * Run method of the thread
     *
     */
    public void run()
    {

        queue = manager.workQueue;
        while(manager.hasWorkLeft())
        {

            working = false;
            //code to make the worker pause, if the pause button has been presed

            //if the stop signal has been given stop the thread
            if(stop)
            {
                return;
            }

            //this pasuses the thread
            synchronized(this)
            {
                while(pleaseWait)
                {
                    try
                    {
                        wait();
                    }
                    catch(InterruptedException e)
                    {
                        return;
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }


            GetMethod httpget = null;
            HeadMethod httphead = null;

            try
            {

                work = (WorkUnit) queue.take();
                working = true;
                url = work.getWork();
                int code = 0;

                String responce = "";
                String rawResponce = "";

                //if the work is a head request
                if(work.getMethod().equalsIgnoreCase("HEAD"))
                {
                    if(Config.debug)
                    {
                        System.out.println("DEBUG Worker[" + threadId + "]: HEAD " + url.toString());
                    }

                    httphead = new HeadMethod(url.toString());

                    //set the custom HTTP headers
                    Vector HTTPheaders = manager.getHTTPHeaders();
                    for(int a = 0; a < HTTPheaders.size(); a++)
                    {
                        HTTPHeader httpHeader = (HTTPHeader) HTTPheaders.elementAt(a);
                        /*
                         * Host header has to be set in a different way!
                         */
                        if(httpHeader.getHeader().startsWith("Host"))
                        {
                            httphead.getParams().setVirtualHost(httpHeader.getValue());
                        }
                        else
                        {
                            httphead.setRequestHeader(httpHeader.getHeader(), httpHeader.getValue());
                        }

                    }
                    httphead.setFollowRedirects(Config.followRedirects);

                    /*
                     * this code is used to limit the number of request/sec
                     */
                    if(manager.isLimitRequests())
                    {
                        while(manager.getTotalDone() / ((System.currentTimeMillis() - manager.getTimestarted()) / 1000.0) > manager.getLimitRequestsTo())
                        {
                            Thread.sleep(100);
                        }
                    }
                    /*
                     * Send the head request
                     */
                    code = httpclient.executeMethod(httphead);
                    if(Config.debug)
                    {
                        System.out.println("DEBUG Worker[" + threadId + "]: " + code + " " + url.toString());
                    }
                    httphead.releaseConnection();

                }
                //if we are doing a get request
                else if(work.getMethod().equalsIgnoreCase("GET"))
                {
                    //make the request;
                    if(Config.debug)
                    {
                        System.out.println("DEBUG Worker[" + threadId + "]: GET " + url.toString());
                    }
                    httpget = new GetMethod(url.toString());

                    //set the custom HTTP headers
                    Vector HTTPheaders = manager.getHTTPHeaders();
                    for(int a = 0; a < HTTPheaders.size(); a++)
                    {

                        HTTPHeader httpHeader = (HTTPHeader) HTTPheaders.elementAt(a);
                        /*
                         * Host header has to be set in a different way!
                         */
                        if(httpHeader.getHeader().startsWith("Host"))
                        {
                            httpget.getParams().setVirtualHost(httpHeader.getValue());
                        }
                        else
                        {
                            httpget.setRequestHeader(httpHeader.getHeader(), httpHeader.getValue());
                        }
                    }
                    httpget.setFollowRedirects(Config.followRedirects);

                    /*
                     * this code is used to limit the number of request/sec
                     */
                    if(manager.isLimitRequests())
                    {
                        while(manager.getTotalDone() / ((System.currentTimeMillis() - manager.getTimestarted()) / 1000.0) > manager.getLimitRequestsTo())
                        {
                            Thread.sleep(100);
                        }
                    }

                    code = httpclient.executeMethod(httpget);

                    if(Config.debug)
                    {
                        System.out.println("DEBUG Worker[" + threadId + "]: " + code + " " + url.toString());
                    }

                    //set up the input stream
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpget.getResponseBodyAsStream()));

                    //save the headers into a string, used in viewing raw responce
                    String rawHeader;
                    rawHeader = httpget.getStatusLine() + "\r\n";
                    Header[] headers = httpget.getResponseHeaders();

                    StringBuffer buf = new StringBuffer();
                    for(int a = 0; a < headers.length; a++)
                    {
                        buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                    }

                    rawHeader = rawHeader + buf.toString();

                    buf = new StringBuffer();
                    //read in the responce body
                    String line;
                    while((line = input.readLine()) != null)
                    {
                        buf.append("\r\n" + line);
                    }
                    responce = buf.toString();
                    input.close();

                    rawResponce = rawHeader + responce;
                    //clean the responce

                    //parse the html of what we have found

                    if(Config.parseHTML && !work.getBaseCaseObj().isUseRegexInstead())
                    {
                        Header contentType = httpget.getResponseHeader("Content-Type");

                        if(contentType != null)
                        {
                            if(contentType.getValue().startsWith("text"))
                            {
                                manager.addHTMLToParseQueue(new HTMLparseWorkUnit(responce, work));
                            }
                        }
                    }

                    responce = FilterResponce.CleanResponce(responce, work);

                    Thread.sleep(10);
                    httpget.releaseConnection();
                }
                else
                {
                    //There is no need to deal with requests other than HEAD or GET
                }



                //if we need to check the against the base case
                if(work.getMethod().equalsIgnoreCase("GET") && work.getBaseCaseObj().useContentAnalysisMode())
                {
                    if(code == 200)
                    {
                        if(Config.debug)
                        {
                            System.out.println("DEBUG Worker[" + threadId + "]: Base Case Check " + url.toString());
                        }


                        //TODO move this option to the Adv options
                        //if the responce does not match the base case
                        Pattern regexFindFile = Pattern.compile(".*file not found.*", Pattern.CASE_INSENSITIVE);

                        Matcher m = regexFindFile.matcher(responce);

                        //need to clean the base case of the item we are looking for
                        String basecase = FilterResponce.removeItemCheckedFor(work.getBaseCaseObj().getBaseCase(), work.getItemToCheck());

                        if(m.find())
                        {
                            //do nothing as we have a 404
                        }
                        else if(!responce.equalsIgnoreCase(basecase))
                        {
                            if(work.isDir())
                            {
                                if(Config.debug)
                                {
                                    System.out.println("DEBUG Worker[" + threadId + "]: Found Dir (base case)" + url.toString());
                                }
                                //we found a dir
                                manager.foundDir(url, code, responce, basecase, rawResponce, work.getBaseCaseObj());
                            }
                            else
                            {
                                //found a file
                                if(Config.debug)
                                {
                                    System.out.println("DEBUG Worker[" + threadId + "]: Found File (base case)" + url.toString());
                                }
                                manager.foundFile(url, code, responce, work.getBaseCaseObj().getBaseCase(), rawResponce, work.getBaseCaseObj());
                            }
                        }
                    }
                    else if(code == 404 || code == 400)
                    {
                        //again do nothing as it is not there
                    }
                    else
                    {
                        if(work.isDir())
                        {
                            if(Config.debug)
                            {
                                System.out.println("DEBUG Worker[" + threadId + "]: Found Dir (base case)" + url.toString());
                            }
                            //we found a dir
                            manager.foundDir(url, code, responce, work.getBaseCaseObj().getBaseCase(), rawResponce, work.getBaseCaseObj());
                        }
                        else
                        {
                            //found a file
                            if(Config.debug)
                            {
                                System.out.println("DEBUG Worker[" + threadId + "]: Found File (base case)" + url.toString());
                            }
                            manager.foundFile(url, code, responce, work.getBaseCaseObj().getBaseCase(), rawResponce, work.getBaseCaseObj());
                        }
                    //manager.foundError(url, "Base Case Mode Error - Responce code came back as " + code + " it should have been 200");
                    //manager.workDone();
                    }
                }
                /*
                 * use the custom regex check instead
                 */
                else if(work.getBaseCaseObj().isUseRegexInstead())
                {
                    Pattern regexFindFile = Pattern.compile(work.getBaseCaseObj().getRegex());

                    Matcher m = regexFindFile.matcher(rawResponce);
                    /*
                    System.out.println("======Trying to find======");
                    System.out.println(work.getBaseCaseObj().getRegex());
                    System.out.println("======In======");
                    System.out.println(responce);
                    System.out.println("======/In======");
                     */
                    if(m.find())
                    {
                        //do nothing as we have a 404
                        if(Config.debug)
                        {

                            System.out.println("DEBUG Worker[" + threadId + "]: Regex matched so it's a 404, " + url.toString());
                        }

                    }
                    else
                    {
                        if(Config.parseHTML)
                        {
                            Header contentType = httpget.getResponseHeader("Content-Type");

                            if(contentType != null)
                            {
                                if(contentType.getValue().startsWith("text"))
                                {
                                    manager.addHTMLToParseQueue(new HTMLparseWorkUnit(rawResponce, work));
                                }
                            }
                        }
                        if(work.isDir())
                        {
                            if(Config.debug)
                            {
                                System.out.println("DEBUG Worker[" + threadId + "]: Found Dir (regex) " + url.toString());
                            }
                            //we found a dir
                            manager.foundDir(url, code, responce, work.getBaseCaseObj().getBaseCase(), rawResponce, work.getBaseCaseObj());
                        }
                        else
                        {
                            //found a file
                            if(Config.debug)
                            {
                                System.out.println("DEBUG Worker[" + threadId + "]: Found File (regex) " + url.toString());
                            }
                            manager.foundFile(url, code, responce, work.getBaseCaseObj().getBaseCase(), rawResponce, work.getBaseCaseObj());
                        }
                    //manager.foundError(url, "Base Case Mode Error - Responce code came back as " + code + " it should have been 200");
                    //manager.workDone();
                    }


                }
                //just check the responce code
                else
                {
                    //if is not the fail code, a 404 or a 400 then we have a possible
                    if(code != work.getBaseCaseObj().getFailCode() && code != 404 && code != 0 && code != 400)
                    {
                        if(work.getMethod().equalsIgnoreCase("HEAD"))
                        {
                            if(Config.debug)
                            {
                                System.out.println("DEBUG Worker[" + threadId + "]: Getting responce via GET " + url.toString());
                            }
                            rawResponce = "";

                            httpget = new GetMethod(url.toString());
                            Vector HTTPheaders = manager.getHTTPHeaders();
                            for(int a = 0; a < HTTPheaders.size(); a++)
                            {
                                HTTPHeader httpHeader = (HTTPHeader) HTTPheaders.elementAt(a);
                                httpget.setRequestHeader(httpHeader.getHeader(), httpHeader.getValue());
                            }
                            httpget.setFollowRedirects(Config.followRedirects);

                            /*
                             * this code is used to limit the number of request/sec
                             */
                            if(manager.isLimitRequests())
                            {
                                while(manager.getTotalDone() / ((System.currentTimeMillis() - manager.getTimestarted()) / 1000.0) > manager.getLimitRequestsTo())
                                {
                                    Thread.sleep(100);
                                }
                            }

                            int newCode = httpclient.executeMethod(httpget);

                            //in some cases the second get can return a different result, than the first head request!
                            if(newCode != code)
                            {
                                manager.foundError(url, "Return code for first HEAD, is different to the second GET: " + code + " - " + newCode);
                            }


                            rawResponce = "";
                            //build a string version of the headers
                            rawResponce = httpget.getStatusLine() + "\r\n";
                            Header[] headers = httpget.getResponseHeaders();

                            StringBuffer buf = new StringBuffer();
                            for(int a = 0; a < headers.length; a++)
                            {
                                buf.append(headers[a].getName() + ": " + headers[a].getValue() + "\r\n");
                            }

                            buf.append("\r\n");

                            rawResponce = rawResponce + buf.toString();

                            if(httpget.getResponseContentLength() > 0)
                            {

                                //get the http body
                                BufferedReader input = new BufferedReader(new InputStreamReader(httpget.getResponseBodyAsStream()));

                                String line;

                                String tempResponce = "";

                                buf = new StringBuffer();
                                while((line = input.readLine()) != null)
                                {
                                    buf.append("\r\n" + line);
                                }
                                tempResponce = buf.toString();
                                input.close();


                                rawResponce = rawResponce + tempResponce;


                                Header contentType = httpget.getResponseHeader("Content-Type");

                                if(Config.parseHTML)
                                {
                                    contentType = httpget.getResponseHeader("Content-Type");

                                    if(contentType != null)
                                    {
                                        if(contentType.getValue().startsWith("text"))
                                        {
                                            manager.addHTMLToParseQueue(new HTMLparseWorkUnit(tempResponce, work));
                                        }
                                    }
                                }
                            }

                            httpget.releaseConnection();
                        }


                        if(work.isDir())
                        {
                            manager.foundDir(url, code, rawResponce, work.getBaseCaseObj());
                        }
                        else
                        {
                            manager.foundFile(url, code, rawResponce, work.getBaseCaseObj());
                        }
                    }
                }

                manager.workDone();
                Thread.sleep(20);

            }
            catch(NoHttpResponseException e)
            {
                manager.foundError(url, "NoHttpResponseException " + e.getMessage());
                manager.workDone();
            }
            catch(ConnectTimeoutException e)
            {
                manager.foundError(url, "ConnectTimeoutException " + e.getMessage());
                manager.workDone();
            }
            catch(URIException e)
            {
                manager.foundError(url, "URIException " + e.getMessage());
                manager.workDone();
            }
            catch(IOException e)
            {

                manager.foundError(url, "IOException " + e.getMessage());
                manager.workDone();
            }
            catch(InterruptedException e)
            {
                //manager.foundError(url, "InterruptedException " + e.getMessage());
                manager.workDone();
                return;
            }
            catch(IllegalArgumentException e)
            {

                e.printStackTrace();
                manager.foundError(url, "IllegalArgumentException " + e.getMessage());
                manager.workDone();
            }
            finally
            {
                if(httpget != null)
                {
                    httpget.releaseConnection();
                }

                if(httphead != null)
                {
                    httphead.releaseConnection();
                }
            }
        }

    }

    /**
     * Method to call to pause the thread
     */
    public void pause()
    {
        pleaseWait = true;
    }

    /**
     * Method to call to unpause the thread
     */
    public void unPause()
    {
        pleaseWait = false;
    }

    /**
     * Return a boolean based on if the thread is working
     * @return boolean value about if the thread is working
     */
    public boolean isWorking()
    {
        return working;
    }

    /**
     * Method to call to stop the thread
     */
    public void stopThread()
    {
        this.stop = true;
    }
}
