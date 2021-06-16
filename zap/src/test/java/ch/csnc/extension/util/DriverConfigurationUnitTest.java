/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package ch.csnc.extension.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit test for {@link DriverConfiguration}. */
class DriverConfigurationUnitTest {

    @Test
    void shouldCreateConfigurationFromFile() throws Exception {
        // Given
        File file = getDriversXmlFile();
        // When
        DriverConfiguration configuration = new DriverConfiguration(file);
        // Then
        assertThat(configuration.getNames(), contains("Name A", "Name B", "Name C"));
        assertThat(configuration.getPaths(), contains("Path A", "Path B", "Path C"));
        assertThat(configuration.getSlots(), contains(0, 2, 4));
        assertThat(configuration.getSlotIndexes(), contains(1, 3, 5));
    }

    @Test
    void shouldWriteConfigurationToFile(@TempDir Path tempDir) throws Exception {
        // Given
        File file = Files.createTempFile(tempDir, "conf", "").toFile();
        DriverConfiguration configuration = new DriverConfiguration(file);
        configuration.setNames(vector("Name A", "Name B", "Name C"));
        configuration.setPaths(vector("Path A", "Path B", "Path C"));
        configuration.setSlots(vector(0, 2, 4));
        configuration.setSlotListIndexes(vector(1, 3, 5));
        // When
        configuration.write();
        // Then
        assertThat(contents(file), is(equalTo(contents(getDriversXmlFile()))));
    }

    private static <T> Vector<T> vector(T value1, T value2, T value3) {
        return new Vector<>(Arrays.asList(value1, value2, value3));
    }

    private File getDriversXmlFile() throws URISyntaxException {
        return new File(getClass().getResource("drivers.xml").toURI());
    }

    private static List<String> contents(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }
}
