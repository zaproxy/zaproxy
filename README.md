# [![](https://raw.githubusercontent.com/wiki/zaproxy/zaproxy/images/zap32x32.png) OWASP ZAP](https://www.owasp.org/index.php/ZAP)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/zaproxy/zaproxy.svg)](https://github.com/zaproxy/zaproxy/wiki/Downloads)
[![Build Status](https://travis-ci.org/zaproxy/zaproxy.svg?branch=develop)](https://travis-ci.org/zaproxy/zaproxy)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/24/badge)](https://bestpractices.coreinfrastructure.org/projects/24)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/5559/badge.svg)](https://scan.coverity.com/projects/zaproxy-zaproxy)
[![Github Releases](https://img.shields.io/github/downloads/zaproxy/zaproxy/latest/total.svg?maxAge=2592000)](https://zapbot.github.io/zap-mgmt-scripts/downloads.html)
[![Javadocs](https://javadoc.io/badge/org.zaproxy/zap/2.6.0.svg)](https://javadoc.io/doc/org.zaproxy/zap/2.6.0)
[![OWASP Flagship](https://img.shields.io/badge/owasp-flagship-brightgreen.svg)](https://www.owasp.org/index.php/OWASP_Project_Inventory#tab=Flagship_Projects)
[![Twitter Follow](https://img.shields.io/twitter/follow/zaproxy.svg?style=social&label=Follow&maxAge=2592000)](https://twitter.com/zaproxy)

The OWASP Zed Attack Proxy (ZAP) is one of the world’s most popular free security tools and is actively maintained by hundreds of international volunteers[*](#justification). It can help you automatically find security vulnerabilities in your web applications while you are developing and testing your applications. Its also a great tool for experienced pentesters to use for manual security testing.


[![](https://raw.githubusercontent.com/wiki/zaproxy/zaproxy/images/ZAP-Download.png)](https://github.com/zaproxy/zaproxy/wiki/Downloads)

#### Please help us to make ZAP even better for you by answering the [ZAP User Questionnaire](https://docs.google.com/forms/d/1-k-vcj_sSxlil6XLxCFade-m-IQVeE2h9gduA-2ZPPA/viewform)!

For general information about ZAP:
  * [Home page](https://www.owasp.org/index.php/ZAP) - the official ZAP page on the OWASP wiki (includes a donate button;)
  * [Twitter](https://twitter.com/zaproxy)	- official ZAP announcements (low volume)
  * [Blog](https://zaproxy.blogspot.com/)	- official ZAP blog
  * [Monthly Newsletters](https://github.com/zaproxy/zaproxy/wiki/Newsletters) - ZAP news, tutorials, 3rd party tools and featured contributors
  * [Swag!](https://github.com/zaproxy/zap-swag) - official ZAP swag that you can buy, as well as all of the original artwork released under the CC License

For help using ZAP:
  * [Getting Started Guide (pdf)](https://github.com/zaproxy/zaproxy/releases/download/2.6.0/ZAPGettingStartedGuide-2.6.pdf) - an introductory guide you can print
  * [Tutorial Videos](https://www.youtube.com/playlist?list=PLEBitBW-Hlsv8cEIUntAO8st2UGhmrjUB)
  * [Articles](https://github.com/zaproxy/zaproxy/wiki/ZAP-Articles) - that go into ZAP features in more depth
  * [Frequently Asked Questions](https://github.com/zaproxy/zaproxy/wiki/FAQtoplevel)
  * [User Guide](https://github.com/zaproxy/zap-core-help/wiki) - online version of the User Guide included with ZAP
  * [User Group](https://groups.google.com/group/zaproxy-users) - ask questions about using ZAP
  * IRC: irc.mozilla.org #websectools (eg [using Mibbit](http://chat.mibbit.com/?server=irc.mozilla.org%3A%2B6697&channel=%23websectools)) - chat with core ZAP developers (European office hours usually best)
  * [Add-ons](https://github.com/zaproxy/zap-extensions/wiki) - help for the optional add-ons you can install
  * [StackOverflow](https://stackoverflow.com/questions/tagged/zap) - because some people use this for everything ;)

Information about the official ZAP Jenkins plugin:
  * [Wiki](https://wiki.jenkins-ci.org/display/JENKINS/zap+plugin)
  * [Group](https://groups.google.com/forum/#%21forum/zaproxy-jenkins)
  * [Issue tracker](https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20component%20%3D%20zap-plugin)
  * [Source code](https://github.com/jenkinsci/zap-plugin)

To learn more about ZAP development:
  * [Source Code](https://github.com/zaproxy) - for all of the ZAP related projects
  * [Wiki](https://github.com/zaproxy/zaproxy/wiki/Introduction) - lots of detailed info
  * [Developer Group](https://groups.google.com/group/zaproxy-develop) - ask questions about the ZAP internals
  * [Crowdin (GUI)](https://crowdin.com/project/owasp-zap) - help translate the ZAP GUI
  * [Crowdin (User Guide)](https://crowdin.com/project/owasp-zap-help) - help translate the ZAP User Guide
  * [OpenHub](https://www.openhub.net/p/zaproxy)	- FOSS analytics
  * [BountySource](https://www.bountysource.com/teams/zap/issues)	- Vote on ZAP issues (you can also donate money here, but 10% taken out)
  * [Bug Bounty Program](https://bugcrowd.com/owaspzap) - please use this to report any potential vulnerabilities you find in ZAP

#### Justification
Justification for the statements made in the tagline at the top;)

Popularity:
  * ToolsWatch Annual Best Free/Open Source Security Tool Survey:
    * 2016 [2nd](http://www.toolswatch.org/2017/02/2016-top-security-tools-as-voted-by-toolswatch-org-readers/)
    * 2015 [1st](http://www.toolswatch.org/2016/02/2015-top-security-tools-as-voted-by-toolswatch-org-readers/)
    * 2014 [2nd](http://www.toolswatch.org/2015/01/2014-top-security-tools-as-voted-by-toolswatch-org-readers/)
    * 2013 [1st](http://www.toolswatch.org/2013/12/2013-top-security-tools-as-voted-by-toolswatch-org-readers/)

Contributors:
  * [Code Contributors](https://www.openhub.net/p/zaproxy)
  * [ZAP core i18n Contributors](https://crowdin.com/project/owasp-zap)
  * [ZAP help i18n Contributors](https://crowdin.com/project/owasp-zap-help)
