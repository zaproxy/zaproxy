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
    public class ApiResponseList : IApiResponse
    {
        public string Name { get; set; }

        private List<IApiResponse> list;

        public List<IApiResponse> List { get { return this.list; } }

        public ApiResponseList(string name)
        {
            this.Name = name;
            this.list = new List<IApiResponse>();
        }

        public ApiResponseList(string name, List<IApiResponse> apiResponse)
        {
            this.Name = name;
            this.list = apiResponse;
        }

        public ApiResponseList(string name, IApiResponse[] apiResponse)
            :
            this(name, apiResponse.ToList())
        {
        }

        public ApiResponseList(XmlNode node)
            :
            this(node.Name)
        {
            XmlNode childNode = node.FirstChild;
            while (childNode != null)
            {
                list.Add(ApiResponseFactory.GetResponse(childNode));
                childNode = childNode.NextSibling;
            }
        }
    }
}
