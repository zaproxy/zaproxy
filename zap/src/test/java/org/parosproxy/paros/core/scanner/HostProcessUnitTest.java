/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parosproxy.paros.core.scanner;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.Locale;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link HostProcess}. */
class HostProcessUnitTest {

    private StructuralNode rootNode;
    private String hostAndPort;
    private Scanner scanner;
    private ScannerParam scannerParam;
    private PluginFactory pluginFactory;
    private RuleConfigParam ruleConfigParam;

    private HostProcess hostProcess;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setup() {
        Constant.messages = new I18N(Locale.ENGLISH);

        rootNode = mock(StructuralNode.class);
        given(rootNode.getName()).willReturn("Sites");

        hostAndPort = "http://localhost:80";

        scanner = mock(Scanner.class);
        given(scanner.isInScope(any())).willReturn(true);

        scannerParam = mock(ScannerParam.class);
        Model model = mock(Model.class);
        Model.setSingletonForTesting(model);
        OptionsParam optionsParam = mock(OptionsParam.class);
        given(model.getOptionsParam()).willReturn(optionsParam);
        given(optionsParam.getConnectionParam())
                .willReturn(mock(org.parosproxy.paros.network.ConnectionParam.class));

        pluginFactory = mock(PluginFactory.class);
        given(pluginFactory.clone()).willReturn(pluginFactory);
        ScanPolicy scanPolicy = mock(ScanPolicy.class);
        given(scanPolicy.getPluginFactory()).willReturn(pluginFactory);

        hostProcess =
                new HostProcess(hostAndPort, scanner, scannerParam, scanPolicy, ruleConfigParam);
    }

    @Test
    void shouldNotScanNullStartNodeSet() {
        // Given
        StructuralNode node = null;
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
    }

    @Test
    void shouldNotScanNullStartNodeAdded() {
        // Given
        StructuralNode node = null;
        hostProcess.addStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
    }

    @Test
    void shouldNotScanNodeWithoutHistoryReference() {
        // Given
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        HistoryReference historyReference = null;
        given(node.getHistoryReference()).willReturn(historyReference);
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
    }

