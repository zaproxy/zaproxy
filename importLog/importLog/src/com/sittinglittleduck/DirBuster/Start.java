/*
 * Start.java
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

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sittinglittleduck.DirBuster.gui.StartGUI;
import com.sittinglittleduck.DirBuster.headless.CatchExit;

/**
 *
 * @author james
 */
public class Start
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

        URL target = null;

        /*
         * Get the manager
         */
        Manager manager = Manager.getInstance();

        /*
         * if no arguments are passed just start the GUI
         */
        if(args.length == 0)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {

                public void run()
                {
                    new StartGUI().setVisible(true);


                }
            });
        }
        /*
         * if command line arguments have been passed
         */
        else
        {
            /*
             * Default Values
             */
            boolean headless = false;
            boolean verbose = false;
            String fileToRead = System.getProperty("user.dir") + File.separatorChar + "directory-list-2.3-small.txt";
            String reportLocation = null;
            int threads = 10;
            String startPoint = "/";
            String exts = "php";
            boolean auto = true;
            String protocol = "";
            String host = "";
            int port = 80;
            boolean recursive = true;




            Getopt opt = new Getopt("DirBuster", args, "s:l:e:t:u:g:r:vfhHPR");
            int c, nErr;
            String arg;
            while((c = opt.getopt()) != -1)
            {
                switch(c)
                {
                    /*
                     * Display the help
                     */
                    case 'h':
                        printUsage();
                        System.exit(0);

                        break;
                    /*
                     * URL of the target
                     */
                    case 'u':
                        arg = opt.getOptarg();
                        try
                        {
                            URL targetURL = new URL(arg);
                            manager.setTargetURL(targetURL);
                            protocol = targetURL.getProtocol();
                            host = targetURL.getHost();
                            port = targetURL.getPort();
                            if(port == -1)
                            {
                                port = targetURL.getDefaultPort();
                            }
                        }
                        catch(MalformedURLException ex)
                        {
                            System.err.println("ERROR: Your target is not a valid URL");
                            System.exit(1);
                        }
                        break;


                    /*
                     * verbose/debug mode
                     */
                    case 'v': //see if the verbose option is there
                        //verbose = true;
                        Config.debug = true;
                        break;


                    /*
                     * Which list to use
                     */
                    case 'l':

                        fileToRead = opt.getOptarg();
                        if(fileToRead != null && !fileToRead.equalsIgnoreCase(""))
                        {
                            try
                            {
                                FileInputStream test = new FileInputStream(new File(fileToRead));
                                manager.setFileLocation(fileToRead);
                            }
                            catch(FileNotFoundException e)
                            {
                                System.err.println("ERROR: Sorry can't open the file with the directory list in.");
                                System.exit(1);
                            }
                        }
                        break;


                    /*
                     * headless mode, run with out the gui
                     */
                    case 'H':
                        headless = true;

                        break;

                    /*
                     * number of threads to use
                     */
                    case 't':
                        arg = opt.getOptarg();

                        try
                        {
                            threads = Integer.parseInt(arg);
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("ERROR: number of threads is a not a number");
                            System.exit(1);
                        }

                        break;

                    /*
                     * Start point of scan
                     */
                    case 's':
                        arg = opt.getOptarg();

                        startPoint = arg;

                        manager.setPointToStartFrom(startPoint);

                        break;

                    /*
                     * Use GET requests only
                     */
                    case 'g':
                        auto = false;

                        break;

                    /*
                     * File extentions to test for
                     */
                    case 'e':
                        arg = opt.getOptarg();

                        exts = arg;

                        manager.setFileExtentions(exts);

                        break;
                    /*
                     * location to write report to
                     */
                    case 'r':
                        arg = opt.getOptarg();

                        reportLocation = arg;
                        manager.setReportLocation(reportLocation);

                        break;
                    /*
                     * flag to not parse the html
                     */
                    case 'P':

                        Config.parseHTML = false;

                        break;
                    /*
                     * flag to not be recursive
                     */
                    case 'R':

                        recursive = false;

                        break;

                    default:

                }//end of switch
            }//end of while



            /*
             * Set Headless mode if we have to
             */
            if(headless)
            {

                if(host.equals(""))
                {
                    System.err.println("ERROR: no target url set, please set with -u <url>");
                    System.exit(1);
                }

                manager.setHeadLessMode(true);
                
                
                Vector extsVector = new Vector(10, 10);

                /*
                 * split up the exts
                 */
                StringTokenizer st = new StringTokenizer(exts, ",", false);
                while(st.hasMoreTokens())
                {

                    //System.out.println("ext = " + st.nextToken().trim());
                    extsVector.addElement(new ExtToCheck(st.nextToken().trim(), true));
                }


                /*
                 * capture the control C, so we can write the report no matter what
                 */
                Runtime.getRuntime().addShutdownHook(new Thread(new CatchExit()));


                System.out.println("Starting OWASP DirBuster " + Config.version + " in headless mode");

                /*
                 * start dibuster
                 * 
                 * public void setupManager(String startPoint,
                String inputFile,
                String protcol,
                String host,
                int port,
                String extention,
                StartGUI gui,
                int ThreadNumber,
                boolean doDirs,
                boolean doFiles,
                boolean recursive,
                boolean blankExt,
                Vector extToUse)
                 */


                manager.setupManager(startPoint, fileToRead, protocol, host, port, exts, null, threads, true, true, recursive, false, extsVector);

                /*
                 * if the report location is the default
                 */
                if(reportLocation == null)
                {
                    reportLocation = System.getProperty("user.dir") + File.separatorChar + "DirBuster-Report-" + manager.getHost() + "-" + manager.getPort() + ".txt";
                    manager.setReportLocation(reportLocation);
                }

                /*
                 * does the file we ar about to write to already exist?
                 */
                try
                {
                    FileInputStream test = new FileInputStream(new File(reportLocation));
                    System.err.println("ERROR: Unable to write to report location, file already exists");
                    System.exit(1);
                }
                catch(FileNotFoundException e)
                {
                    /*
                     * do nothing as the file is not already there.
                     */
                }


                manager.setAuto(auto);

                /*
                 * start the scan
                 */
                manager.start();



            }
            else
            {
                java.awt.EventQueue.invokeLater(new Runnable()
                {

                    public void run()
                    {
                        Manager manager = Manager.getInstance();
                        StartGUI gui = new StartGUI();

                        /*
                         * set the target
                         */
                        if(manager.getTargetURL() != null)
                        {
                            gui.jPanelSetup.jTextFieldTarget.setText(manager.getTargetURL().toString());
                        }

                        /*
                         * set the file location
                         */
                        if(manager.getFileLocation() != null)
                        {
                            gui.jPanelSetup.jTextFieldFile.setText(manager.getFileLocation());
                        }

                        /*
                         * set the report location
                         */
                        if(manager.getReportLocation() != null)
                        {
                            gui.jPanelReport.jTextFieldReportFile.setText(manager.getReportLocation());
                        }


                        /*
                         * set the file extention
                         */
                        if(manager.getFileExtentions() != null)
                        {
                            gui.jPanelSetup.jTextFieldFileExtention.setText(manager.getFileExtentions());
                        }

                        /*
                         * point to start the scan from
                         */
                        if(manager.getPointToStartFrom() != null)
                        {
                            gui.jPanelSetup.jTextFieldDirToStart.setText(manager.getPointToStartFrom());
                        }

                        gui.setVisible(true);



                    }
                });
            }



        }
    }

    public static void printUsage()
    {
        System.out.println("DirBuster - " + Config.version);
        System.out.println("Usage: java -jar DirBuster-" + Config.version + " -u <URL http://example.com/> [Options]");
        System.out.println("");
        System.out.println("\tOptions:");
        System.out.println("\t -h : Display this help message");
        System.out.println("\t -H : Start DirBuster in headless mode (no gui), report will be auto saved on exit");
        System.out.println("\t -l <Word list to use> : The Word list to use for the list based brute force. Default: " + System.getProperty("user.dir") + File.separatorChar + "directory-list-2.3-small.txt");
        System.out.println("\t -g : Only use GET requests. Default Not Set");
        System.out.println("\t -e <File Extention list> : File Extention list eg asp,aspx. Default: php");
        System.out.println("\t -t <Number of Threads> : Number of connection threads to use. Default: 10");
        System.out.println("\t -s <Start point> : Start point of the scan. Default: /");
        System.out.println("\t -v : Verbose output, Default: Not set");
        System.out.println("\t -P : Don't Parse html, Default: Not Set");
        System.out.println("\t -R : Don't be recursive, Default: Not Set");
        System.out.println("\t -r <location> : File to save report to. Default: " + System.getProperty("user.dir") + File.separatorChar + "DirBuster-Report-[hostname]-[port].txt");
        System.out.println("");
        System.out.println("Examples:");
        System.out.println("");
        System.out.println("Run DirBuster in headless mode");
        System.out.println("java -jar DirBuster-" + Config.version + ".jar -H -u https://www.target.com/");
        System.out.println("");
        System.out.println("Start GUI with target file prepopulated");
        System.out.println("java -jar DirBuster-" + Config.version + ".jar -u https://www.target.com/");


    }
}
