/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.zaproxy.zap.db.sql;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.extension.option.DatabaseParam;

/** Unit test for {@link SqlTableHistory}. */
class SqlTableHistoryUnitTest {

    private SqlTableHistory table;

    @BeforeEach
    void setUp() throws Exception {
        DbSQL.setSqlProperties(new Properties());

        table = new SqlTableHistory();
    }

    @AfterEach
    void cleanUp() {
        DbSQL.reset();
    }

    @Test
    void shouldSetDatabaseOptions() {
        // Given
        DatabaseParam options = new DatabaseParam();
        // When / Then
        assertDoesNotThrow(() -> table.setDatabaseOptions(options));
    }

    @Test
    void shouldThrowWhenSettingNullDatabaseOptions() {
        // Given
        DatabaseParam options = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> table.setDatabaseOptions(options));
    }

    @Test
    void shouldReadDatabaseOptionsOnReconnect() {
        // Given
        DatabaseParam options = mock(DatabaseParam.class);
        table.setDatabaseOptions(options);
        Connection conn = mock(Connection.class);
        // When
        assertThrows(NullPointerException.class, () -> table.reconnect(conn));
        // Then
        verify(options).getRequestBodySize();
        verify(options).getResponseBodySize();
    }
}
