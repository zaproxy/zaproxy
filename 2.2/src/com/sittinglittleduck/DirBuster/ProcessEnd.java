/*
 * ProcessEnd.java
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

import java.util.TimerTask;
import java.util.Vector;

public class ProcessEnd extends TimerTask
{

    Manager manager;

    /** Creates a new instance of ProcessChecker */
    public interface ProcessUpdate
    {

        void isAlive();
    }

    public ProcessEnd()
    {
        this.manager = Manager.getInstance();

    }

    public void run()
    {
        if (manager.dirQueue.isEmpty() && manager.workQueue.isEmpty() && manager.parseQueue.isEmpty() && !manager.isFailCaseDialogVisable())
        {
            //test to see if all the workers are done
            boolean allWorkersFinished = true;
            Vector workers = manager.getWorkers();
            Vector parsers = manager.getParseWorkers();

            for (int a = 0; a < workers.size(); a++)
            {
                if (((Worker) workers.elementAt(a)).isWorking())
                {
                    //there is a worker still working so break
                    allWorkersFinished = false;
                    break;
                }
            }

            for (int a = 0; a < parsers.size(); a++)
            {
                if (((HTMLparse) parsers.elementAt(a)).isWorking())
                {
                    allWorkersFinished = false;
                    break;
                }
            }


            if (allWorkersFinished)
            {
                //all the workers are finished and there is nothing 
                manager.setStatus("Finished");
                manager.youAreFinished();
            }

        }
        else
        {
            //if we are fuzzing
            if (manager.urlFuzz || manager.pureBrutefuzz)
            {
                if (manager.isURLFuzzGenFinished())
                {
                    boolean allWorkersFinished = true;
                    Vector workers = manager.getWorkers();
                    Vector parsers = manager.getParseWorkers();

                    for (int a = 0; a < workers.size(); a++)
                    {
                        if (((Worker) workers.elementAt(a)).isWorking())
                        {
                            //there is a worker still working so break
                            allWorkersFinished = false;
                            break;
                        }
                    }

                    for (int a = 0; a < parsers.size(); a++)
                    {
                        if (((HTMLparse) parsers.elementAt(a)).isWorking())
                        {
                            allWorkersFinished = false;
                            break;
                        }
                    }


                    if (allWorkersFinished)
                    {
                        //all the workers are finished and there is nothing 
                        manager.setStatus("Finished");
                        manager.youAreFinished();
                    }
                }
            }
        }

    }
}

