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

namespace OWASPZAPDotNetAPI
{
    public class Alert
    {
        public enum RiskLevel {Informational, Low, Medium, High}
        [Obsolete("Use of ReliabilityLevel has been deprecated from 2.4.0 in favour of using ConfidenceLevel.")]
        public enum ReliabilityLevel { Suspicious, Warning}
        public enum ConfidenceLevel { Low, Medium, High, Confirmed }
        public string AlertMessage { get; set; }
        public RiskLevel Risk { get; set; }
        [Obsolete("Use of Reliability has been deprecated from 2.4.0 in favour of using Confidence.")]
        public ReliabilityLevel Reliability { get; set; }
        public ConfidenceLevel Confidence { get; set; }
        public string Url { get; set; }
        public string Other { get; set; }
        public string Parameter { get; set; }
        public string Attack { get; set; }
        public string Evidence { get; set; }
        public string Description { get; set; }
        public string Reference { get; set; }
        public string Solution { get; set; }
        public int CWEId { get; set; }
        public int WASCId { get; set; }

        public Alert(string alert, string url)
        {
            this.AlertMessage = alert;
            this.Url = url;
        }

        public Alert(string alert, string url, RiskLevel risk, ConfidenceLevel confidence)
            :
            this(alert, url)
        {
            this.Risk = risk;
            this.Confidence = confidence;
        }

        public Alert(string alert, string url, RiskLevel risk, ConfidenceLevel confidence, string parameter, string other)
            :
            this(alert, url, risk, confidence)
        {
            this.Other = other;
            this.Parameter = parameter;
        }

        public Alert(string alert, string url, RiskLevel risk, ConfidenceLevel confidence,
            string parameter, string other, string attack, string description, string reference, string solution,
            string evidence, int cweId, int wascId)
            :
            this(alert, url, risk, confidence, parameter, other)
        {
            this.Attack = attack;
            this.Description = description;
            this.Reference = reference;
            this.Solution = solution;
            this.Evidence = evidence;
            this.CWEId = cweId;
            this.WASCId = wascId;
        }

        public override bool Equals(object obj)
        {
            if (obj == null || GetType() != obj.GetType())
                return false;

            Alert alertToCompate = (Alert)obj;

            if (this.AlertMessage == null)
            {
                if (alertToCompate.AlertMessage != null)
                {
                    return false;
                }
            }
            else if (!this.AlertMessage.Equals(alertToCompate.AlertMessage))
            {
                return false;
            }

            if (!this.Risk.Equals(alertToCompate.Risk))
            {
                return false;
            }

            if (!this.Confidence.Equals(alertToCompate.Confidence))
            {
                return false;
            }

            if (this.Url == null)
            {
                if (alertToCompate.Url != null)
                {
                    return false;
                }
            }
            else if (!this.Url.Equals(alertToCompate.Url))
            {
                return false;
            }

            if (this.Other == null)
            {
                if (alertToCompate.Other != null)
                {
                    return false;
                }
            }
            else if (!this.Other.Equals(alertToCompate.Other))
            {
                return false;
            }

            if (this.Parameter == null)
            {
                if (alertToCompate.Parameter != null)
                {
                    return false;
                }
            }
            else if (!this.Parameter.Equals(alertToCompate.Parameter))
            {
                return false;
            }

            if (this.Attack == null)
            {
                if (alertToCompate.Attack != null)
                {
                    return false;
                }
            }
            else if (!this.Attack.Equals(alertToCompate.Attack))
            {
                return false;
            }

            if (this.Evidence == null)
            {
                if (alertToCompate.Evidence != null)
                {
                    return false;
                }
            }
            else if (!this.Evidence.Equals(alertToCompate.Evidence))
            {
                return false;
            }

            if (this.Description == null)
            {
                if (alertToCompate.Description != null)
                {
                    return false;
                }
            }
            else if (!this.Description.Equals(alertToCompate.Description))
            {
                return false;
            }

            if (this.Reference == null)
            {
                if (alertToCompate.Reference != null)
                {
                    return false;
                }
            }
            else if (!this.Reference.Equals(alertToCompate.Reference))
            {
                return false;
            }

            if (this.Solution == null)
            {
                if (alertToCompate.Solution != null)
                {
                    return false;
                }
            }
            else if (!this.Solution.Equals(alertToCompate.Solution))
            {
                return false;
            }

            if (this.CWEId != alertToCompate.CWEId)
            {
                return false;
            }

            if (this.WASCId != alertToCompate.WASCId)
            {
                return false;
            }

            return true;
        }

        public override int GetHashCode()
        {
            unchecked
            {
                int hash = 17;
                hash = hash * 23 + ((AlertMessage == null) ? 0 : AlertMessage.GetHashCode());
                hash = hash * 23 + (Risk.GetHashCode());
                hash = hash * 23 + (Confidence.GetHashCode());
                hash = hash * 23 + ((Url == null) ? 0 : Url.GetHashCode());
                hash = hash * 23 + ((Other == null) ? 0 : Other.GetHashCode());
                hash = hash * 23 + ((Parameter == null) ? 0 : Parameter.GetHashCode());
                hash = hash * 23 + ((Attack == null) ? 0 : Attack.GetHashCode());
                hash = hash * 23 + ((Evidence == null) ? 0 : Evidence.GetHashCode());
                hash = hash * 23 + ((Description == null) ? 0 : Description.GetHashCode());
                hash = hash * 23 + ((Reference == null) ? 0 : Reference.GetHashCode());
                hash = hash * 23 + ((Solution == null) ? 0 : Solution.GetHashCode());
                hash = hash * 23 + CWEId.GetHashCode();
                hash = hash * 23 + WASCId.GetHashCode();
                return hash;
            }
        }

        public override string ToString()
        {
            return base.ToString();
        }
    }
}
