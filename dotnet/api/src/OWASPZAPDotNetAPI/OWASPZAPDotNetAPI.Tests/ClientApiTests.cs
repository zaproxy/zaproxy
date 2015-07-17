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
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace OWASPZAPDotNetAPI.Tests
{
    [TestClass]
    public class ClientApiTests
    {
        private ClientApi zap;

        [TestInitialize]
        public void InstantiateClientApi()
        {
            zap = new ClientApi("localhost", 7070);
        }

        [TestCleanup]
        public void DisposeClientApi()
        {
            zap.Dispose();
        }

        [TestMethod]
        public void When_CallApi_Is_Called_IApiResponse_IsReturned()
        {
            var response = zap.CallApi("authentication", "view", "getSupportedAuthenticationMethods", null);
            Assert.IsInstanceOfType(response, typeof(IApiResponse));
        }

        [TestMethod]
        public void When_CallApi_getSupportedAuthenticationMethods_Is_Called_ApiResponseList_IsReturned()
        {
            var response = zap.CallApi("authentication", "view", "getSupportedAuthenticationMethods", null);
            Assert.IsInstanceOfType(response, typeof(ApiResponseList));
        }

        [TestMethod]
        [ExpectedException(typeof(Exception), "Message=bad_view")]
        public void When_CallApi_getForcedUser_With_NonExistantContext_Is_Called_Exception_Thrown()
        {
            IApiResponse response = zap.CallApi("authentication", "view", "asasasasdasd", null);
        }
        
        [TestMethod]
        public void When_CallApi_getSupportedAuthenticationMethods_Is_Called_ApiResponseList_With_formBasedAuthentication_IsReturned()
        {
            var response = zap.CallApi("authentication", "view", "getSupportedAuthenticationMethods", null);
            bool formBasedAuthenticationFound = false;
            ApiResponseList apiResponseList = (ApiResponseList)response;            
            foreach (var item in apiResponseList.List)
            {
                var apiResponseElement = (ApiResponseElement)item;
                if (apiResponseElement.Value == "formBasedAuthentication")
                {
                    formBasedAuthenticationFound = true;
                    break;
                }
            }
            Assert.IsTrue(formBasedAuthenticationFound);
        }

        [TestMethod]
        public void When_CallApi_alerts_Is_Called_ApiResponseList_Is_Returned()
        {
            var response = zap.CallApi("core", "view", "alerts", null);
            ApiResponseList apiResponseList = (ApiResponseList)response;
            Assert.IsInstanceOfType(response, typeof(ApiResponseList));
        }
    }
}
