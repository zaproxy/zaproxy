using OWASPZAPDotNetAPI;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace OWASPZAPDotNetAPI.Samples
{
    class AuthenticatedScanWithFormsAuthentication
    {
        private static string _target = "http://localhost:8020/SqliModernApp";
        private static string _apikey = string.Empty;
        private static ClientApi _api = new ClientApi("localhost", 7070);
        private static IApiResponse _apiResponse;

        public static void Go()
        {
            LoadTargetUrlToSitesTree();

            string contextName = "SqliModernApp";
            string contextId = CreateContext(contextName);
            string urlToIncludeInContext = @"\Qhttp://localhost:8020/SqliModernApp\E.*";
            IncludeUrlToContext(contextName, urlToIncludeInContext);

            if (IsFormsAuthenticationSupported())
            {
                string formBasedAuthenticationConfiguration = PrepareLoginUrlAndRequestData();
                SetFormsBasedAuthentication(contextId, formBasedAuthenticationConfiguration);
                SetLoggedInIndicatorForFormsBasedAuthentication(contextId);
            }

            string userId = string.Empty;
            string userName = "ZAP";
            string password = "adminadmin";

            userId = CreateNewUser(contextId, userName);
            SetUserNameAndPassword(contextId, userId, userName, password);
            EnableUser(contextId, userId);

            SetASpecificForcedUser(contextId, userId);
            EnableForcedUserMode();

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
            File.WriteAllBytes(reportFileName + ".html" , _api.core.htmlreport(_apikey));
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
            Thread.Sleep(10000);
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
            Thread.Sleep(10000);
        }

        private static string StartSpidering()
        {
            Console.WriteLine("Spider: " + _target);
            _apiResponse = _api.spider.scan(_apikey, _target, "");
            string scanid = ((ApiResponseElement)_apiResponse).Value;
            return scanid;
        }

        private static void EnableForcedUserMode()
        {
            _apiResponse = _api.forcedUser.setForcedUserModeEnabled(_apikey, true);
        }

        private static void SetASpecificForcedUser(string contextId, string userId)
        {
            _apiResponse = _api.forcedUser.setForcedUser(_apikey, contextId, userId);
        }

        private static void EnableUser(string contextId, string userId)
        {
            _apiResponse = _api.users.setUserEnabled(_apikey, contextId, userId, "true");

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("user enabled");
        }

        private static void SetUserNameAndPassword(string contextId, string userId, string userName, string password)
        {
            string userAuthenticationConfigFormat = "username={0}&password={1}";
            string userAuthenticationConfig = string.Format(userAuthenticationConfigFormat, userName, password);

            _apiResponse = _api.users.setAuthenticationCredentials(_apikey, contextId, userId, userAuthenticationConfig);

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("User credentials set");
        }

        private static string CreateNewUser(string contextId, string userName)
        {
            _apiResponse = _api.users.newUser(_apikey, contextId, userName);
            string userId = ((ApiResponseElement)_apiResponse).Value;
            return userId;
        }

        private static void ConfigureFormsBasedAuthentication(string contextId)
        {

        }

        private static void SetLoggedInIndicatorForFormsBasedAuthentication(string contextId)
        {
            string loggedInIndicator = @"\Q<form action=""/SqliModernApp/Account/LogOff"" id=""logoutForm"" method=""post"">\E";
            _apiResponse = _api.authentication.setLoggedInIndicator(_apikey, contextId, loggedInIndicator);

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("loggedInIndicator is set");
        }

        private static void SetFormsBasedAuthentication(string contextId, string formBasedAuthenticationConfiguration)
        {
            _apiResponse = _api.authentication.setAuthenticationMethod(_apikey, contextId, "formBasedAuthentication", formBasedAuthenticationConfiguration);

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("formBasedAuthentication is configured");
        }

        private static string PrepareLoginUrlAndRequestData()
        {
            string loginUrl = "http://localhost:8020/SqliModernApp/Account/Login";
            string loginRequestData = "username={%username%}&password={%password%}";

            string formBasedAuthenticationConfigurationFormat = "loginUrl={0}&loginRequestData={1}";
            string formBasedAuthenticationConfiguration = string.Format(formBasedAuthenticationConfigurationFormat,
                Uri.EscapeDataString(loginUrl),
                Uri.EscapeDataString(loginRequestData));
            return formBasedAuthenticationConfiguration;
        }

        private static bool IsFormsAuthenticationSupported()
        {
            bool isFormsAuthenticatedSupported = false;
            string formsAuthenticationString = "formBasedAuthentication";

            _apiResponse = _api.authentication.getSupportedAuthenticationMethods();

            Console.WriteLine("Supported authentication methods are:" + Environment.NewLine);
            foreach (var authenticationMethod in ((ApiResponseList)_apiResponse).List)
            {
                string authenticationMethodName = ((ApiResponseElement)authenticationMethod).Value;
                if (formsAuthenticationString.IndexOf(authenticationMethodName, StringComparison.InvariantCultureIgnoreCase) > -1)
                {
                    isFormsAuthenticatedSupported = true;
                }
                Console.WriteLine(authenticationMethodName + Environment.NewLine);
            }

            return isFormsAuthenticatedSupported;
        }

        private static void IncludeUrlToContext(string contextName, string urlToIncludeInContext)
        {
            _apiResponse = _api.context.includeInContext(_apikey, contextName, urlToIncludeInContext);

            if ("OK" == ((ApiResponseElement)_apiResponse).Value)
                Console.WriteLine("{0} included to context {1}", urlToIncludeInContext, contextName);
        }

        private static string CreateContext(string contextName)
        {
            _apiResponse = _api.context.newContext(_apikey, contextName);
            string contextId = ((ApiResponseElement)_apiResponse).Value;
            Console.WriteLine("{0} context created with id {1}", contextName, contextId);
            return contextId;
        }

        private static void LoadTargetUrlToSitesTree()
        {
            _api.AccessUrl(_target);
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
