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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit test for {@link Statistics}. */
class StatisticsUnitTest {

    private static final String STAT_KEY = "stat.key";

    @Test
    void shouldReturnNullValueIfStatNotPresent() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        Long value = statistics.getStat(STAT_KEY);
        // Then
        assertThat(value, is(nullValue()));
    }

    @Test
    void shouldIncreaseCounter() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.incCounter(STAT_KEY);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(1L)));
    }

    @Test
    void shouldIncreaseExistingCounter() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter(STAT_KEY);
        // When
        statistics.incCounter(STAT_KEY);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(2L)));
    }

    @Test
    void shouldIncreaseCounterWithGivenValue() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.incCounter(STAT_KEY, 5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(5L)));
    }

    @Test
    void shouldIncreaseExistingCounterWithGivenValue() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter(STAT_KEY);
        // When
        statistics.incCounter(STAT_KEY, 5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(6L)));
    }

    @Test
    void shouldIncreaseCounterWithNegativeValues() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.incCounter(STAT_KEY, -5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(-5L)));
    }

    @Test
    void shouldDecreaseCounter() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.decCounter(STAT_KEY);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(-1L)));
    }

    @Test
    void shouldDecreaseExistingCounter() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.decCounter(STAT_KEY);
        // When
        statistics.decCounter(STAT_KEY);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(-2L)));
    }

    @Test
    void shouldDecreaseCounterWithGivenValue() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.decCounter(STAT_KEY, 5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(-5L)));
    }

    @Test
    void shouldDecreaseExistingCounterWithGivenValue() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.decCounter(STAT_KEY);
        // When
        statistics.decCounter(STAT_KEY, 5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(-6L)));
    }

    @Test
    void shouldDecreaseCounterWithNegativeValues() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        // When
        statistics.decCounter(STAT_KEY, -5);
        // Then
        assertThat(statistics.getStat(STAT_KEY), is(equalTo(5L)));
    }

    @Test
    void shouldReturnStatsWithSamePrefix() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter("stats.a");
        statistics.incCounter("stats.b");
        statistics.incCounter("other.stats.a");
        statistics.incCounter("other.stats.b");
        // When
        Map<String, Long> stats = statistics.getStats("stats");
        // Then
        assertThat(
                stats,
                allOf(
                        hasKey("stats.a"),
                        hasKey("stats.b"),
                        not(hasKey("other.stats.a")),
                        not(hasKey("other.stats.b"))));
    }

    @Test
    void shouldReturnNoStatsIfNoneWithGivenPrefix() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter("other.stats.a");
        statistics.incCounter("other.stats.b");
        statistics.incCounter("other.stats.c");
        // When
        Map<String, Long> stats = statistics.getStats("stats");
        // Then
        assertThat(stats.size(), is(equalTo(0)));
    }

    @Test
    void shouldClearAllStats() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter("stats.a");
        statistics.incCounter("other.stats.a");
        // When
        statistics.clearAll();
        // Then
        assertThat(statistics.getStat("stats.a"), is(nullValue()));
        assertThat(statistics.getStat("other.stats.a"), is(nullValue()));
    }

    @Test
    void shouldClearStatsWithSamePrefix() throws Exception {
        // Given
        Statistics statistics = new Statistics();
        statistics.incCounter("stats");
        statistics.incCounter("stats.a");
        statistics.incCounter("stats.b");
        statistics.incCounter("other.stats.a");
        statistics.incCounter("other.stats.b");
        // When
        statistics.clear("stats");
        // Then
        assertThat(statistics.getStat("stats"), is(nullValue()));
        assertThat(statistics.getStat("stats.a"), is(nullValue()));
        assertThat(statistics.getStat("stats.b"), is(nullValue()));
        assertThat(statistics.getStat("other.stats.a"), is(not(nullValue())));
        assertThat(statistics.getStat("other.stats.b"), is(not(nullValue())));
    }
}
