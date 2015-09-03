/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace OWASPZAPDotNetAPI.Samples
{
    /// <summary>
    /// Samples to start the OWASP Zed Attack Proxy
    /// Refer to the command line options at https://github.com/zaproxy/zap-core-help/wiki/HelpCmdline
    /// </summary>
    public static class ZAP
    {
        public static void StartZapUI()
        {
            Console.WriteLine("Trying to StartZapUI");
            ProcessStartInfo zapProcessStartInfo = new ProcessStartInfo();
            zapProcessStartInfo.FileName = @"C:\Program Files (x86)\OWASP\Zed Attack Proxy\ZAP.exe";
            zapProcessStartInfo.WorkingDirectory = @"C:\Program Files (x86)\OWASP\Zed Attack Proxy";

            Console.WriteLine(zapProcessStartInfo.ToString());
            Console.WriteLine("Issuing command to StartZapUI");
            Process zap = Process.Start(zapProcessStartInfo);

            //Sleep(120000); //you can choose to wait for 2 minutes and bet that ZAP has started
            CheckIfZAPHasStartedByPollingTheAPI(1); //you can try accessing an API and ensure ZAP has fully initialized
        }

        public static void StartZAPDaemon()
        {
            Console.WriteLine("Trying to StartZAPDaemon");
            ProcessStartInfo zapProcessStartInfo = new ProcessStartInfo();
            zapProcessStartInfo.FileName = @"C:\Program Files (x86)\OWASP\Zed Attack Proxy\ZAP.exe";
            zapProcessStartInfo.WorkingDirectory = @"C:\Program Files (x86)\OWASP\Zed Attack Proxy";
            zapProcessStartInfo.Arguments = "-daemon -host 127.0.0.1 -port 7070";

            Console.WriteLine("Issuing command to StartZAPDaemon");
            Console.WriteLine(zapProcessStartInfo.ToString());
            Process zap = Process.Start(zapProcessStartInfo);

            //Sleep(120000); //you can choose to wait for 2 minutes and bet that ZAP has started
            CheckIfZAPHasStartedByPollingTheAPI(1); //you can try accessing an API and ensure ZAP has fully initialized
        }

        private static void Sleep(int sleepTime)
        {
            Console.WriteLine("Sleeping for {0} minutes", sleepTime / 1000);
            Thread.Sleep(sleepTime);
        }

        public static void CheckIfZAPHasStartedByPollingTheAPI(int minutesToWait)
        {
            WebClient webClient = new WebClient();
            Stopwatch watch = new Stopwatch();
            watch.Start();
            int millisecondsToWait = minutesToWait * 60 * 1000;
            string zapUrlToDownload = "http://localhost:7070";

            while (millisecondsToWait > watch.ElapsedMilliseconds)
            {
                try
                {
                    Console.WriteLine("Trying to check if ZAP has started by accessing the ZAP API at {0}", zapUrlToDownload);
                    string responseString = webClient.DownloadString(zapUrlToDownload);
                    Console.WriteLine(Environment.NewLine + responseString + Environment.NewLine);
                    Console.WriteLine("Obtained a response from the ZAP API at {0} {1}Hence assuming that ZAP started successfully", zapUrlToDownload, Environment.NewLine);
                    return;
                }
                catch (WebException webException)
                {
                    Console.WriteLine("Seems like ZAP did not start yet");
                    Console.WriteLine(webException.Message + Environment.NewLine);
                    Console.WriteLine("Sleeping for 2 seconds");
                    Thread.Sleep(2000);
                } 
            }

            throw new Exception(string.Format("Waited for {0} minutes, however could not access the API successfully, hence could not verify if ZAP started successfully or not", minutesToWait));
        }
    }
}
