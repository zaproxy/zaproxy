/*
 * CatchExit.java 
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

package com.sittinglittleduck.DirBuster.headless;

import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.ReportWriter;

/**
 *
 * @author james
 */
public class CatchExit implements Runnable
{

    public void run()
    {
        Manager manager = Manager.getInstance();
        //String reportLocation = System.getProperty("user.dir") + File.separatorChar + "DirBuster-Report-" + manager.getHost() + "-" + manager.getPort() +".txt";
        String reportLocation = manager.getReportLocation();
        ReportWriter report = new ReportWriter(reportLocation);
        System.out.println("");
        System.out.println("Caught exit of DirBuster");
        System.out.println("Writing report");
        report.writeReportHeadless();
        System.out.println("Report saved to " + reportLocation);
        System.out.println("Enjoy the rest of your day");
        
        
    }

}
