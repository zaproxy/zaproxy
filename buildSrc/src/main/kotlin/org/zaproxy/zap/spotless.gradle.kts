package org.zaproxy.zap

plugins {
    com.diffplug.spotless
}

spotless {
    format ("license", {
        licenseHeaderFile(project.file("$rootDir/gradle/spotless/license.java"), "package ")

        target(project.fileTree(project.projectDir) {
            include(listOf(
                "src/main/java/ch/csnc/extension/httpclient/PKCS11Configuration.java",
                "src/main/java/ch/csnc/extension/util/OptionsParamExperimentalSliSupport.java",
                "src/main/java/org/parosproxy/paros/db/TableSessionUrl.java",
                "src/main/java/org/parosproxy/paros/db/DatabaseUnsupportedException.java",
                "src/main/java/org/parosproxy/paros/db/TableSession.java",
                "src/main/java/org/parosproxy/paros/db/RecordContext.java",
                "src/main/java/org/parosproxy/paros/db/TableTag.java",
                "src/main/java/org/parosproxy/paros/db/TableAlert.java",
                "src/main/java/org/parosproxy/paros/db/RecordStructure.java",
                "src/main/java/org/parosproxy/paros/db/RecordTag.java",
                "src/main/java/org/parosproxy/paros/db/Database.java",
                "src/main/java/org/parosproxy/paros/db/RecordParam.java",
                "src/main/java/org/parosproxy/paros/db/DbUtils.java",
                "src/main/java/org/parosproxy/paros/db/RecordSessionUrl.java",
                "src/main/java/org/parosproxy/paros/db/DatabaseException.java",
                "src/main/java/org/parosproxy/paros/db/TableParam.java",
                "src/main/java/org/parosproxy/paros/db/paros/ParosTableParam.java",
                "src/main/java/org/parosproxy/paros/db/paros/ParosTableTag.java",
                "src/main/java/org/parosproxy/paros/db/paros/ParosTableSessionUrl.java",
                "src/main/java/org/parosproxy/paros/db/paros/ParosTableContext.java",
                "src/main/java/org/parosproxy/paros/db/paros/ParosTableStructure.java",
                "src/main/java/org/parosproxy/paros/db/TableStructure.java",
                "src/main/java/org/parosproxy/paros/db/AbstractDatabase.java",
                "src/main/java/org/parosproxy/paros/db/TableScan.java",
                "src/main/java/org/parosproxy/paros/db/DatabaseServer.java",
                "src/main/java/org/parosproxy/paros/db/TableContext.java",
                "src/main/java/org/parosproxy/paros/db/TableHistory.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantScript.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantDirectWebRemotingQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantXMLQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantDdnPath.java",
                "src/main/java/org/parosproxy/paros/core/scanner/ScannerParamFilter.java",
                "src/main/java/org/parosproxy/paros/core/scanner/MultipartFormParameter.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantURLPath.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantHeader.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantAbstractRPCQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantODataIdQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantODataFilterQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantUserDefined.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantMultipartFormParameters.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantGWTQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantCookie.java",
                "src/main/java/org/parosproxy/paros/core/scanner/ScannerHook.java",
                "src/main/java/org/parosproxy/paros/core/scanner/PluginStats.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantJSONQuery.java",
                "src/main/java/org/parosproxy/paros/core/scanner/VariantCustom.java",
                "src/main/java/org/parosproxy/paros/core/proxy/CustomStreamsSocket.java",
                "src/main/java/org/parosproxy/paros/core/proxy/OverrideMessageProxyListener.java",
                "src/main/java/org/parosproxy/paros/core/proxy/ConnectRequestProxyListener.java",
                "src/main/java/org/parosproxy/paros/core/proxy/ArrangeableProxyListener.java",
                "src/main/java/org/parosproxy/paros/model/HttpMessageCachedData.java",
                "src/main/java/org/parosproxy/paros/model/HistoryReferenceEventPublisher.java",
                "src/main/java/org/parosproxy/paros/model/SiteMapEventPublisher.java",
                "src/main/java/org/parosproxy/paros/network/ProxyExcludedDomainMatcher.java",
                "src/main/java/org/parosproxy/paros/network/HtmlParameter.java",
                "src/main/java/org/parosproxy/paros/network/HttpHeaderField.java",
                "src/main/java/org/parosproxy/paros/network/DecoratedSocketsSslSocketFactory.java",
                "src/main/java/org/parosproxy/paros/extension/option/DatabaseParam.java",
                "src/main/java/org/parosproxy/paros/extension/option/DialogModifyProxyExcludedDomain.java",
                "src/main/java/org/parosproxy/paros/extension/option/OptionsJvmPanel.java",
                "src/main/java/org/parosproxy/paros/extension/option/DialogAddProxyExcludedDomain.java",
                "src/main/java/org/parosproxy/paros/extension/option/SecurityProtocolsPanel.java",
                "src/main/java/org/parosproxy/paros/extension/option/ProxyExcludedDomainsTableModel.java",
                "src/main/java/org/parosproxy/paros/extension/option/OptionsDatabasePanel.java",
                "src/main/java/org/parosproxy/paros/extension/manualrequest/http/impl/HttpPanelSender.java",
                "src/main/java/org/parosproxy/paros/extension/manualrequest/MessageSender.java",
                "src/main/java/org/parosproxy/paros/extension/history/HistoryFilter.java",
                "src/main/java/org/parosproxy/paros/extension/history/HistoryTableModel.java",
                "src/main/java/org/parosproxy/paros/extension/history/ProxyListenerLogEventPublisher.java",
                "src/main/java/org/parosproxy/paros/extension/history/HistoryTable.java",
                "src/main/java/org/parosproxy/paros/extension/history/PopupMenuResendSites.java",
                "src/main/java/org/parosproxy/paros/security/CertData.java",
                "src/main/java/org/zaproxy/**/*.java",
                "src/test/java/**/*.java"
            ))
            // Different license (e.g. 3rd-party, generated code)
            exclude(listOf(
                "src/main/java/org/zaproxy/zap/extension/ascan/AllCategoryTableModel.java",
                "src/main/java/org/zaproxy/zap/extension/ascan/CategoryTableModel.java",
                "src/main/java/org/zaproxy/zap/extension/ascan/PolicyAllCategoryPanel.java",
                "src/main/java/org/zaproxy/zap/extension/ascan/PolicyCategoryPanel.java",
                "src/main/java/org/zaproxy/zap/extension/ascan/PolicyDialog.java",
                "src/main/java/org/zaproxy/zap/extension/dynssl/DynSSLParam.java",
                "src/main/java/org/zaproxy/zap/extension/dynssl/DynamicSSLPanel.java",
                "src/main/java/org/zaproxy/zap/extension/dynssl/DynamicSSLWelcomeDialog.java",
                "src/main/java/org/zaproxy/zap/extension/dynssl/ExtensionDynSSL.java",
                "src/main/java/org/zaproxy/zap/extension/dynssl/SslCertificateUtils.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/DialogAddToken.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/DialogModifyToken.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/ExtensionGlobalExcludeURL.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/GlobalExcludeURLParam.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/GlobalExcludeURLParamToken.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/OptionsGlobalExcludeURLPanel.java",
                "src/main/java/org/zaproxy/zap/extension/globalexcludeurl/OptionsGlobalExcludeURLTableModel.java",
                "src/main/java/org/zaproxy/zap/extension/httppanel/view/posttable/RequestPostTableModel.java",
                "src/main/java/org/zaproxy/zap/extension/httppanel/view/syntaxhighlight/lexers/*.java",
                "src/main/java/org/zaproxy/zap/extension/stats/StatsdClient.java",
                "src/main/java/org/zaproxy/zap/network/ZapNTLMEngineImpl.java",
                "src/main/java/org/zaproxy/zap/network/ZapNTLMScheme.java",
                "src/main/java/org/zaproxy/zap/spider/URLCanonicalizer.java",
                "src/main/java/org/zaproxy/zap/spider/URLResolver.java",
                "src/main/java/org/zaproxy/zap/utils/SortedComboBoxModel.java"
            ))
        })
    })

    java {
        clearSteps()
        googleJavaFormat("1.7").aosp()

        // Exclude forked/3rd-party/generated files.
        targetExclude(listOf(
            "src/main/java/org/apache/**/*.java",
            "src/main/java/org/parosproxy/paros/network/GenericMethod.java",
            "src/main/java/org/zaproxy/zap/extension/stats/StatsdClient.java",
            "src/main/java/org/zaproxy/zap/network/ZapNTLMEngineImpl.java",
            "src/main/java/org/zaproxy/zap/network/ZapNTLMScheme.java"
        ))
    }
}

