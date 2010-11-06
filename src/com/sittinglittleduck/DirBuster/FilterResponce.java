/*
 * FilterResponce.java
 *
 * Created on 01 December 2005, 22:10
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
import java.util.regex.*;
import java.net.URL;


/**
 * Util class to normliaze http responces
 */
public class FilterResponce
{
    
    /**
     * Creates a new instance of FilterResponce
     */
    public FilterResponce()
    {
        
    }
    
    /**
     * Clean the responce of a work unit
     * @param toclean String to clean
     * @param work Unit of work the toclean string refferes to
     * @return Cleaned responce
     */
    public static String CleanResponce(String toclean, WorkUnit work)
    {
        return CleanResponce(toclean, work.getWork(), work.getItemToCheck());
    }
    
    /**
     * Clean the responce of a work based on a URL
     * @param toclean String to clean
     * @param url URL that generated the reponce that is to be cleaned
     * @return String of cleaned responce
     */
    public static String CleanResponce(String toclean, URL url, String itemChecked)
    {
        
        if(toclean != null)
        {
            if(!toclean.equals(""))
            {
                //remove the firstline from the responce
                //firstline = toclean.
                
                //remove date header
                Pattern p = Pattern.compile("Date: [\\w\\d, :;=/]+\\W", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(toclean);
                if(m.find())
                {
                    //System.out.println("Found Date value = '" + m.group(0) + "'");
                    toclean = toclean.replaceAll(m.group(0), "DATE LINE REMOVED");
                }
                
                //remove P3P header
                p = Pattern.compile("P3P: [\\w\\d, :;=/]+\\W", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean);
                if(m.find())
                {
                    //System.out.println("Found Date value = '" + m.group(0) + "'");
                    toclean = toclean.replaceAll(m.group(0), "PSP LINE REMOVED");
                }
                
                //Remove cookie header
                p = Pattern.compile("Set-Cookie:.*?\r\n", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean);
                if(m.find())
                {
                    //System.out.println("Found Date value = '" + m.group(0) + "'");
                    toclean = toclean.replaceAll(m.group(0), "SET-COOKIE LINE REMOVED\r\n");
                }
                
                //Remove Expires
                p = Pattern.compile("Expires: [\\w\\d, :-;=/]+\\W", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean);
                if(m.find())
                {
                    //System.out.println("Found Date value = '" + m.group(0) + "'");
                    toclean = toclean.replaceAll(m.group(0), "EXPIRES LINE REMOVED");
                }
                
                //Remove Etag
                p = Pattern.compile("ETag: [\\w\\d\"\', :]+\\W", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean);
                if(m.find())
                {
                    //System.out.println("Found Date value = '" + m.group(0) + "'");
                    toclean = toclean.replaceAll(m.group(0), "");
                }
                
                //Remove a possible date
                p = Pattern.compile("\\w\\w\\w,? \\d\\d? \\w\\w\\w \\d\\d\\d\\d \\d?\\d?:?\\d?\\d?:?\\d?\\d? \\w?\\w?\\w?", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("DATE REMOVED");
                
                //remove the host
                p = Pattern.compile(Pattern.quote(url.getHost()), Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("HOST REMOVED");
                
                //remove the entire URL
                p = Pattern.compile(Pattern.quote(url.toString()), Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("ADDRESSED REMOVED");
                
                //remove the file location
                p = Pattern.compile(Pattern.quote(url.getFile()), Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("FILE REMOVED");
                
                p = Pattern.compile(Pattern.quote(url.getPath()), Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("PATH REMOVED");
                
                //remove any ip address
                p = Pattern.compile("\\d\\d\\d?\\.\\d\\d\\d?\\.\\d\\d\\d?\\.\\d\\d\\d?", Pattern.CASE_INSENSITIVE);
                m = p.matcher(toclean); // get a matcher object
                toclean = m.replaceAll("IP ADDRESSED REMOVED");
                /*
                if(itemChecked != null)
                {
                    //remove the item that is being checked for
                    p = Pattern.compile(itemChecked, Pattern.CASE_INSENSITIVE);
                    m = p.matcher(toclean); // get a matcher object
                    toclean = m.replaceAll("ITEM TOCHECK REMOVED");
                }
                 */
            }
            
        }
        return toclean;
        
    }
    
    public static String removeItemCheckedFor(String toclean, String itemToCheckFor)
    {
        /*
        if (itemToCheckFor != null && toclean != null)
        {
            //remove the item that is being checked for
            Pattern p = Pattern.compile(itemToCheckFor, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(toclean); // get a matcher object
            toclean = m.replaceAll("ITEM TOCHECK REMOVED");
        }
         */
        
        return toclean;
    }
    
    private static String RegexSafe(String toMakeSafe)
    {
        //toMakeSafe.replaceAll("\\", "\\\\");
        toMakeSafe.replaceAll("\\.", "\\\\.");
        toMakeSafe.replaceAll("\\*", "\\\\*");
        return toMakeSafe;
    }
    
}
