/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.parosproxy.paros;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link CommandLine}. */
@ExtendWith(MockitoExtension.class)
class CommandLineUnitTest {

    private static final Vector<CommandLineArgument[]> NO_EXTENSIONS_CUSTOM_ARGUMENTS =
            new Vector<>();
    private static final Map<String, CommandLineListener> NO_SUPPORTED_FILE_EXTENSIONS =
            Collections.emptyMap();

    @Mock(lenient = true)
    private I18N i18n;

    private CommandLine cmdLine;

    private static final String[][] TEST_CONF_VALUES = {
        {"aaa(0).aaa", "bbb"},
        {"aaa(0).bbb", "ccc"},
        {"aaa(0).ccc", "ddd"},
        {"aaa(0).ddd", "eee"},
        {"aaa(1).aaa", "ddd"},
        {"aaa(1).bbb", "eee"},
        {"aaa(1).ccc", "fff"},
        {"aaa(1).ddd", "ggg"}
    };

    @BeforeEach
    void setUp() throws Exception {
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), any())).willReturn("");
        Constant.messages = i18n;
    }

    @Test
    void shouldAcceptNullArguments() throws Exception {
        // Given
        String[] args = null;
        // When / Then
        assertDoesNotThrow(() -> new CommandLine(args));
    }

    @Test
    void shouldParseNullArguments() throws Exception {
        // Given
        String[] args = {null, null};
        cmdLine = new CommandLine(args);
        // When / Then
        assertDoesNotThrow(
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS));
    }

    @Test
    void emptyCommandLine() throws Exception {
        cmdLine = new CommandLine(new String[] {});
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        assertTrue(cmdLine.isGUI());
        assertFalse(cmdLine.isDaemon());
        assertFalse(cmdLine.isReportVersion());
    }

    @Test
    void daemonFlag() throws Exception {
        cmdLine = new CommandLine(new String[] {CommandLine.DAEMON});
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        assertFalse(cmdLine.isGUI());
        assertTrue(cmdLine.isDaemon());
        assertFalse(cmdLine.isReportVersion());
    }

    @Test
    void shouldReportNonDaemonNorGuiIfSetCommandLineArgument() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] {CommandLine.CMD});
        // Then
        assertThat(cmdLine.isDaemon(), is(equalTo(false)));
        assertThat(cmdLine.isGUI(), is(equalTo(false)));
    }

    @Test
    void shouldFailIfDaemonAndCommandLineArgumentsAreSet() throws Exception {
        // Given
        String[] args = {CommandLine.CMD, CommandLine.DAEMON};
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> new CommandLine(args));
        // Then
        assertThat(e.getMessage(), containsString("used at the same time"));
    }

    @Test
    void shouldFailIfSessionArgumentDoesNotHaveValue() throws Exception {
        // Given
        String[] args = {CommandLine.SESSION};
        // When / Then
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new CommandLine(args));
    }

    @Test
    void shouldAcceptSessionArgument() throws Exception {
        // Given
        String argumentValue = "/Dummy/Session/Path";
        // When
        cmdLine = new CommandLine(new String[] {CommandLine.SESSION, argumentValue});
        // Then
        assertThat(cmdLine.getArgument(CommandLine.SESSION), is(equalTo(argumentValue)));
    }

    @Test
    void shouldFailIfNewSessionArgumentDoesNotHaveValue() throws Exception {
        // Given
        String[] args = {CommandLine.NEW_SESSION};
        // When / Then
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new CommandLine(args));
    }

    @Test
    void shouldAcceptNewSessionArgument() throws Exception {
        // Given
        String argumentValue = "/Dummy/Session/Path";
        // When
        cmdLine = new CommandLine(new String[] {CommandLine.NEW_SESSION, argumentValue});
        // Then
        assertThat(cmdLine.getArgument(CommandLine.NEW_SESSION), is(equalTo(argumentValue)));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotParsePortArgument() throws Exception {
        // Given
        int port = 8080;
        // When
        cmdLine = new CommandLine(new String[] {CommandLine.PORT, Integer.toString(port)});
        // Then
        assertThat(cmdLine.getPort(), is(equalTo(-1)));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldNotParseHostArgument() throws Exception {
        // Given
        String hostname = "127.0.0.1";
        // When
        cmdLine = new CommandLine(new String[] {CommandLine.HOST, hostname});
        // Then
        assertThat(cmdLine.getHost(), is(nullValue()));
    }

    @Test
    void shouldHaveNoStdOutArgumentDisabledByDefault() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] {});
        // Then
        assertThat(cmdLine.isNoStdOutLog(), is(equalTo(false)));
    }

    @Test
    void shouldParseNoStdOutArgument() throws Exception {
        // Given / When
        cmdLine = new CommandLine(new String[] {CommandLine.NOSTDOUT});
        // Then
        assertThat(cmdLine.isNoStdOutLog(), is(equalTo(true)));
    }

    @Test
    void shouldGetNullFromNonGivenArgument() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] {});
        // When
        cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then
        assertThat(cmdLine.getArgument("-NonGivenArgument"), is(equalTo(null)));
    }

    @Test
    void shouldGetNullValueFromNonBuiltInArgument() throws Exception {
        // Given
        String argName = "-arg";
        Vector<CommandLineArgument[]> supportedArguments = new Vector<>();
        supportedArguments.add(new CommandLineArgument[] {new CommandLineArgument(argName, 1)});
        cmdLine = new CommandLine(new String[] {argName, "value"});
        // When
        cmdLine.parse(supportedArguments, NO_SUPPORTED_FILE_EXTENSIONS);
        // Then
        assertThat(cmdLine.getArgument(argName), is(equalTo(null)));
    }

    @Test
    void shouldFailIfGivenUnsupportedArgument() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] {"-unsupported"});
        // When / Then
        assertThrows(
                Exception.class,
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS));
    }

    @Test
    void shouldNotFailIfGivenUnsupportedArgumentAndNotReportUnsupported() throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] {"-unsupported"});
        // When / Then
        assertDoesNotThrow(
                () ->
                        cmdLine.parse(
                                NO_EXTENSIONS_CUSTOM_ARGUMENTS,
                                NO_SUPPORTED_FILE_EXTENSIONS,
                                false));
    }

    @Test
    void claWithoutArgs() throws Exception {
        cmdLine = new CommandLine(new String[] {"-a", "-b"});
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-a", 0, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-b", 0, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-c", 0, null, null, null)});
        cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);

        assertTrue(customArguments.get(0)[0].isEnabled());
        assertTrue(customArguments.get(1)[0].isEnabled());
        assertFalse(customArguments.get(2)[0].isEnabled());
    }

    @Test
    void claWithArgs() throws Exception {
        cmdLine = new CommandLine(new String[] {"-a", "aaa", "-b", "bbb", "BBB"});
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-a", 1, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-b", 2, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-c", 3, null, null, null)});
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

    @Test
    void claWithMissingArgs() throws Exception {
        cmdLine = new CommandLine(new String[] {"-a", "aaa", "-b", "bbb"});
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-a", 1, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-b", 2, null, null, null)});
        customArguments.add(
                new CommandLineArgument[] {new CommandLineArgument("-c", 3, null, null, null)});
        assertThrows(
                Exception.class,
                () -> cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS));
    }

    @Test
    void claWithPattern() throws Exception {
        cmdLine = new CommandLine(new String[] {"-script", "aaa", "bbb", "ccc"});
        Vector<CommandLineArgument[]> customArguments = new Vector<>();
        customArguments.add(
                new CommandLineArgument[] {
                    new CommandLineArgument("-script", -1, ".*", null, null)
                });
        cmdLine.parse(customArguments, NO_SUPPORTED_FILE_EXTENSIONS);
        assertTrue(customArguments.get(0)[0].isEnabled());
        assertThat(customArguments.get(0)[0].getArguments().size(), is(equalTo(3)));
    }

    @Test
    void shouldFailTheParseIfArgumentIsNotSupportedArgumentNorFile() throws Exception {
        // Given
        String notAFile = "NotAFile" + new Random().nextInt();
        cmdLine = new CommandLine(new String[] {notAFile});
        // When / Then
        assertThrows(
                Exception.class,
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS));
    }

    @Test
    void shouldFailTheParseIfArgumentIsNotSupportedArgumentNorSupportedFileWithExtension()
            throws Exception {
        // Given
        cmdLine = new CommandLine(new String[] {"notsupported.test"});
        // When / Then
        assertThrows(
                Exception.class,
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, NO_SUPPORTED_FILE_EXTENSIONS));
    }

    @Test
    void shouldAcceptFileArgumentIfHasSupportedFileExtension(@TempDir Path folder)
            throws Exception {
        // Given
        String fileExtension = "test";
        File testFile = Files.createFile(folder.resolve("aaa." + fileExtension)).toFile();
        Map<String, CommandLineListener> supportedExtensions = new HashMap<>();
        supportedExtensions.put(fileExtension, new AcceptAllFilesCommandLineListener());
        cmdLine = new CommandLine(new String[] {testFile.toString()});
        // When / Then = Accepted file argument
        assertDoesNotThrow(
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, supportedExtensions));
    }

    @Test
    void shouldNotAcceptFileArgumentIfRejectedBySupportedFileExtension(@TempDir Path folder)
            throws Exception {
        // Given
        String fileExtension = "test";
        File testFile = Files.createFile(folder.resolve("aaa." + fileExtension)).toFile();
        Map<String, CommandLineListener> supportedExtensions = new HashMap<>();
        supportedExtensions.put(fileExtension, new RejectAllFilesCommandLineListener());
        cmdLine = new CommandLine(new String[] {testFile.toString()});
        // When / Then
        assertThrows(
                Exception.class,
                () -> cmdLine.parse(NO_EXTENSIONS_CUSTOM_ARGUMENTS, supportedExtensions));
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
        public void execute(CommandLineArgument[] args) {}
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
        public void execute(CommandLineArgument[] args) {}
    }

    @Test
    void shouldMaintainConfigOrder() throws Exception {
        List<String> list = new ArrayList<>();
        for (String[] kv : TEST_CONF_VALUES) {
            list.add("-config");
            list.add(kv[0] + "=" + kv[1]);
        }
        String[] cl = new String[list.size()];
        cl = list.toArray(cl);
        cmdLine = new CommandLine(cl);
        Map<String, String> map = cmdLine.getOrderedConfigs();
        assertThat(map.size(), is(equalTo(8)));
        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        Entry<String, String> entry;
        for (String[] kv : TEST_CONF_VALUES) {
            entry = iter.next();
            assertThat(entry.getKey(), is(equalTo(kv[0])));
            assertThat(entry.getValue(), is(equalTo(kv[1])));
        }
    }

    @Test
    void shouldMaintainConfigfileOrder(@TempDir Path folder) throws Exception {
        File testFile = Files.createFile(folder.resolve("text.conf")).toFile();
        PrintWriter pw = new PrintWriter(testFile);
        for (String[] kv : TEST_CONF_VALUES) {
            pw.println(kv[0] + "=" + kv[1]);
        }
        pw.close();
        cmdLine = new CommandLine(new String[] {"-configfile", testFile.toString()});
        Map<String, String> map = cmdLine.getOrderedConfigs();
        assertThat(map.size(), is(equalTo(8)));
        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        Entry<String, String> entry;
        for (String[] kv : TEST_CONF_VALUES) {
            entry = iter.next();
            assertThat(entry.getKey(), is(equalTo(kv[0])));
            assertThat(entry.getValue(), is(equalTo(kv[1])));
        }
    }
}
