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
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;

namespace OWASPZAPDotNetAPI
{
    public class ApiResponseSet : IApiResponse
    {
        public string Name { get; set; }
        private Dictionary<string, string> dictionary;
        public Dictionary<string, string> Dictionary { get { return this.dictionary; } }
        //private string[] attributes; // attributes field is present at org.zaproxy.clientapi.core, but I couldn't track the usage in the java api client, hence ignoring it in the dot net api

        public ApiResponseSet(string name)
        {
            this.Name = name;
        }

        public ApiResponseSet(string name, Dictionary<string, string> dictionary)
        {
            this.Name = name;
            this.dictionary = dictionary;
        }

        public ApiResponseSet(XmlNode node)
        {
            this.Name = node.Name;
            XmlNode childNode = node.FirstChild;
            this.dictionary = new Dictionary<string, string>();
            while (childNode != null)
            {
                ApiResponseElement apiResponseElement = (ApiResponseElement)ApiResponseFactory.GetResponse(childNode);
                this.dictionary.Add(apiResponseElement.Name, apiResponseElement.Value);
                childNode = childNode.NextSibling;
            }
        }
    }
}
