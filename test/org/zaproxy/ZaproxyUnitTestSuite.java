package org.zaproxy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.zaproxy.zap.spider.URLResolverUnitTest;

import ch.csnc.extension.httpclient.AliasKeyManagerUnitTest;

/**
 * Suite for all unit tests of ZAP. Tests attached to this suite must run
 * independent of any environmental settings or preferences on any possible
 * machine.
 * 
 * @author bjoern.kimminich@gmx.de
 */
@RunWith(Suite.class)
@SuiteClasses({ URLResolverUnitTest.class, AliasKeyManagerUnitTest.class })
public class ZaproxyUnitTestSuite {

}
