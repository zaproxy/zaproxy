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
    sealed class ApiResponseFactory
    {
        private ApiResponseFactory()
        {
        }

        public static IApiResponse GetResponse(XmlNode node)
        {
            if (node == null || node.Attributes.Count < 0)
                throw new ArgumentException("node");

            XmlNode typeNode = node.Attributes.GetNamedItem("type");

            if (typeNode != null)
            {
                string type = typeNode.Value;

                switch (type)
                {
                    case "list":
                        return new ApiResponseList(node);
                    case "set":
                        return new ApiResponseSet(node);
                    case "exception":
                        XmlAttributeCollection attributes = node.Attributes;
                        string code = attributes.GetNamedItem("code") != null
                            ?
                            attributes.GetNamedItem("code").Value : "0";
                        string detail = attributes.GetNamedItem("detail") != null
                            ?
                            attributes.GetNamedItem("detail").Value : string.Empty;
                        throw new Exception(node.Value
                            + Environment.NewLine + code
                            + Environment.NewLine + detail);
                    default:
                        break;
                }
            }

            return new ApiResponseElement(node);
        }
    }
}
