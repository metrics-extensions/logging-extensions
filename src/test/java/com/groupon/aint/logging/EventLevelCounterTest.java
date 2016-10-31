/**
 * Copyright 2016 Groupon.com
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
package com.groupon.aint.logging;

import com.arpnetworking.metrics.MetricsFactory;
import com.arpnetworking.steno.LoggerFactory;
import com.arpnetworking.steno.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * @author Ryan Ascheman (rascheman at groupon dot com)
 */
public class EventLevelCounterTest {

    @Test
    public void nonEmptyFile() throws Exception {
        final String logName = UUID.randomUUID().toString();
        final MetricsFactory metricsFactory = MetricsAppenderTests.testMetricsFactory(logName);

        MetricsAppenderTests.attachAppenderToRootLogger(new EventLevelCounter(metricsFactory));

        final Logger LOGGER = LoggerFactory.getLogger(EventLevelCounterTest.class);
        LOGGER.info().setMessage("testing").log();

        // Writing the metrics only happens once per second
        // Minimum value before file was written for local testing was ~1040, 1100 adds some padding
        Thread.sleep(1100);

        // Verify log level metric
        final File logFile = new File("logs/" + logName + ".log");
        Assert.assertTrue(logFile.exists());
        Assert.assertNotEquals(0L, logFile.length());

        final List<String> lines = Files.readAllLines(Paths.get(logFile.getPath()));
        Assert.assertEquals(1, lines.size());
        Assert.assertTrue(lines.get(0).contains("\"counters\":{\"logging/messages/INFO\":{\"values\":[{\"value\":1}]}}"));
    }

    @Test
    public void emptyFile() throws Exception {
        final String logName = UUID.randomUUID().toString();
        final MetricsFactory metricsFactory = MetricsAppenderTests.testMetricsFactory(logName);

        MetricsAppenderTests.attachAppenderToRootLogger(new EventLevelCounter(metricsFactory));

        // Writing the metrics only happens once per second
        // Minimum value before file was written for local testing was ~1040, 1100 adds some padding
        Thread.sleep(1100);

        // Verify log level metric
        final File logFile = new File("logs/" + logName + ".log");
        Assert.assertTrue(logFile.exists());
        Assert.assertEquals(0L, logFile.length());
    }
}
