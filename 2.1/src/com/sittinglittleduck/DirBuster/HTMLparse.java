/*
 * HTMLparse.java
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
 *///TODO convert this over to a thread, so it doe snot tie up the workers :)
package com.sittinglittleduck.DirBuster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

/**
 * This class is to paser the returned html pages and extract other dirs and files from them
 * @author james
 */
public class HTMLparse extends Thread
{

    private String sourceAsString = null;
    private WorkUnit work = null;
    private Manager manager;
    boolean working;
    private boolean continueWorking = true;

    /** Creates a new instance of HTMLparse */
    public HTMLparse()
    {
        super("DirBuster-HTMLparse");
        manager = Manager.getInstance();
    }

    public void stopWorking()
    {
        continueWorking = false;
        this.interrupt();

    }

    public void run()
    {
        while(continueWorking)
        {
            working = false;
            sourceAsString = "";
            work = null;
            HTMLparseWorkUnit parseUnit = null;
            try
            {
                parseUnit = manager.parseQueue.take();
            }
            catch(InterruptedException ex)
            {
                //ex.printStackTrace();
                return;
            }
            working = true;
            sourceAsString = parseUnit.getHtmlToParse();
            work = parseUnit.getWorkUnit();



            if(sourceAsString != null || work != null)
            {
                if(!sourceAsString.equals(""))
                {




                    if(Config.debug)
                    {

                        System.out.println("DEBUG HTMLParser: Parsing text from " + work.getWork().toString());
                        System.out.println("DEBUG HTMLParser: text - " + sourceAsString);
                    }

                    Vector links = new Vector(50, 10);
                    Vector imageLinks = new Vector(50, 10);
                    Vector foundItems = new Vector(20, 10);

                    manager = Manager.getInstance();

                    //create the source
                    Source source = new Source(sourceAsString);

                    Vector elementsToParse = manager.getElementsToParse();

                    //loop trought all the things we wish to parse
                    for(int z = 0; z < elementsToParse.size(); z++)
                    {
                        HTMLelementToParse elementToParse = (HTMLelementToParse) elementsToParse.elementAt(z);

                        for(Iterator i = source.getAllElements(elementToParse.getTag()).iterator(); i.hasNext();)
                        {
                            Element element = (Element) i.next();
                            Attributes attributes = element.getAttributes();
                            Attribute attr = attributes.get(elementToParse.getAttr());
                            //System.out.println(href.getValue());
                            try
                            {
                                if(attr != null)
                                {
                                    //creates a full qulaifed domian name, based on the page we have just tested
                                    URL tempURL = new URL(work.getWork(), attr.getValue());

                                    String urlString = tempURL.getPath();
                                    //check it is not already there and the link is from the same host
                                    if(!links.contains(urlString) && tempURL.getHost().equalsIgnoreCase(work.getWork().getHost()))
                                    {
                                        //add to vector to remove duplicates
                                        //links.addElement(urlString);
                                        Vector found = processURL(tempURL);

                                        if(found != null)
                                        {
                                            for(int a = 0; a < found.size(); a++)
                                            {
                                                String item = (String) found.elementAt(a);
                                                if(!foundItems.contains(item))
                                                {
                                                    foundItems.addElement(item);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            catch(MalformedURLException e)
                            {
                                //System.out.println("Man thats a bad url!");
                            }
                        }

                        try
                        {

                            Thread.sleep(100);
                        }
                        catch(InterruptedException ex)
                        {
                            return;
                        //ex.printStackTrace();
                        }
                    }//end of for loop for elements


                    //process all the found items
                    for(int a = 0; a < foundItems.size(); a++)
                    {
                        String founditem = (String) foundItems.elementAt(a);
                        //System.out.println((String) foundItems.elementAt(a));

                        boolean process = true;


                        for(int b = 0; b < manager.extsToMiss.size(); b++)
                        {
                            if(founditem.endsWith("." + (String) manager.extsToMiss.elementAt(b)))
                            {
                                process = false;
                                break;
                            }
                        }

                        //if it is ok to process the link
                        if(process)
                        {

                            //check if the found item has already been procced
                            //System.out.println("Testing to see if found item (" + founditem + ") has already been done");
                            if(!manager.hasLinkBeenDone(founditem))
                            {
                                //System.out.println(founditem + " has not already been done");
                                //get base case for item
                                BaseCase baseCase = findBaseCasePoint(founditem);
                                if(baseCase != null)
                                {
                                    String method = "";
                                    //create work unit for item
                                    if(manager.getAuto() && !baseCase.useContentAnalysisMode() && !baseCase.isUseRegexInstead())
                                    {
                                        method = "HEAD";
                                    }
                                    else
                                    {
                                        method = "GET";
                                    }

                                    try
                                    {
                                        //create work unit, so item can be added to the queue
                                    	// ZAP: Added port - otherwise will fail on non standard ports
                                        WorkUnit workUnit = new WorkUnit(
                                                new URL(work.getWork().getProtocol(), work.getWork().getHost(), work.getWork().getPort(), founditem),
                                                founditem.endsWith("/"), method, baseCase, null);

                                        //add item to the work queue to tested
                                        if(manager.addParsedLink(founditem))
                                        {
                                            //increment the counter for the amount of work done
                                            manager.addParsedLinksProcessed();
                                            manager.workQueue.put(workUnit);
                                        //System.out.println("added " + workUnit.getWork().toString() + " to the work queue");
                                        }
                                    }
                                    catch(MalformedURLException ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                    catch(InterruptedException ex)
                                    {
                                        return;
                                    //ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }//end of wile
    }

    /**
     * Splits up the URL found
     * @param url url to be processed
     */
    private Vector processURL(URL url)
    {
        try
        {
            Vector foundItems = new Vector(10, 10);

            String toProcess = url.getPath();
            boolean noFile = url.getPath().endsWith("/");
            String[] split = toProcess.split("/");

            String found = "";

            for(int a = 0; a < split.length; a++)
            {
                //if is the last element and there is a file
                if(a == (split.length - 1) && !noFile)
                {
                    found = found + split[a];
                }
                else
                {
                    found = found + split[a] + "/";
                }
                //System.out.println("Item = " + found);


                foundItems.addElement(found);
            }
            Thread.sleep(10);

            return foundItems;
        }
        catch(InterruptedException ex)
        {
            return null;

        }

    }

    private BaseCase findBaseCasePoint(String item)
    {

        try
        {
            boolean isDir = false;
            String fileExtention = null;
            if(item.length() == 1)
            {
                //System.out.println("found a / in findBaseCasePoint");
                return GenBaseCase.genBaseCase(manager.getFirstPartOfURL() + "/", true, null);
            }
            String[] array = item.split("/");

            String baseItem = "";
            for(int a = 0; a < array.length - 1; a++)
            {
                baseItem = baseItem + array[a] + "/";
            }

            if(item.endsWith("/"))
            {
                isDir = true;
                fileExtention = null;
            }
            else
            {
                String file = array[array.length - 1];
                int loc = file.indexOf(".");
                if(loc != -1)
                {
                    fileExtention = file.substring(loc + 1);
                }
                else
                {
                    fileExtention = "";
                }
            }
            //System.out.println("baseItem = " + baseItem);
            //System.out.println("file extention = " + fileExtention);


            Thread.sleep(100);

            return GenBaseCase.genBaseCase(manager.getFirstPartOfURL() + baseItem, isDir, fileExtention);
        }
        catch(MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        catch(InterruptedException ex)
        {
            //ex.printStackTrace();
            return null;
        }

        return null;
    }

    public boolean isWorking()
    {
        return working;
    }
}
