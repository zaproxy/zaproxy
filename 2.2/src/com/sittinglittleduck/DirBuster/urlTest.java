/*
 * urlTest.java
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
import java.net.URL;
import java.util.Date;

public class urlTest
{
    
    /** Creates a new instance of urlTest */
    public urlTest()
    {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
        //URL url = new URL("http://www.sittinglittleduck.com/testing1/wibble/getit/test.php?id=1&id2=qwerty#here");
        URL url = new URL("http://www.sittinglittleduck.com/testing1/test2/index.jsp");
        System.out.println("Path = " + url.getPath());
        System.out.println("toString = " + url.toString());
        System.out.println("file = " + url.getFile());
        System.out.println("host = " + url.getHost());
        System.out.println("port = " + url.getPort());
        System.out.println("last bit of file = " + url.getPath());
        System.out.println("Processing the url");
        
        String item = url.getPath();
        String fullItem = item;
        
        if(item.endsWith("/"))
        {
            item  = item.substring(0, item.length() -1);
            
        }
        System.out.println("item = " + item);
            int location = item.lastIndexOf("/");
            item = item.substring(location + 1);
            System.out.println("item = " + item);
        
        int index = 0;    
        while((index = fullItem.indexOf("/")) != -1)
        {
            String realitem = fullItem.substring(0, index);
            String rest = fullItem.substring(index + 1);
            System.out.println("real = " + realitem);
            System.out.println("rest = " + rest);
            fullItem = rest;
        }
        
        Date now = new Date();
                //if(now.)
        Date update =  new Date(0L);
        
        Long passed = now.getTime() - update.getTime();
        System.out.println("now: " + now.getTime());
        System.out.println("update: " + update.getTime());
        System.out.println("time passed: " + passed);
        
        
            
        //HTMLparse.processURL(url);
        }
        catch(Exception e)
        {
            
        }
        
        
        /*
        Date date = new Date(System.currentTimeMillis());
        System.out.println("date = "+ date);
        
        System.out.println("user.dir = " + System.getProperty("user.dir"));
        
        int test = 2^4;
        System.out.println("2^4 = " + test); 
        
        long timeInSecs = 1452;
        System.out.println(convertSecsToTime(timeInSecs));
        
        String fuzzURL = "\text.html?start=123&url={dir}&wibble=wobble";
        
        int startLoc = fuzzURL.indexOf("{dir}");
        
        System.out.println("start: " + fuzzURL.substring(0, startLoc));
        System.out.println("end: " + fuzzURL.substring(startLoc + 5, fuzzURL.length()));
         * 
         */
        
    }
    
    private static String convertSecsToTime(long secs)
    {
        //get the number of minuates
        if(secs < 60)
        {
            return "00:00:" + secs;
        }
        long mins = secs / 60;
        long secsleft = secs - (mins * 60);
        if(mins < 60)
        {
            return "00:" + mins + ":" + secsleft;
        }
        long hours = mins / 60;
        long minsleft = mins - (hours * 60);
        if(hours < 10)
        {
            return "0" + hours + ":" + minsleft + ":" + secsleft;
        }
        else
        {
            return hours + ":" + minsleft + ":" + secsleft;
        }
    }
    
    
}
