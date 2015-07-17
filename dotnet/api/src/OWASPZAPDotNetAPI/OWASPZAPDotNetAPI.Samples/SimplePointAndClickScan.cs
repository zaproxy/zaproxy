using OWASPZAPDotNetAPI;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace OWASPZAPDotNetAPI.Samples
{
    class SimplePointAndClickScan
    {
        private static string _target = "http://www.renthoughtsweb.com:8020/SqliModernApp/";
        private static string _apikey = string.Empty;
        private static ClientApi _api = new ClientApi("localhost", 7070);
        private static IApiResponse _apiResponse;

        public static void Go()
        {
            string spiderScanId = StartSpidering();
            PollTheSpiderTillCompletion(spiderScanId);

            StartAjaxSpidering();
            PollTheAjaxSpiderTillCompletion();

            string activeScanId = StartActiveScanning();
            PollTheActiveScannerTillCompletion(activeScanId);

            string reportFileName = string.Format("report-{0}", DateTime.Now.ToString("dd-MMM-yyyy-hh-mm-ss"));
            WriteXmlReport(reportFileName);
            WriteHtmlReport(reportFileName);
            PrintAlertsToConsole();

            ShutdownZAP();
        }

        private static void ShutdownZAP()
        {
            _apiResponse = _api.core.shutdown("");
            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("ZAP shutdown success " + _target);
        }

        private static void PrintAlertsToConsole()
        {
            List<Alert> alerts = _api.GetAlerts(_target, 0, 0);
            foreach (var alert in alerts)
            {
                Console.WriteLine(alert.AlertMessage
                    + Environment.NewLine
                    + alert.CWEId
                    + Environment.NewLine
                    + alert.Url
                    + Environment.NewLine
                    + alert.WASCId
                    + Environment.NewLine
                    + alert.Evidence
                    + Environment.NewLine
                    + alert.Parameter
                    + Environment.NewLine
                );
            }
        }

        private static void WriteHtmlReport(string reportFileName)
        {
            File.WriteAllBytes(reportFileName + ".html", _api.core.htmlreport(_apikey));
        }

        private static void WriteXmlReport(string reportFileName)
        {
            File.WriteAllBytes(reportFileName + ".xml", _api.core.xmlreport(_apikey));
        }

        private static void PollTheActiveScannerTillCompletion(string activeScanId)
        {
            int activeScannerprogress;
            while (true)
            {
                Sleep(5000);
                activeScannerprogress = int.Parse(((ApiResponseElement)_api.ascan.status(activeScanId)).Value);
                Console.WriteLine("Active scanner progress: {0}%", activeScannerprogress);
                if (activeScannerprogress >= 100)
                    break;
            }
            Console.WriteLine("Active scanner complete");
        }

        private static string StartActiveScanning()
        {
            Console.WriteLine("Active Scanner: " + _target);
            _apiResponse = _api.ascan.scan(_apikey, _target, "", "", "", "", "");

            string activeScanId = ((ApiResponseElement)_apiResponse).Value;
            return activeScanId;
        }

        private static void PollTheAjaxSpiderTillCompletion()
        {
            while (true)
            {
                Sleep(1000);
                string ajaxSpiderStatusText = string.Empty;
                ajaxSpiderStatusText = Convert.ToString(((ApiResponseElement)_api.ajaxspider.status()).Value);
                if (ajaxSpiderStatusText.IndexOf("running", StringComparison.InvariantCultureIgnoreCase) > -1)
                    Console.WriteLine("Ajax Spider running");
                else
                    break;
            }

            Console.WriteLine("Ajax Spider complete");
            Sleep(10000);
        }

        private static void StartAjaxSpidering()
        {
            Console.WriteLine("Ajax Spider: " + _target);
            _apiResponse = _api.ajaxspider.scan(_apikey, _target, "");

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("Ajax Spider started for " + _target);
        }

        private static void PollTheSpiderTillCompletion(string scanid)
        {
            int spiderProgress;
            while (true)
            {
                Sleep(1000);
                spiderProgress = int.Parse(((ApiResponseElement)_api.spider.status(scanid)).Value);
                Console.WriteLine("Spider progress: {0}%", spiderProgress);
                if (spiderProgress >= 100)
                    break;
            }

            Console.WriteLine("Spider complete");
            Sleep(10000);
        }

        private static string StartSpidering()
        {
            Console.WriteLine("Spider: " + _target);
            _apiResponse = _api.spider.scan(_apikey, _target, "");
            string scanid = ((ApiResponseElement)_apiResponse).Value;
            return scanid;
        }

        private static void Sleep(int milliseconds)
        {
            do
            {
                Thread.Sleep(milliseconds);
                Console.WriteLine("...zz" + Environment.NewLine);
                milliseconds = milliseconds - 2000;
            } while (milliseconds > 2000);
        }
    }
}
