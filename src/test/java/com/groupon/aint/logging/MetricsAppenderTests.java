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
import com.arpnetworking.metrics.Sink;
import com.arpnetworking.metrics.impl.TsdLogSink;
import com.arpnetworking.metrics.impl.TsdMetricsFactory;

import java.io.File;
import java.util.Collections;

/**
 * @author Ryan Ascheman (rascheman at groupon dot com)
 */
public class MetricsAppenderTests {
    public static MetricsFactory testMetricsFactory(final String logName) {
        File folder = new File("logs");
        folder.mkdirs();

        final Sink sink = new TsdLogSink.Builder()
                .setName(logName)
                .setDirectory(folder)
                .build();

        return new TsdMetricsFactory.Builder()
                .setClusterName("test")
                .setServiceName("logging_extensions")
                .setSinks(Collections.singletonList(sink))
                .build();
    }

    public static void attachAppenderToRootLogger(final MetricsAppender appender) {
        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        appender.setContext(rootLogger.getLoggerContext());
        appender.start();

        rootLogger.addAppender(appender);
    }
}
