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
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.arpnetworking.metrics.MetricsFactory;

import java.util.concurrent.locks.Lock;

/**
 * @author Ryan Ascheman (rascheman at groupon dot com)
 */
public class ExceptionCounter extends MetricsAppender {

    public ExceptionCounter(MetricsFactory metricsFactory) {
        super(metricsFactory);
    }

    @Override
    protected String getMetricNamePrefix() {
        return "logging/exceptions/";
    }

    @Override
    protected void append(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null ||
            !(throwableProxy instanceof ThrowableProxy)) {
            return;
        }

        final ThrowableProxy throwableProxyImpl = (ThrowableProxy) throwableProxy;
        final String throwableName = throwableProxyImpl.getThrowable().getClass().getSimpleName();

        final Lock lock = _lock.readLock();
        try {
            _atomicMetrics.get().incrementCounter(getMetricNamePrefix() + throwableName);
        } finally {
            lock.unlock();
        }

        _empty.set(false);
    }
}