    @Test
    void shouldNotScanNodeWithScannerHistoryReference() {
        // Given
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getHistoryType()).willReturn(HistoryReference.TYPE_SCANNER);
        given(node.getHistoryReference()).willReturn(historyReference);
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
    }

    @Test
    void shouldNotScanNodeNotInScope() {
        // Given
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        given(scanner.isInScope(any())).willReturn(false);
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
        verify(scanner).isInScope("GET:file");
    }

    @Test
    void shouldNotScanFilteredNode() throws Exception {
        // Given
        ScanFilter scanFilter = mock(ScanFilter.class);
        String filteredReason = "reason";
        FilterResult filterResult = new FilterResult(filteredReason);
        given(scanFilter.isFiltered(any())).willReturn(filterResult);
        given(scanner.getScanFilters()).willReturn(asList(scanFilter));
        HttpMessage httpMessage = mock(HttpMessage.class);
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        given(node.getHistoryReference().getHttpMessage()).willReturn(httpMessage);
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
        verify(scanFilter).isFiltered(node);
        verify(scanner).notifyFilteredMessage(httpMessage, filteredReason);
    }

    @Test
    void shouldScanNonFilteredNode() {
        // Given
        ScanFilter scanFilter = mock(ScanFilter.class);
        given(scanFilter.isFiltered(any())).willReturn(FilterResult.NOT_FILTERED);
        given(scanner.getScanFilters()).willReturn(asList(scanFilter));
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
        verify(scanFilter).isFiltered(node);
        verify(scanner, times(0)).notifyFilteredMessage(any(), any());
    }

    @Test
    void shouldHandleExceptionsThrownByFilters() {
        // Given
        ScanFilter scanFilter = mock(ScanFilter.class);
        given(scanFilter.isFiltered(any())).willThrow(RuntimeException.class);
        given(scanner.getScanFilters()).willReturn(asList(scanFilter));
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
        verify(scanFilter).isFiltered(node);
        verify(scanner, times(0)).notifyFilteredMessage(any(), any());
    }

    @Test
    void shouldScanStartNodeSet() {
        // Given
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        hostProcess.setStartNode(node);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
    }

    @Test
    void shouldScanStartNodesAdded() {
        // Given
        StructuralNode node1 = createLeafNode("GET:file1", "GET", "http://localhost/file1");
        hostProcess.addStartNode(node1);
        StructuralNode node2 = createLeafNode("GET:file2", "GET", "http://localhost/file2");
        hostProcess.addStartNode(node2);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(2)));
    }

    @Test
    void shouldScanStartNodeAndChildrenIfScanChildrenIsEnabled() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode child1 =
                createLeafNode(parentNode, "GET:file1", "GET", "http://localhost/file1");
        StructuralNode child2 =
                createLeafNode(parentNode, "GET:file2", "GET", "http://localhost/file2");
        given(parentNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        hostProcess.addStartNode(parentNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(3)));
    }

    @Test
    void shouldNotScanStartNodeChildrenIfScanChildrenIsDisabled() {
        // Given
        given(scanner.scanChildren()).willReturn(false);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode child1 =
                createLeafNode(parentNode, "GET:file1", "GET", "http://localhost/file1");
        StructuralNode child2 =
                createLeafNode(parentNode, "GET:file2", "GET", "http://localhost/file2");
        given(parentNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        hostProcess.addStartNode(parentNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
    }

    @Test
    void shouldScanStartNodeAndMatchingChildrenSiblingsIfScanChildrenIsEnabled() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        StructuralNode dirGetNode =
                createLeafNode(parentNode, "GET:dir", "GET", "http://localhost/dir");
        StructuralNode otherFileNode =
                createLeafNode(parentNode, "GET:file", "GET", "http://localhost/file");
        given(parentNode.getChildIterator())
                .willReturn(asList(dirNode, dirGetNode, otherFileNode).iterator());
        hostProcess.addStartNode(dirGetNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(3)));
    }

    @Test
    void shouldNotScanStartNodeMatchingChildrenSiblingsIfScanChildrenIsDisabled() {
        // Given
        given(scanner.scanChildren()).willReturn(false);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        StructuralNode dirGetNode =
                createLeafNode(parentNode, "GET:dir", "GET", "http://localhost/dir");
        StructuralNode otherFileNode =
                createLeafNode(parentNode, "GET:file", "GET", "http://localhost/file");
        given(parentNode.getChildIterator())
                .willReturn(asList(dirNode, dirGetNode, otherFileNode).iterator());
        hostProcess.addStartNode(dirGetNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
    }

    @Test
    void shouldScanStartDirNodeAndChildrenIfScanChildrenIsEnabled() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        given(parentNode.getChildIterator()).willReturn(asList(dirNode).iterator());
        hostProcess.addStartNode(dirNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(3)));
    }

    @Test
    void shouldNotScanDirStartNodeChildrenIfScanChildrenIsDisabled() {
        // Given
        given(scanner.scanChildren()).willReturn(false);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        given(parentNode.getChildIterator()).willReturn(asList(dirNode).iterator());
        hostProcess.addStartNode(dirNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
    }

    @Test
    void shouldNotScanStartDirNodeMatchingSiblingEvenWithScanChildrenIsEnabled() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        StructuralNode dirGetNode =
                createLeafNode(parentNode, "GET:dir", "GET", "http://localhost/dir");
        StructuralNode otherFileNode =
                createLeafNode(parentNode, "GET:file", "GET", "http://localhost/file");
        given(parentNode.getChildIterator())
                .willReturn(asList(dirNode, dirGetNode, otherFileNode).iterator());
        hostProcess.addStartNode(dirNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(3)));
    }

    @Test
    void shouldScanStartNodeAndAllChildrenIfScanChildrenIsEnabled() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode child1 =
                createLeafNode(dirNode, "GET:file1", "GET", "http://localhost/dir/file1");
        StructuralNode child2 =
                createLeafNode(dirNode, "GET:file2", "GET", "http://localhost/dir/file2");
        given(dirNode.getChildIterator()).willReturn(asList(child1, child2).iterator());
        StructuralNode otherFileNode =
                createLeafNode(parentNode, "GET:file", "GET", "http://localhost/file");
        given(parentNode.getChildIterator()).willReturn(asList(dirNode, otherFileNode).iterator());
        hostProcess.addStartNode(parentNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(5)));
    }

    @Test
    void shouldNotScanDuplicatedChildDirNodes() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        StructuralNode dirNodeChild =
                createLeafNode(dirNode, "GET:file", "GET", "http://localhost/dir/file");
        given(dirNode.getChildIterator()).willReturn(asList(dirNodeChild).iterator());
        StructuralNode dirNodeGet =
                createLeafNode(parentNode, "GET:dir", "GET", "http://localhost/dir");
        StructuralNode pathNode = createDirNode(parentNode, "path", "http://localhost/path");
        StructuralNode pathNodeChild =
                createLeafNode(pathNode, "GET:file", "GET", "http://localhost/path/file");
        given(pathNode.getChildIterator()).willReturn(asList(pathNodeChild).iterator());
        StructuralNode pathNodeGet =
                createLeafNode(parentNode, "GET:path", "GET", "http://localhost/path");
        given(parentNode.getChildIterator())
                .willReturn(asList(dirNode, dirNodeGet, pathNodeGet, pathNode).iterator());
        hostProcess.addStartNode(parentNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(5)));
    }

    @Test
    void shouldScanStartNodeAndChildrenWithDifferentMethodsAndData() {
        // Given
        given(scanner.scanChildren()).willReturn(true);
        StructuralNode parentNode =
                createNode(rootNode, "http://localhost", "GET", "http://localhost");
        given(rootNode.getChildIterator()).willReturn(asList(parentNode).iterator());
        StructuralNode dirNode = createDirNode(parentNode, "dir", "http://localhost/dir");
        given(parentNode.getChildIterator()).willReturn(asList(dirNode).iterator());
        StructuralNode getPage =
                createLeafNode(dirNode, "GET:page", "GET", "http://localhost/dir/page");
        StructuralNode getPageAb =
                createLeafNode(
                        dirNode, "GET:page(a,b)", "GET", "http://localhost/dir/page?a=x&b=y");
        StructuralNode getPageCd =
                createLeafNode(
                        dirNode, "GET:page(c,d)", "GET", "http://localhost/dir/page?c=x&d=y");
        StructuralNode getPageOpAdd =
                createLeafNode(dirNode, "GET:page(add)", "GET", "http://localhost/dir/page?op=add");
        StructuralNode getPageOpEdit =
                createLeafNode(
                        dirNode, "GET:page(edit)", "GET", "http://localhost/dir/page?op=edit");
        StructuralNode postPageAb =
                createLeafNode(dirNode, "POST:page(a,b)", "POST", "http://localhost/dir/page");
        StructuralNode postPageCd =
                createLeafNode(dirNode, "POST:page(c,d)", "POST", "http://localhost/dir/page");
        given(dirNode.getChildIterator())
                .willReturn(
                        asList(
                                        getPage,
                                        getPageAb,
                                        getPageCd,
                                        getPageOpAdd,
                                        getPageOpEdit,
                                        postPageAb,
                                        postPageCd)
                                .iterator());
        hostProcess.addStartNode(parentNode);
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(9)));
    }

    @Test
    void shouldNotifyWhenCompleted() {
        // Given / When
        hostProcess.run();
        // Then
        verify(scanner).notifyHostProgress(hostAndPort, null, 100);
        verify(scanner).notifyHostComplete(hostAndPort);
    }

    @Test
    void shouldSetDealyIntoPlugin() {
        // Given
        int delay = 1234;
        given(scannerParam.getDelayInMs()).willReturn(delay);
        Plugin plugin = createPlugin(Plugin.class, 1);
        setupPluginFactoryWith(plugin);
        // When
        hostProcess.run();
        // Then
        verify(plugin).setDelayInMs(delay);
    }

    @Test
    void shouldUseTechSetAllByDefault() {
        assertThat(hostProcess.getTechSet(), is(equalTo(TechSet.getAllTech())));
    }

    @Test
    void shouldSetNonNullTechSet() {
        // Given
        TechSet techSet = mock(TechSet.class);
        // When
        hostProcess.setTechSet(techSet);
        // Then
        assertThat(hostProcess.getTechSet(), is(equalTo(techSet)));
    }

    @Test
    void shouldThrowWhenSettingNullTechSet() {
        // Given
        TechSet techSet = null;
        // When/ Then
        assertThrows(IllegalArgumentException.class, () -> hostProcess.setTechSet(techSet));
    }

    @Test
    void shouldSetTechSetIntoPlugin() {
        // Given
        TechSet techSet = mock(TechSet.class);
        Plugin plugin = createPlugin(Plugin.class, 1234);
        setupPluginFactoryWith(plugin);
        hostProcess.setTechSet(techSet);
        // When
        hostProcess.run();
        // Then
        verify(plugin).setTechSet(techSet);
    }

    @Test
    void shouldSkipPluginsIfNoStartNodes() {
        // Given
        int pluginId = 1234;
        setupPluginFactoryWith(createPlugin(Plugin.class, pluginId));
        // When
        hostProcess.run();
        // Then
        assertThat(hostProcess.getPluginStats(pluginId).isSkipped(), is(equalTo(true)));
        assertThat(
                hostProcess.getPluginStats(pluginId).getSkippedReason(),
                is(equalTo("no nodes to scan")));
        assertThat(hostProcess.getPluginStats(pluginId).getMessageCount(), is(equalTo(0)));
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(0)));
    }

    @Test
    void shouldSkipPluginIfItDoesNotTargetTechSet() {
        // Given
        int pluginId = 1234;
        Plugin plugin = createPlugin(Plugin.class, pluginId);
        setupPluginFactoryWith(plugin);
        TechSet techSet = mock(TechSet.class);
        given(plugin.targets(techSet)).willReturn(false);
        StructuralNode node = createLeafNode("GET:file", "GET", "http://localhost/file");
        hostProcess.setStartNode(node);
        hostProcess.setTechSet(techSet);
        // When
        hostProcess.run();
        // Then
        verify(plugin).targets(techSet);
        assertThat(hostProcess.getPluginStats(pluginId).isSkipped(), is(equalTo(true)));
        assertThat(
                hostProcess.getPluginStats(pluginId).getSkippedReason(),
                is(equalTo("scanner does not target selected technologies")));
        assertThat(hostProcess.getPluginStats(pluginId).getMessageCount(), is(equalTo(0)));
        assertThat(hostProcess.getTestTotalCount(), is(equalTo(1)));
    }

    @Test
    void isCustomPageShouldReturnTrueWhenCustomPageMatches() {
        // Given
        Context context = mock(Context.class);
        hostProcess.setContext(context);
        HttpMessage msg = new HttpMessage();
        CustomPage.Type cpType = CustomPage.Type.OTHER;
        given(context.isCustomPage(msg, cpType)).willReturn(true);
        // When / Then
        assertTrue(hostProcess.isCustomPage(msg, cpType));
        verify(context).isCustomPage(msg, cpType);
    }

    @Test
    void isCustomPageShouldReturnFalseWhenCustomPageDoesNotMatch() {
        // Given
        Context context = mock(Context.class);
        hostProcess.setContext(context);
        HttpMessage msg = new HttpMessage();
        CustomPage.Type cpType = CustomPage.Type.OTHER;
        given(context.isCustomPage(msg, cpType)).willReturn(false);
        // When / Then
        assertFalse(hostProcess.isCustomPage(msg, cpType));
        verify(context).isCustomPage(msg, cpType);
    }

    @Test
    void isCustomPageShouldReturnFalseWhenContextIsNull() {
        // Given
        Context context = mock(Context.class);
        hostProcess.setContext(null);
        HttpMessage msg = new HttpMessage();
        CustomPage.Type cpType = CustomPage.Type.OTHER;
        // When / Then
        assertFalse(hostProcess.isCustomPage(msg, cpType));
        verifyNoInteractions(context);
    }

    private static StructuralNode createLeafNode(String name, String method, String uri) {
        return createLeafNode(null, name, method, uri);
    }

    private static StructuralNode createLeafNode(
            StructuralNode parent, String name, String method, String uri) {
        StructuralNode node = createNode(parent, name, method, uri);
        given(node.isLeaf()).willReturn(true);
        given(node.getChildIterator()).willReturn(Collections.emptyIterator());
        return node;
    }

    private static StructuralNode createDirNode(StructuralNode parent, String name, String uri) {
        return createNode(parent, name, "GET", uri, HistoryReference.TYPE_TEMPORARY);
    }

    private static StructuralNode createNode(
            StructuralNode parent, String name, String method, String uri) {
        return createNode(parent, name, method, uri, HistoryReference.TYPE_ZAP_USER);
    }

    private static StructuralNode createNode(
            StructuralNode parent, String name, String method, String uri, int historyType) {
        StructuralNode node = mock(StructuralNode.class);
        given(node.getName()).willReturn(name);
        given(node.getMethod()).willReturn(method);
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getHistoryType()).willReturn(historyType);
        given(node.getHistoryReference()).willReturn(historyReference);
        try {
            given(node.getURI()).willReturn(new URI(uri, true));
            given(node.getParent()).willReturn(parent);
            given(historyReference.getHttpMessage()).willReturn(mock(HttpMessage.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        given(node.isSameAs(node)).willReturn(true);
        return node;
    }

    private void setupPluginFactoryWith(Plugin plugin) {
        given(pluginFactory.getPending()).willReturn(asList(plugin));
        given(pluginFactory.existPluginToRun()).willReturn(true, false);
        given(pluginFactory.nextPlugin()).willReturn(plugin);
    }

    private static <T extends Plugin> T createPlugin(Class<T> clazz, int id) {
        T plugin = mock(clazz);
        given(plugin.getId()).willReturn(id);
        given(plugin.getCodeName()).willReturn("MockedPlugin");
        return plugin;
    }
}
