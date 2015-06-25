/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.parosproxy.paros;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zaproxy.zap.utils.I18N;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Constant.class)
public class CommandLineUnitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final Vector<CommandLineArgument[]> NO_EXTENSIONS_CUSTOM_ARGUMENTS = new Vector<>();
    private static final Map<String, CommandLineListener> NO_SUPPORTED_FILE_EXTENSIONS = Collections.emptyMap();

    @Mock
    private Constant constant;
    @Mock
    private I18N i18n;

    private CommandLine cmdLine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockConstantClass();
    }

    private void mockConstantClass() {
        constant = PowerMockito.mock(Constant.class);
        i18n = PowerMockito.mock(I18N.class);
        Whitebox.setInternalState(constant, "messages", i18n);
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), anyObject())).willReturn("");
    }

    @Test
    public void emptyCommandLine() throws Exception {
        cmdLine = new CommandLine(new String[] {});
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        assertTrue(cmdLine.isGUI());
        assertFalse(cmdLine.isDaemon());
        assertFalse(cmdLine.isReportVersion());
    }

    @Test
    public void daemonFlag() throws Exception {
        cmdLine = new CommandLine(new String[] { CommandLine.DAEMON });
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        assertFalse(cmdLine.isGUI());
        assertTrue(cmdLine.isDaemon());
        assertFalse(cmdLine.isReportVersion());
    }

    @Test
    public void shouldReportNonDaemonNorGuiIfSetCommandLineArgument() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.CMD });
        // Then
        assertThat(cmdLine.isDaemon(), is(equalTo(false)));
        assertThat(cmdLine.isGUI(), is(equalTo(false)));
    }

    @Test(expected = Exception.class)
    public void shouldFailIfSessionArgumentDoesNotHaveValue() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.SESSION });
        // Then = Exception.class
    }

    @Test
    public void shouldAcceptSessionArgument() throws Exception {
        // Given
        String argumentValue = "/Dummy/Session/Path";
        // When
        cmdLine = new CommandLine(new String[] { CommandLine.SESSION, argumentValue });
        // Then
        assertThat(cmdLine.getArgument(CommandLine.SESSION), is(equalTo(argumentValue)));
    }

    @Test(expected = Exception.class)
    public void shouldFailIfNewSessionArgumentDoesNotHaveValue() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.NEW_SESSION });
        // Then = Exception.class
    }

    @Test
    public void shouldAcceptNewSessionArgument() throws Exception {
        // Given
        String argumentValue = "/Dummy/Session/Path";
        // When
        cmdLine = new CommandLine(new String[] { CommandLine.NEW_SESSION, argumentValue });
        // Then
        assertThat(cmdLine.getArgument(CommandLine.NEW_SESSION), is(equalTo(argumentValue)));
    }

    @Test(expected = Exception.class)
    public void shouldFailIfPortArgumentDoesNotHaveValue() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.PORT });
        // Then = Exception.class
    }

    @Test(expected = Exception.class)
    public void shouldFailToParseInvalidPortArgument() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.PORT, "InvalidPort" });
        // Then = Exception.class
    }

    @Test
    public void shouldParseValidPortArgument() throws Exception {
        // Given
        int port = 8080;
        // When
        cmdLine = new CommandLine(new String[] { CommandLine.PORT, Integer.toString(port) });
        // Then
        assertThat(cmdLine.getPort(), is(equalTo(port)));
        assertThat(cmdLine.getArgument(CommandLine.PORT), is(equalTo("8080")));
    }

    @Test(expected = Exception.class)
    public void shouldFailIfHostArgumentDoesNotHaveValue() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] { CommandLine.HOST });
        // Then = Exception.class
    }

    @Test
    public void shouldParseHostArgument() throws Exception {
        // Given
        String hostname = "127.0.0.1";
        // When
        cmdLine = new CommandLine(new String[] { CommandLine.HOST, hostname });
        // Then
        assertThat(cmdLine.getHost(), is(equalTo(hostname)));
    }

    @Test
    public void shouldGetNullFromNonGivenArgument() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] {});
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then
        assertThat(cmdLine.getArgument("-NonGivenArgument"), is(equalTo(null)));
    }

    @Test
    public void shouldGetNullValueFromNonBuiltInArgument() throws Exception {
        // Given
        String argName = "-arg";
        Vector<CommandLineArgument[]> supportedArguments = new Vector<>();
        supportedArguments.add(new CommandLineArgument[] { new CommandLineArgument(argName, 1) });
        cmdLine = new CommandLine(new String[] { argName, "value" });
        // When
        cmdLine.parse(supportedArguments, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then
        assertThat(cmdLine.getArgument(argName), is(equalTo(null)));
    }

    @Test(expected = Exception.class)
    public void shouldFailIfGivenUnsupportedArgument() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] { "-unsupported" });
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then = Exception.class
    }

    @Test
    public void claWithoutArgs() throws Exception {
        cmdLine = new CommandLine(new String[] { "-a", "-b" });
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-a", 0, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-b", 0, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-c", 0, null, null, null) });
        cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);

        assertTrue(customArguments.get(0)[0].isEnabled());
        assertTrue(customArguments.get(1)[0].isEnabled());
        assertFalse(customArguments.get(2)[0].isEnabled());
    }

    /*@Test	TODO temp
    public void claWithArgs() throws Exception {
        cmdLine = new CommandLine(new String[] { "-a", "aaa", "-b", "bbb", "BBB" });
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-a", 1, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-b", 2, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-c", 3, null, null, null) });
        cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);

        assertTrue(customArguments.get(0)[0].isEnabled());
        assertThat(customArguments.get(0)[0].getArguments(), hasSize(1));
        assertThat(customArguments.get(0)[0].getArguments(), hasItem("aaa"));
        assertFalse(customArguments.get(0)[0].getArguments().contains("bbb"));

        assertTrue(customArguments.get(1)[0].isEnabled());
        assertThat(customArguments.get(1)[0].getArguments(), hasSize(2));
        assertFalse(customArguments.get(1)[0].getArguments().contains("aaa"));
        assertThat(customArguments.get(1)[0].getArguments(), hasItem("bbb"));
        assertThat(customArguments.get(1)[0].getArguments(), hasItem("BBB"));

        assertFalse(customArguments.get(2)[0].isEnabled());
    }
    */

    @Test
    public void claWithMissingArgs() throws Exception {
        cmdLine = new CommandLine(new String[] { "-a", "aaa", "-b", "bbb" });
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-a", 1, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-b", 2, null, null, null) });
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-c", 3, null, null, null) });
        try {
            cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);
            fail("Expected an exception");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void claWithPattern() throws Exception {
        cmdLine = new CommandLine(new String[] { "-script", "aaa", "bbb", "ccc" });
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(new CommandLineArgument[] { new CommandLineArgument("-script", -1, ".*", null, null) });
        cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);
        assertTrue(customArguments.get(0)[0].isEnabled());
        assertThat(customArguments.get(0)[0].getArguments().size(), is(equalTo(3)));
    }

    @Test(expected = Exception.class)
    public void shouldFailTheParseIfArgumentIsNotSupportedArgumentNorFile() throws Exception {
        // Given
        String notAFile = "NotAFile" + new Random().nextInt();
        cmdLine = new CommandLine(new String[] { notAFile });
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then = Exception.class
    }

    @Test(expected = Exception.class)
    public void shouldFailTheParseIfArgumentIsNotSupportedArgumentNorSupportedFileWithExtension() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] { "notsupported.test" });
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then = Exception.class
    }

    /*@Test TODO
    public void shouldAcceptFileArgumentIfHasSupportedFileExtension() throws Exception {
        // Given
        String fileExtension = "test";
        folder.create();
        File testFile = folder.newFile("aaa." + fileExtension);
        Map<String, CommandLineListener> supportedExtensions = new HashMap<>();
        supportedExtensions.put(fileExtension, new AcceptAllFilesCommandLineListener());
        cmdLine = new CommandLine(new String[] { testFile.toString() });
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, supportedExtensions);
        // Then = Accepted file argument
    }
    */

    @Test(expected = Exception.class)
    public void shouldNotAcceptFileArgumentIfRejectedBySupportedFileExtension() throws Exception {
        // Given
        String fileExtension = "test";
        File testFile = folder.newFile("aaa." + fileExtension);
        Map<String, CommandLineListener> supportedExtensions = new HashMap<>();
        supportedExtensions.put(fileExtension, new RejectAllFilesCommandLineListener());
        cmdLine = new CommandLine(new String[] { testFile.toString() });
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, supportedExtensions);
        // Then = Exception.class
    }

    private static class AcceptAllFilesCommandLineListener implements CommandLineListener {

        @Override
        public boolean handleFile(File file) {
            return true;
        }

        @Override
        public List<String> getHandledExtensions() {
            return null;
        }

        @Override
        public void execute(CommandLineArgument[] args) {
        }
    }

    private static class RejectAllFilesCommandLineListener implements CommandLineListener {

        @Override
        public boolean handleFile(File file) {
            return false;
        }

        @Override
        public List<String> getHandledExtensions() {
            return null;
        }

        @Override
        public void execute(CommandLineArgument[] args) {
        }
    }

}
