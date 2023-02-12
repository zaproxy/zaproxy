/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.PostponedTasksRunner.DeleteFileTask;
import org.zaproxy.zap.control.PostponedTasksRunner.Task;
import org.zaproxy.zap.control.PostponedTasksRunner.UninstallAddOnTask;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link PostponedTasksRunner}. */
class PostponedTasksRunnerUnitTest extends AddOnTestUtils {

    private ZapXmlConfiguration config;
    private AddOnCollection aoc;
    private PostponedTasksRunner postponedTasks;

    @BeforeEach
    void setup() throws Exception {
        Constant.setZapHome(newTempDir("home").toAbsolutePath().toString());

        config = new ZapXmlConfiguration();
        config.setFile(createHomeFile("tasks.xml").toFile());
        aoc = mock(AddOnCollection.class);
        postponedTasks = new PostponedTasksRunner(config, aoc);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "NOT_VALID"})
    void shouldIgnoreUnsupportedTask(String type) throws Exception {
        // Given
        config.setProperty("postponedTasks.task(0).type", type);
        config.setProperty("postponedTasks.task(0).field", "value");
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertThat(postponedTasks.getTasks(), hasSize(0));
    }

    @Test
    void shouldAddUninstallAddOnTask() throws Exception {
        // Given
        Path file = createAddOnFile("addOnId.zap");
        AddOn addOn = new AddOn(file);
        // When
        postponedTasks.addUninstallAddOnTask(addOn);
        // Then
        assertPersistedUninstallAddOnTask(0, file);
    }

    @Test
    void shouldRunUninstallAddOnTask() throws Exception {
        // Given
        Path homeFile1 = createHomeFile("file1.txt");
        Path homeFile2 = createHomeFile("a/file2.txt");
        AddOn addOn =
                new AddOn(
                        createAddOnWithLibs(
                                manifest -> {
                                    manifest.append("<files>");
                                    manifest.append("<file>file1.txt</file>");
                                    manifest.append("<file>a/file2.txt</file>");
                                    manifest.append("</files>");
                                },
                                "lib1",
                                "lib2"));
        Path file = addOn.getFile().toPath();
        installLib(addOn, "lib1");
        installLib(addOn, "lib2");
        postponedTasks.addUninstallAddOnTask(addOn);
        // When
        postponedTasks.run();
        // Then
        assertThat(Files.notExists(file), is(equalTo(true)));
        assertThat(Files.notExists(homeFile1), is(equalTo(true)));
        assertThat(Files.notExists(homeFile2), is(equalTo(true)));
        assertThat(Files.notExists(AddOnInstaller.getAddOnDataDir(addOn)), is(equalTo(true)));
        verify(aoc).getAddOn("addon");
        verifyNoMoreInteractions(aoc);
        assertNoPersistedTasks();
    }

    @Test
    void shouldRemoveExistingAddOnFromAddOnCollectionOnUninstallAddOnTask() throws Exception {
        // Given
        String addOnId = "addOnId";
        Path file = createAddOnFile(addOnId + ".zap");
        AddOn existingAddOn = new AddOn(file);
        given(aoc.getAddOn(addOnId)).willReturn(existingAddOn);
        AddOn addOn = new AddOn(file);
        postponedTasks.addUninstallAddOnTask(addOn);
        // When
        postponedTasks.run();
        // Then
        assertThat(Files.notExists(file), is(equalTo(true)));
        verify(aoc).getAddOn(addOnId);
        verify(aoc).removeAddOn(existingAddOn);
        verifyNoMoreInteractions(aoc);
        assertNoPersistedTasks();
    }

    @Test
    void shouldNotRemoveExistingAddOnFromAddOnCollectionIfNotSameFileOnUninstallAddOnTask()
            throws Exception {
        // Given
        String addOnId = "addOnId";
        Path existingFile = createAddOnFile(addOnId + ".zap", "release", "1.4.2");
        AddOn existingAddOn = new AddOn(existingFile);
        given(aoc.getAddOn(addOnId)).willReturn(existingAddOn);
        Path file = createAddOnFile(addOnId + ".zap");
        AddOn addOn = new AddOn(file);
        postponedTasks.addUninstallAddOnTask(addOn);
        // When
        postponedTasks.run();
        // Then
        assertThat(Files.notExists(file), is(equalTo(true)));
        verify(aoc).getAddOn(addOnId);
        verify(aoc, times(0)).removeAddOn(existingAddOn);
        assertThat(Files.exists(existingFile), is(equalTo(true)));
        verifyNoMoreInteractions(aoc);
        assertNoPersistedTasks();
    }

    @Test
    void shouldReadUninstallAddOnTask() throws Exception {
        // Given
        Path file = createAddOnFile("addOnId.zap");
        config.setProperty("postponedTasks.task(0).type", "UNINSTALL_ADD_ON");
        config.setProperty("postponedTasks.task(0).addOn", file.toString());
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertUninstallAddOnTask(0, file);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "/not/path/to/file"})
    void shouldIgnoreUninstallAddOnTaskWithInvalidPath(String path) throws Exception {
        // Given
        config.setProperty("postponedTasks.task(0).type", "UNINSTALL_ADD_ON");
        config.setProperty("postponedTasks.task(0).addOn", path);
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertThat(postponedTasks.getTasks(), hasSize(0));
    }

    @Test
    void shouldIgnoreUninstallAddOnTaskWithInvalidAddOn() throws Exception {
        // Given
        Path file = createHomeFile("not-add-on.zap");
        config.setProperty("postponedTasks.task(0).type", "UNINSTALL_ADD_ON");
        config.setProperty("postponedTasks.task(0).addOn", file.toString());
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertThat(postponedTasks.getTasks(), hasSize(0));
    }

    @Test
    void shouldAddDeleteFileTask() throws Exception {
        // Given
        Path file = createHomeFile("file.txt");
        // When
        postponedTasks.addDeleteFileTask(file);
        // Then
        assertPersistedDeleteFileTask(0, file);
    }

    @Test
    void shouldRunDeleteFileTask() throws Exception {
        // Given
        Path file = createHomeFile("file.txt");
        postponedTasks.addDeleteFileTask(file);
        // When
        postponedTasks.run();
        // Then
        assertThat(Files.notExists(file), is(equalTo(true)));
        verifyNoInteractions(aoc);
        assertNoPersistedTasks();
    }

    @Test
    void shouldReadDeleteFileTask() throws Exception {
        // Given
        Path file = createHomeFile("file.txt");
        config.setProperty("postponedTasks.task(0).type", "DELETE_FILE");
        config.setProperty("postponedTasks.task(0).file", file.toString());
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertDeleteFileTask(0, file);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "/not/path/to/file"})
    void shouldIgnoreDeleteFileTaskWithInvalidPath(String path) throws Exception {
        // Given
        config.setProperty("postponedTasks.task(0).type", "DELETE_FILE");
        config.setProperty("postponedTasks.task(0).file", path);
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertThat(postponedTasks.getTasks(), hasSize(0));
    }

    @Test
    void shouldIgnoreDeleteFileTaskWithFileNotUnderHomeDir() throws Exception {
        // Given
        Path file = createFile();
        config.setProperty("postponedTasks.task(0).type", "DELETE_FILE");
        config.setProperty("postponedTasks.task(0).file", file.toString());
        // When
        postponedTasks = new PostponedTasksRunner(config, aoc);
        // Then
        assertThat(postponedTasks.getTasks(), hasSize(0));
    }

    private static Path createFile() throws IOException {
        return createFile(newTempDir().resolve("file.txt"));
    }

    private void assertUninstallAddOnTask(int idx, Path file) {
        Task task = postponedTasks.getTasks().get(idx);
        assertThat(task, is(instanceOf(UninstallAddOnTask.class)));
        UninstallAddOnTask uninstallAddOnTask = (UninstallAddOnTask) task;
        assertThat(uninstallAddOnTask.getType(), is(equalTo(Task.Type.UNINSTALL_ADD_ON)));
        assertThat(uninstallAddOnTask.getAddOn(), is(notNullValue()));
        assertThat(uninstallAddOnTask.getAddOn().getFile(), is(equalTo(file.toFile())));
    }

    private void assertDeleteFileTask(int idx, Path file) {
        Task task = postponedTasks.getTasks().get(idx);
        assertThat(task, is(instanceOf(DeleteFileTask.class)));
        DeleteFileTask deleteFileTask = (DeleteFileTask) task;
        assertThat(deleteFileTask.getType(), is(equalTo(Task.Type.DELETE_FILE)));
        assertThat(deleteFileTask.getFile(), is(equalTo(file)));
    }

    private void assertPersistedUninstallAddOnTask(int idx, Path file) {
        assertPersistedTask(idx, "UNINSTALL_ADD_ON", "addOn", file);
    }

    private void assertPersistedDeleteFileTask(int idx, Path file) {
        assertPersistedTask(idx, "DELETE_FILE", "file", file);
    }

    private void assertPersistedTask(int idx, String type, String propertyName, Path file) {
        String baseKey = "postponedTasks.task(" + idx + ").";
        assertThat(config.getProperty(baseKey + "type"), is(equalTo(type)));
        assertThat(config.getProperty(baseKey + propertyName), is(equalTo(file.toString())));
    }

    private void assertNoPersistedTasks() {
        assertThat(config.getProperties("postponedTasks.task").size(), is(equalTo(0)));
    }
}
