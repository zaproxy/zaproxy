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
