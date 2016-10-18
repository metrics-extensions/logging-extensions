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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.arpnetworking.metrics.Metrics;
import com.arpnetworking.metrics.MetricsFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Logback appender that, when attached to a Logger, will increment a counter that correlates to the log level.
 * Primary use case: Attach to root Logger during application startup.
 *
 * @author Ryan Ascheman (rascheman at groupon dot com)
 */
public abstract class MetricsAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    /**
     * Metrics Factory.
     */
    private final MetricsFactory _metricsFactory;

    /**
     * How long to wait before first attempt to serialize metrics
     *
     * TimeUnit.SECONDS
     */
    private static final int _initialWriteMetricsDelay = 1;

    /**
     * How long to wait between attempts to serialize metrics
     *
     * TimeUnit.SECONDS
     */
    private static final int _writeMetricsPeriod = 1;

    /**
     * Package-private for extension
     */
    final AtomicReference<Metrics> _atomicMetrics;
    final AtomicBoolean _empty = new AtomicBoolean(true);
    final ReadWriteLock _lock = new ReentrantReadWriteLock();

    /**
     * @return Prefix to add to every counter
     */
    protected abstract String getMetricNamePrefix();

    /**
     * Public constructor.
     * Set metrics factory and start schedule for writing metrics to disk.
     *
     * @param metricsFactory Metrics Factory
     */
    MetricsAppender(final MetricsFactory metricsFactory) {
        _metricsFactory = metricsFactory;
        _atomicMetrics = new AtomicReference<>(_metricsFactory.create());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::writeMetrics,
                _initialWriteMetricsDelay,
                _writeMetricsPeriod,
                TimeUnit.SECONDS);
    }

    private void writeMetrics() {
        // Don't bother creating a new object if nothing has been counted
        if (_empty.get()) {
            return;
        }

        // Close the existing metrics object and create a new one
        final Lock lock = _lock.writeLock();
        final Metrics metrics;
        try {
            metrics = _atomicMetrics.getAndSet(_metricsFactory.create());
            _empty.set(true);
        } finally {
            lock.unlock();
        }

        metrics.close();
    }
}
